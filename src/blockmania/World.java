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
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class World extends RenderObject {

    // Chunk bounding box for the occlusion queries
    private int chunkBoundingBoxID;
    // Used to determine occluded chunks
    private IntBuffer occlusionQueries;
    // Used for updating/generating the world
    Thread updateThread;
    // The chunks to display
    Chunk[][][] chunks;
    // Random number generator
    Random rand = new Random();
    /**
     * The perlin noise generator used
     * for creating the procedural terrain.
     */
    PerlinNoiseGenerator pGen = new PerlinNoiseGenerator();
    // Dimensions of the finite world
    public static final Vector3f worldDimensions = new Vector3f(1024, 256, 1024);

    public World() {
        chunks = new Chunk[(int) Configuration.viewingDistanceInChunks.x][(int) Configuration.viewingDistanceInChunks.y][(int) Configuration.viewingDistanceInChunks.z];

        occlusionQueries = BufferUtils.createIntBuffer(8192);
        GL15.glGenQueries(occlusionQueries);

        updateThread = new Thread(new Runnable() {

            @Override
            public void run() {
                updateWorld();
            }
        });

        updateThread.start();

        chunkBoundingBoxID = glGenLists(1);

        glNewList(chunkBoundingBoxID, GL_COMPILE);
            glColor3f(255.0f, 0.0f, 0.0f);
            glBegin(GL_LINE_LOOP);
            glVertex3f(0.0f, 0.0f, 0.0f);
            glVertex3f(Chunk.chunkDimensions.x, 0.0f, 0.0f);
            glVertex3f(Chunk.chunkDimensions.x, Chunk.chunkDimensions.y, 0.0f);
            glVertex3f(0.0f, Chunk.chunkDimensions.y, 0.0f);
            glEnd();

            glBegin(GL_LINE_LOOP);
            glVertex3f(0.0f, 0.0f, 0.0f);
            glVertex3f(0.0f, 0.0f, Chunk.chunkDimensions.z);
            glVertex3f(0.0f, Chunk.chunkDimensions.y, Chunk.chunkDimensions.z);
            glVertex3f(0.0f, Chunk.chunkDimensions.y, 0.0f);
            glVertex3f(0.0f, 0.0f, 0.0f);
            glEnd();

            glBegin(GL_LINE_LOOP);
            glVertex3f(0.0f, 0.0f, Chunk.chunkDimensions.z);
            glVertex3f(Chunk.chunkDimensions.x, 0.0f, Chunk.chunkDimensions.z);
            glVertex3f(Chunk.chunkDimensions.x, Chunk.chunkDimensions.y, Chunk.chunkDimensions.z);
            glVertex3f(0.0f, Chunk.chunkDimensions.y, Chunk.chunkDimensions.z);
            glVertex3f(0.0f, 0.0f, Chunk.chunkDimensions.z);
            glEnd();

            glBegin(GL_LINE_LOOP);
            glVertex3f(Chunk.chunkDimensions.x, 0.0f, 0.0f);
            glVertex3f(Chunk.chunkDimensions.x, 0.0f, Chunk.chunkDimensions.z);
            glVertex3f(Chunk.chunkDimensions.x, Chunk.chunkDimensions.y, Chunk.chunkDimensions.z);
            glVertex3f(Chunk.chunkDimensions.x, Chunk.chunkDimensions.y, 0.0f);
            glVertex3f(Chunk.chunkDimensions.x, 0.0f, 0.0f);
            glEnd();
            glEndList();

        glEndList();
    }

    /*
     * Renders the world.
     */
    @Override
    public void render() {

        glPushMatrix();

        glDisable(GL_COLOR_MATERIAL);
        //glDepthMask(false);
        //glColorMask(false, false, false, false);

//        for (int x = 0; x < (int) Configuration.viewingDistanceInChunks.x; x++) {
//            for (int y = 0; y < (int) Configuration.viewingDistanceInChunks.y; y++) {
//                for (int z = 0; z < (int) Configuration.viewingDistanceInChunks.z; z++) {
//
//                    glPushMatrix();
//                    glTranslatef(x * 16.0f, y * 128.0f, z * 16.0f);
//
//                    GL15.glBeginQuery(GL15.GL_SAMPLES_PASSED, occlusionQueries.get(64 * x + 2 * y + 64 * z));
//                    glCallList(chunkBoundingBoxID);
//                    GL15.glEndQuery(GL15.GL_SAMPLES_PASSED);
//
//                    glPopMatrix();
//                }
//
//            }
//        }

        glEnable(GL_COLOR_MATERIAL);
        glColorMask(true, true, true, true);
        glDepthMask(true);

        int chunkUpdates = 0;

        for (int x = 0; x < (int) Configuration.viewingDistanceInChunks.x; x++) {
            for (int y = 0; y < (int) Configuration.viewingDistanceInChunks.y; y++) {
                for (int z = 0; z < (int) Configuration.viewingDistanceInChunks.z; z++) {
                    if (chunks[x][y][z] != null) {

                        if (!updateThread.isAlive()) {

                            if (chunkUpdates < 5) {
                                if (chunks[x][y][z].updateDisplayList()) {
                                    chunkUpdates++;
                                }
                            }

                            chunks[x][y][z].render();
                        }
                    }
                }
            }
        }


        glPopMatrix();
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

//        for (int x = 0; x < worldDimensions.x; x++) {
//            for (int z = 0; z < worldDimensions.z; z++) {
//                for (int y = 0; y < 128; y++) {
//                    if (getCaveDensityAt(x, y, z) < 0.5) {
//                        setBlock(new Vector3f(x, y, z), 0x3);
//                    }
//                }
//            }
//        }
//
//                for (int x = 0; x < worldDimensions.x; x++) {
//            for (int z = 0; z < worldDimensions.z; z++) {
//                for (int y = 128; y >= 128 && y < 140; y++) {
//                    if (getCaveDensityAt(x, y, z) < 0.8) {
//                        setBlock(new Vector3f(x, y, z), 0x2);
//                    }
//                }
//            }
//        }



        System.out.println("World updated (" + (System.currentTimeMillis() - timeStart) / 1000d + "s).");

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
    float calcTerrainElevation(float x, float y) {
        float result = 0.0f;
        result += pGen.noise(0.2f * x, 0.2f * y, 0.2f);
        return result;
    }

    /**
     * Returns the roughness for the base terrain.
     */
    float calcTerrainRoughness(float x, float y) {
        float result = 0.0f;
        result += pGen.noise(0.01f * x, 0.01f * y, 0.01f);
        return result;
    }

    /**
     * Returns the detail level for the base terrain.
     */
    float calcTerrainDetail(float x, float y) {
        float result = 0.0f;
        result += pGen.noise(0.1f * x, 0.1f * y, 1.318293f);
        return result;
    }

    /**
     * Returns the cave density for the base terrain.
     */
    float getCaveDensityAt(float x, float y, float z) {
        float result = 0.0f;
        result += pGen.noise(0.005f * x, 0.005f * y, 0.005f * z);
        return result;
    }
}
