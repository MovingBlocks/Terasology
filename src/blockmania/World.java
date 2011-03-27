/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package blockmania;

import org.lwjgl.BufferUtils;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL15;
import java.util.Random;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class World extends RenderObject {

    Random rand;
    // Title of the world
    String title = "WORLD1";
    // Seed
    String seed = "SEED1";
    // Used for updating/generating the world
    Thread updateThread;
    // The chunks to display
    Chunk[][][] chunks;
    /**
     * The perlin noise generator used
     * for creating the procedural terrain.
     */
    PerlinNoiseGenerator pGen;
    // Dimensions of the finite world
    public static final Vector3f worldDimensions = new Vector3f(128, 256, 128);

    public World(String title, String seed) {
        rand = new Random(seed.hashCode());
        pGen = new PerlinNoiseGenerator(seed);

        chunks = new Chunk[(int) Configuration.viewingDistanceInChunks.x][(int) Configuration.viewingDistanceInChunks.y][(int) Configuration.viewingDistanceInChunks.z];

        updateThread = new Thread(new Runnable() {

            @Override
            public void run() {
                updateWorld();
            }
        });

        updateThread.start();
    }

    /*
     * Update the world.
     */
    @Override
    public void update(long delta) {
        int chunkUpdates = 0;

        for (int x = 0; x < (int) Configuration.viewingDistanceInChunks.x; x++) {
            for (int y = 0; y < (int) Configuration.viewingDistanceInChunks.y; y++) {
                for (int z = 0; z < (int) Configuration.viewingDistanceInChunks.z; z++) {

                    if (chunks[x][y][z] != null) {
                        if (!updateThread.isAlive()) {
                            if (chunkUpdates < 15) {

                                boolean updated1 = chunks[x][y][z].updateDisplayList(true);
                                boolean updated2 = chunks[x][y][z].updateDisplayList(false);

                                if (updated1 || updated2) {
                                    chunkUpdates++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates the world.
     * NOTE: Will be replaced with a per-chunk-system later.
     */
    public final void updateWorld() {

        long timeStart = System.currentTimeMillis();

        for (int x = 0; x < worldDimensions.x; x++) {
            for (int z = 0; z < worldDimensions.z; z++) {
                setBlock(new Vector3f(x, 0, z), 0x3);
            }

        }

        for (int x = 0; x < worldDimensions.x; x++) {
            for (int z = 0; z < worldDimensions.z; z++) {

                float height = calcTerrainElevation(x, z) + (calcTerrainRoughness(x, z) * calcTerrainDetail(x, z)) * 64 + 64;

                float y = height;

                while (y > 0) {
                    if (getCaveDensityAt(x, y, z) > 0) {
                        if (height == y) {
                            setBlock(new Vector3f(x, y, z), 0x1);

                            if (rand.nextFloat() < 0.01f) {
                                generateTree(new Vector3f(x, y, z));
                            }
                        } else {
                            setBlock(new Vector3f(x, y, z), 0x2);
                        }
                    }

                    if (y < 50) {
                        if (getBlock(new Vector3f(x, y, z)) == 0) {
                            setBlock(new Vector3f(x, y, z), 0x4);
                        }
                    }
                    y--;
                }

            }
        }

        System.out.println("World updated (" + (System.currentTimeMillis() - timeStart) / 1000d + "s).");
    }

    private void generateTree(Vector3f pos) {

        int height = rand.nextInt() % 5 + 6;

        // Generate tree trunk
        for (int i = 0; i < height; i++) {
            setBlock(new Vector3f(pos.x, pos.y + i, pos.z), 0x5);
        }

        // Generate the treetop
        for (int y = height / 2; y < height+2; y++) {
            for (int x = -3; x < 3; x++) {
                for (int z = -3; z < 3; z++) {
                    if (rand.nextFloat() < 0.6 && x != 0 && z != 0) {
                        setBlock(new Vector3f(pos.x + x, pos.y + y, pos.z + z), 0x6);
                    }
                }
            }
        }

    }

    /**
     * Sets the type of a block at a given position.
     * @param pos
     * @param type
     */
    public final void setBlock(Vector3f pos, int type) {
        Vector3f chunkPos = calcChunkPos(pos);
        Vector3f blockCoord = calcBlockPos(pos, chunkPos);

        try {
            Chunk c = chunks[(int) chunkPos.x][(int) chunkPos.y][(int) chunkPos.z];

            // Create a new chunk if needed
            if (c == null) {
                //System.out.println("Generating chunk at X: " + chunkPos.x + ", Y: " + chunkPos.y + ", Z: " + chunkPos.z);
                c = new Chunk(this, new Vector3f(chunkPos.x, chunkPos.y, chunkPos.z));
                chunks[(int) chunkPos.x][(int) chunkPos.y][(int) chunkPos.z] = c;
            }

            // Generate or update the corresponding chunk
            c.setBlock(blockCoord, type);
        } catch (Exception e) {
            return;
        }
    }

    /**
     * Returns a block at a position by looking up the containing chunk.
     */
    public final int getBlock(Vector3f pos) {
        Vector3f chunkPos = calcChunkPos(pos);
        Vector3f blockCoord = calcBlockPos(pos, chunkPos);

        try {
            Chunk c = chunks[(int) chunkPos.x][(int) chunkPos.y][(int) chunkPos.z];
            return c.getBlock((int) blockCoord.x, (int) blockCoord.y, (int) blockCoord.z);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Returns true if the given position is hitting a block below.
     */
    public boolean isHitting(Vector3f pos) {
        Vector3f chunkPos = calcChunkPos(pos);
        Vector3f blockCoord = calcBlockPos(pos, chunkPos);

        try {
            Chunk c = chunks[(int) chunkPos.x][(int) chunkPos.y][(int) chunkPos.z];
            return (c.getBlock((int) blockCoord.x, (int) blockCoord.y, (int) blockCoord.z) > 0);
        } catch (Exception e) {
            return false;
        }

    }

    /**
     * Calculate the corresponding chunk for a given position within the world.
     */
    private Vector3f calcChunkPos(Vector3f pos) {
        return new Vector3f((int) (pos.x / Chunk.chunkDimensions.x), (int) (pos.y / Chunk.chunkDimensions.y), (int) (pos.z / Chunk.chunkDimensions.z));
    }

    /**
     * Calculate the position of a world-block within a specific chunk.
     */
    private Vector3f calcBlockPos(Vector3f pos, Vector3f chunkPos) {
        return new Vector3f(pos.x - (chunkPos.x * Chunk.chunkDimensions.x), pos.y - (chunkPos.y * Chunk.chunkDimensions.y), pos.z - (chunkPos.z * Chunk.chunkDimensions.z));
    }

    /**
     * Returns the base elevation for the terrain.
     */
    private float calcTerrainElevation(float x, float z) {
        float result = 0.0f;
        result += pGen.noise(0.2f * x, 0.2f, 0.2f * z);
        return result;
    }

    /**
     * Returns the roughness for the base terrain.
     */
    private float calcTerrainRoughness(float x, float z) {
        float result = 0.0f;
        result += pGen.noise(0.01f * x, 0.01f, 0.01f * z);
        return result;
    }

    /**
     * Returns the detail level for the base terrain.
     */
    private float calcTerrainDetail(float x, float z) {
        float result = 0.0f;
        result += pGen.noise(0.1f * x, 0.1f, 0.1f * z);
        return result;
    }

    /**
     * Returns the cave density for the base terrain.
     */
    private float getCaveDensityAt(float x, float y, float z) {
        float result = 0.0f;
        result += pGen.noise(0.005f * x, 0.005f * y, 0.005f * z);
        return result;
    }
}
