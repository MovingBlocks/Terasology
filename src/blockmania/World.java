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

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class World extends RenderObject {

    private float daylight = 0.75f;
    private Random rand;
    // Used for updating/generating the world
    private Thread updateThread;
    // The chunks to display
    private Chunk[][][] chunks;
    // Logger
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    // Day & Night
    private Timer dayNight = new Timer();
    /**
     * The perlin noise generator used
     * for creating the procedural terrain.
     */
    PerlinNoiseGenerator pGen;

    public World(String title, String seed) {
        rand = new Random(seed.hashCode());
        pGen = new PerlinNoiseGenerator(seed);

        chunks = new Chunk[(int) Configuration.viewingDistanceInChunks.x][(int) Configuration.viewingDistanceInChunks.y][(int) Configuration.viewingDistanceInChunks.z];

        updateThread = new Thread(new Runnable() {

            @Override
            public void run() {

                long timeStart = System.currentTimeMillis();

                LOGGER.log(Level.INFO, "Generating chunks.");

                for (int x = 0; x < Configuration.viewingDistanceInChunks.x; x++) {
                    for (int y = 0; y < Configuration.viewingDistanceInChunks.y; y++) {
                        for (int z = 0; z < Configuration.viewingDistanceInChunks.z; z++) {
                            generateChunk(new Vector3f(x, y, z));
                        }
                    }
                }

                LOGGER.log(Level.INFO, "World updated ({0}s).", (System.currentTimeMillis() - timeStart) / 1000d);
            }
        });
    }

    public void init() {
        dayNight.schedule(new TimerTask() {

            @Override
            public void run() {
                daylight -= 0.15;

                if (daylight < 0.25f) {
                    daylight = 0.75f;
                }

                for (int x = 0; x < Configuration.viewingDistanceInChunks.x; x++) {
                    for (int y = 0; y < Configuration.viewingDistanceInChunks.y; y++) {
                        for (int z = 0; z < Configuration.viewingDistanceInChunks.z; z++) {
                            Chunk c = chunks[x][y][z];

                            if (c != null) {
                                LOGGER.log(Level.INFO, "Updating daylight.");
                                c.markAsDirty();
                            }
                        }
                    }
                }
            }
        }, 30000, 30000);

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
                        if (chunkUpdates < 3) {

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

    /**
     * Generates the world.
     * NOTE: Will be replaced with a per-chunk-system later.
     */
    public final void generateChunk(Vector3f chunkPosition) {

        Vector3f chunkOrigin = new Vector3f(chunkPosition.x * Chunk.chunkDimensions.x, chunkPosition.y * Chunk.chunkDimensions.y, chunkPosition.z * Chunk.chunkDimensions.z);

        for (int x = (int) chunkOrigin.x; x < (int) chunkOrigin.x + Chunk.chunkDimensions.x; x++) {
            for (int z = (int) chunkOrigin.z; z < (int) chunkOrigin.z + Chunk.chunkDimensions.z; z++) {
                setBlock(new Vector3f(x, 0, z), 0x3, false);
            }

        }

        for (int x = (int) chunkOrigin.x; x < (int) chunkOrigin.x + Chunk.chunkDimensions.x; x++) {
            for (int z = (int) chunkOrigin.z; z < (int) chunkOrigin.z + Chunk.chunkDimensions.z; z++) {

                float height = calcTerrainElevation(x, z) + (calcTerrainRoughness(x, z) * calcTerrainDetail(x, z)) * 64 + 64;

                float y = height;

                while (y > 0) {
                    if (getCaveDensityAt(x, y, z) < 0.25) {

                        if (height == y) {
                            if (rand.nextFloat() < 150f / 100000f && height > 32) {
                                generateTree(new Vector3f(x, height, z));
                            }

                            setBlock(new Vector3f(x, y, z), 0x1, false);
                        } else {
                            setBlock(new Vector3f(x, y, z), 0x2, false);
                        }
                    }

                    y--;
                }

                // Generate water
                for (int i = 32; i > 0; i--) {
                    if (getBlock(new Vector3f(x, i, z)) == 0) {
                        setBlock(new Vector3f(x, i, z), 0x4, false);
                    }
                }
            }
        }

        Chunk c = chunks[(int) chunkPosition.x][(int) chunkPosition.y][(int) chunkPosition.z];

        if (c != null) {
            c.markAsDirty();
        }
    }

    private void generateTree(Vector3f pos) {

        int height = rand.nextInt() % 5 + 3;

        // Generate tree trunk
        for (int i = 0; i < height; i++) {
            setBlock(new Vector3f(pos.x, pos.y + i, pos.z), 0x5, true);
        }

        // Generate the treetop
        for (int y = height / 2; y < height + 6; y++) {
            for (int x = -4; x < 4; x++) {
                for (int z = -4; z < 4; z++) {
                    if (rand.nextFloat() < 0.75 && !(x == 0 && z == 0)) {
                        setBlock(new Vector3f(pos.x + x, pos.y + y, pos.z + z), 0x6, true);
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
    public final void setBlock(Vector3f pos, int type, boolean dirty) {
        Vector3f chunkPos = calcChunkPos(pos);
        Vector3f blockCoord = calcBlockPos(pos, chunkPos);

        try {
            Chunk c = chunks[(int) chunkPos.x][(int) chunkPos.y][(int) chunkPos.z];

            // Create a new chunk if needed
            if (c == null) {
                c = new Chunk(this, new Vector3f(chunkPos.x, chunkPos.y, chunkPos.z));
                //LOGGER.log(Level.INFO, "Generating chunk at X: {0}, Y: {1}, Z: {2}", new Object[]{chunkPos.x, chunkPos.y, chunkPos.z});
                chunks[(int) chunkPos.x][(int) chunkPos.y][(int) chunkPos.z] = c;
            }

            // Generate or update the corresponding chunk
            c.setBlock(blockCoord, type, dirty);
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
        result += pGen.noise(0.009f * x, 0.009f, 0.009f * z) * 128.0f;
        return result;
    }

    /**
     * Returns the roughness for the base terrain.
     */
    private float calcTerrainRoughness(float x, float z) {
        float result = 0.0f;
        result += pGen.noise(0.001f * x, 0.001f, 0.001f * z);
        return result;
    }

    /**
     * Returns the detail level for the base terrain.
     */
    private float calcTerrainDetail(float x, float z) {
        float result = 0.0f;
        result += pGen.noise(0.09f * x, 0.09f, 0.09f * z);
        return result;
    }

    /**
     * Returns the cave density for the base terrain.
     */
    private float getCaveDensityAt(float x, float y, float z) {
        float result = 0.0f;
        result += pGen.noise(0.02f * x, 0.02f * y, 0.02f * z);
        return result;
    }

    public float getDaylight() {
        return daylight;
    }
}
