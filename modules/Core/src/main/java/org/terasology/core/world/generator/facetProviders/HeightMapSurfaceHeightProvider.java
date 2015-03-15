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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.math.ChunkMath;
import org.terasology.math.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.math.Vector2i;
import org.terasology.math.geom.Vector3i;
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
import java.util.Set;

@Produces(SurfaceHeightFacet.class)
@Requires(@Facet(SeaLevelFacet.class))
public class HeightMapSurfaceHeightProvider implements FacetProvider {
    private static final Logger logger = LoggerFactory.getLogger(HeightMapSurfaceHeightProvider.class);
    private static final int MAX_HEIGHT = 256;

    private static float[][] heightmap;

    @Override
    public void setSeed(long seed) {
        logger.info("Reading height map..");

        Texture texture = Assets.getTexture("core:platec_heightmap");
        ByteBuffer[] bb = texture.getData().getBuffers();
        IntBuffer intBuf = bb[0].asIntBuffer();

        int width = texture.getWidth();
        int height = texture.getHeight();

        heightmap = new float[width][height];
        while (intBuf.position() < intBuf.limit()) {
            int pos = intBuf.position();
            int val = intBuf.get();
            heightmap[pos % width][pos / width] = val / (256 * 256 * 256 * 12.8f);
        }

        heightmap = shiftArray(rotateArray(heightmap), -50, -100);

    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(SurfaceHeightFacet.class);
        SurfaceHeightFacet facet = new SurfaceHeightFacet(region.getRegion(), border);

        Set<Vector3i> chunkCoordinates = Sets.newHashSet();
        for (Vector3i pos : border.expandTo3D(region.getRegion())) {
            chunkCoordinates.add(ChunkMath.calcChunkPos(pos));
        }

        for (Vector3i chunkCoordinate : chunkCoordinates) {
            Vector3i minWorldPosForChunk = new Vector3i(ChunkConstants.SIZE_X * chunkCoordinate.x,
                    ChunkConstants.SIZE_Y * chunkCoordinate.y,
                    ChunkConstants.SIZE_Z * chunkCoordinate.z);
            Region3i chunkWorldRegion = Region3i.createFromMinAndSize(minWorldPosForChunk, ChunkConstants.CHUNK_SIZE);

            int hmX = (((chunkWorldRegion.minX() / chunkWorldRegion.sizeX()) % 512) + 512) % 512;
            int hmZ = (((chunkWorldRegion.minZ() / chunkWorldRegion.sizeZ()) % 512) + 512) % 512;

            double scaleFactor = 0.05 * MAX_HEIGHT;

            double p00 = heightmap[hmX][hmZ] * scaleFactor;
            double p10 = heightmap[(hmX - 1 + 512) % 512][(hmZ) % 512] * scaleFactor;
            double p11 = heightmap[(hmX - 1 + 512) % 512][(hmZ + 1 + 512) % 512] * scaleFactor;
            double p01 = heightmap[(hmX) % 512][(hmZ + 1 + 512) % 512] * scaleFactor;

            Rect2i worldRegion = Rect2i.createFromMinAndSize(chunkWorldRegion.minX(),
                    chunkWorldRegion.minZ(),
                    chunkWorldRegion.sizeX(),
                    chunkWorldRegion.sizeZ());

            for (Vector2i pos : worldRegion) {
                Vector3i localPos = ChunkMath.calcBlockPos(new Vector3i(pos.x, 0, pos.y));
                int x = localPos.x;
                int z = localPos.z;
                //calculate avg height
                float interpolatedHeight = (float) lerp(x / (double) ChunkConstants.CHUNK_REGION.sizeX(), lerp(z / (double) ChunkConstants.CHUNK_REGION.sizeZ(), p10, p11),
                        lerp(z / (double) ChunkConstants.CHUNK_REGION.sizeZ(), p00, p01));

                if (facet.getWorldRegion().contains(pos)) {
                    facet.setWorld(pos, interpolatedHeight);
                }
            }
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
