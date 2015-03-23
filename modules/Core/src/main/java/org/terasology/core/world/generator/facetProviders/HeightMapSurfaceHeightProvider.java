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

import com.google.common.collect.Sets;
import com.google.common.math.IntMath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.math.ChunkMath;
import org.terasology.math.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.math.Vector3i;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Produces(SurfaceHeightFacet.class)
@Requires(@Facet(SeaLevelFacet.class))
public class HeightMapSurfaceHeightProvider implements FacetProvider {

    private static final Logger logger = LoggerFactory.getLogger(HeightMapSurfaceHeightProvider.class);

    private static float[][] heightmap;

    private int mapWidth;
    private int mapHeight;

    private float heightOffset = 5;
    private float heightScale = 80;

    private int terrainScale = 4;

    @Override
    public void setSeed(long seed) {
        logger.info("Reading height map..");

        Texture texture = Assets.getTexture("core:platec_heightmap");
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

        heightmap = shiftArray(rotateArray(heightmap), -50, -100);

    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(SurfaceHeightFacet.class);
        SurfaceHeightFacet facet = new SurfaceHeightFacet(region.getRegion(), border);

        for (Vector2i pos : facet.getWorldRegion()) {
            int wrapX = IntMath.mod(pos.getX(), mapWidth * terrainScale);
            int wrapZ = IntMath.mod(pos.getY(), mapHeight * terrainScale);

            int mapX = wrapX / terrainScale;
            int mapZ = wrapZ / terrainScale;
            double p00 = heightmap[mapX][mapZ];
            double p10 = heightmap[(mapX - 1 + 512) % 512][(mapZ) % 512];
            double p11 = heightmap[(mapX - 1 + 512) % 512][(mapZ + 1 + 512) % 512];
            double p01 = heightmap[(mapX) % 512][(mapZ + 1 + 512) % 512];

            float relX = (wrapX % terrainScale) / (float) terrainScale;
            float relZ = (wrapZ % terrainScale) / (float) terrainScale;

            float interpolatedHeight = (float) lerp(relX, lerp(relZ, p10, p11), lerp(relZ, p00, p01));
            float height = heightOffset + heightScale * interpolatedHeight;

            facet.setWorld(pos, height);
        }

        region.setRegionFacet(SurfaceHeightFacet.class, facet);

    }

    //helper functions for the Mapdesign until real mapGen is in
    public static float[][] rotateArray(float[][] array) {
        float[][] newArray = new float[array[0].length][array.length];
        for (int i = 0; i < newArray.length; i++) {
            for (int j = 0; j < newArray[0].length; j++) {
                newArray[i][j] = array[j][array[j].length - i - 1];
            }
        }
        return newArray;
    }

    public static float[][] shiftArray(float[][] array, int x, int y) {
        int size = array.length;
        float[][] newArray = new float[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                newArray[i][j] = array[(i + x + size) % size][(j + y + size) % size];
            }
        }
        return newArray;
    }

    private static double lerp(double t, double a, double b) {
        return a + fade(t) * (b - a);  //not sure if i should fade t, needs a bit longer to generate chunks but is definately nicer
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
}
