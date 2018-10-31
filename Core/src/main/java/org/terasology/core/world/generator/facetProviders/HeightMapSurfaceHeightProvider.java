/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.core.world.generator.facetProviders;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.Component;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.properties.OneOf.List;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.rendering.nui.properties.OneOf.Enum;

import com.google.common.math.IntMath;

@Produces(SurfaceHeightFacet.class)
@Requires(@Facet(SeaLevelFacet.class))
public class HeightMapSurfaceHeightProvider implements ConfigurableFacetProvider {

    public enum WrapMode {
        CLAMP,
        REPEAT
    }

    private static final Logger logger = LoggerFactory.getLogger(HeightMapSurfaceHeightProvider.class);

    private float[][] heightmap;

    private int mapWidth;
    private int mapHeight;

    private HeightMapConfiguration configuration = new HeightMapConfiguration();

    @Override
    public void setSeed(long seed) {
        initialize();
    }

    @Override
    public void initialize() {
        if (heightmap == null) {
            reloadHeightmap();
        }
    }

    private void reloadHeightmap() {
        logger.info("Reading height map '{}'", configuration.heightMap);

        ResourceUrn urn = new ResourceUrn("core", configuration.heightMap);
        Texture texture = Assets.getTexture(urn).get();
        ByteBuffer[] bb = texture.getData().getBuffers();
        IntBuffer intBuf = bb[0].asIntBuffer();

        mapWidth = texture.getWidth();
        mapHeight = texture.getHeight();

        heightmap = new float[mapWidth][mapHeight];
        while (intBuf.position() < intBuf.limit()) {
            int pos = intBuf.position();
            long val = intBuf.get() & 0xFFFFFFFFL;
            heightmap[pos % mapWidth][pos / mapWidth] = val / (256 * 256 * 256 * 256f);
        }
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(SurfaceHeightFacet.class);
        SurfaceHeightFacet facet = new SurfaceHeightFacet(region.getRegion(), border);

        for (BaseVector2i pos : facet.getWorldRegion().contents()) {
            int xzScale = configuration.terrainScale;

            int mapX0;
            int mapZ0;
            int mapX1;
            int mapZ1;
            switch (configuration.wrapMode) {
                case CLAMP:
                    mapX0 = TeraMath.clamp(pos.getX(), 0, mapWidth * xzScale - 1) / xzScale;
                    mapZ0 = TeraMath.clamp(pos.getY(), 0, mapHeight * xzScale - 1) / xzScale;
                    mapX1 = TeraMath.clamp(mapX0 + 1, 0, mapWidth - 1);
                    mapZ1 = TeraMath.clamp(mapZ0 + 1, 0, mapHeight - 1);
                    break;
                case REPEAT:
                    mapX0 = IntMath.mod(pos.getX(), mapWidth * xzScale) / xzScale;
                    mapZ0 = IntMath.mod(pos.getY(), mapHeight * xzScale) / xzScale;
                    mapX1 = IntMath.mod(mapX0 + 1, mapWidth);
                    mapZ1 = IntMath.mod(mapZ0 + 1, mapHeight);
                    break;
                default:
                    throw new UnsupportedOperationException("Not supported: " + configuration.wrapMode);
            }

            double p00 = heightmap[mapX0][mapZ0];
            double p10 = heightmap[mapX1][mapZ0];
            double p11 = heightmap[mapX1][mapZ1];
            double p01 = heightmap[mapX0][mapZ1];

            float relX = IntMath.mod(pos.getX(), xzScale) / (float) xzScale;
            float relZ = IntMath.mod(pos.getY(), xzScale) / (float) xzScale;

            float interpolatedHeight = (float) lerp(relX, lerp(relZ, p00, p01), lerp(relZ, p10, p11));
            float height = configuration.heightOffset + configuration.heightScale * interpolatedHeight;

            facet.setWorld(pos, height);
        }

        region.setRegionFacet(SurfaceHeightFacet.class, facet);

    }

    private static double lerp(double t, double a, double b) {
        return a + fade(t) * (b - a);  //not sure if i should fade t, needs a bit longer to generate chunks but is definately nicer
    }

    private static double fade(double t) {
        // This is Perlin
//        return t * t * t * (t * (t * 6 - 15) + 10);

        // This is Hermite
        return t * t * (3 - 2 * t);
    }

    @Override
    public String getConfigurationName() {
        return "Height Map";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        String prevHeightMap = this.configuration.heightMap;
        this.configuration = (HeightMapConfiguration) configuration;

        if (!Objects.equals(prevHeightMap, this.configuration.heightMap)) {
            reloadHeightmap();
        }
    }

    private static class HeightMapConfiguration implements Component {

        @Enum(description = "Wrap Mode")
        private WrapMode wrapMode = WrapMode.REPEAT;

        @List(items = { "platec_heightmap", "opposing_islands" }, description = "Height Map")
        private String heightMap = "platec_heightmap";

        @Range(min = 0, max = 50f, increment = 1f, precision = 0, description = "Height Offset")
        private float heightOffset = 12;

        @Range(min = 10, max = 200f, increment = 1f, precision = 0, description = "Height Scale Factor")
        private float heightScale = 70;

        @Range(min = 1, max = 32, increment = 1, precision = 0, description = "Terrain Scale Factor")
        private int terrainScale = 8;
    }
}
