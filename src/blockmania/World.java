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

import java.util.ArrayList;
import java.util.Collections;
import static org.lwjgl.opengl.GL11.*;

import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import noise.PerlinNoise;

import org.lwjgl.util.glu.*;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class World extends RenderObject {

    private long daylightTimer = Helper.getInstance().getTime();
    private boolean worldGenerated;
    private int displayListSun = -1;
    private Player player;
    private float daylight = 1.0f;
    private Random rand;
    // Used for updating/generating the world
    private Thread updateThread;
    private Thread worldThread;
    // The chunks to display
    private Chunk[][][] chunks;
    // Update queue for generating the light and vertex arrays
    private final PriorityBlockingQueue<Chunk> chunkUpdateQueue = new PriorityBlockingQueue<Chunk>(2048);
    // Update queue for generating the display lists
    private final PriorityBlockingQueue<Chunk> chunkUpdateQueueDL = new PriorityBlockingQueue<Chunk>(2048);
    private PerlinNoise pGen1;
    private PerlinNoise pGen2;
    private PerlinNoise pGen3;

    public World(String title, String seed, Player p) {
        this.player = p;
        rand = new Random(seed.hashCode());
        pGen1 = new PerlinNoise(rand.nextInt());
        pGen2 = new PerlinNoise(rand.nextInt());
        pGen3 = new PerlinNoise(rand.nextInt());
        final World currentWorld = this;

        chunks = new Chunk[(int) Configuration.viewingDistanceInChunks.x][(int) Configuration.viewingDistanceInChunks.y][(int) Configuration.viewingDistanceInChunks.z];

        updateThread = new Thread(new Runnable() {

            @Override
            public void run() {

                long timeStart = System.currentTimeMillis();

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Generating chunks. Please wait.");

                for (int x = 0; x < Configuration.viewingDistanceInChunks.x; x++) {
                    for (int y = 0; y < Configuration.viewingDistanceInChunks.y; y++) {
                        for (int z = 0; z < Configuration.viewingDistanceInChunks.z; z++) {
                            Chunk c = new Chunk(currentWorld, new Vector3f(x, y, z));
                            chunks[x][y][z] = c;
                            c.generate();
                            c.populate();
<<<<<<< HEAD
                            c.calcSunlight();
=======
>>>>>>> 0e9889760be50faada1af2b9337c2e5f395de758

                            queueChunkForUpdate(c);
                        }
                    }
                }

                setWorldGenerated(true);
                player.resetPlayer();

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "World updated ({0}s).", (System.currentTimeMillis() - timeStart) / 1000d);

                while (true) {
                    Chunk c = null;
                    synchronized (chunkUpdateQueueDL) {
                        c = chunkUpdateQueue.peek();
                        // Do not add a chunk which is beeing generated at the moment
                        if (chunkUpdateQueueDL.contains(c)) {
                            c = null;
                        } else {
                            chunkUpdateQueue.poll();
                        }
                    }

                    if (c != null) {
<<<<<<< HEAD
                        c.calcLight();
=======
                        c.calcSunlight();
>>>>>>> 0e9889760be50faada1af2b9337c2e5f395de758
                        c.generateVertexArray();
                        synchronized (chunkUpdateQueueDL) {
                            chunkUpdateQueueDL.add(c);
                        }
                    }
                }
            }
        });

        worldThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    updateInfWorld();

                    if (Helper.getInstance().getTime() - daylightTimer > 20000) {
                        daylight -= 0.2;

                        if (daylight <= 0.4f) {
                            daylight = 1.0f;
                        }

                        daylightTimer = Helper.getInstance().getTime();
                        updateAllChunks();
                    }
                }
            }
        });
    }

    public void init() {
        updateThread.start();
        worldThread.start();

        /**
         * Generates the display list used for displaying the sun.
         */
        Sphere s = new Sphere();
        displayListSun = glGenLists(1);
        glNewList(displayListSun, GL_COMPILE);
        glColor4f(1.0f, 0.8f, 0.0f, 1.0f);
        s.draw(256.0f, 16, 32);
        glEndList();


    }

    @Override
    public void render() {

        /**
         * Draws the sun.
         */
        glPushMatrix();
        glDisable(GL_FOG);
        glTranslatef(Configuration.viewingDistanceInChunks.x * Chunk.chunkDimensions.x * 1.5f + player.getPosition().x, Configuration.viewingDistanceInChunks.y * Chunk.chunkDimensions.y + player.getPosition().y, Configuration.viewingDistanceInChunks.z * Chunk.chunkDimensions.z * 1.5f + player.getPosition().z);
        glCallList(displayListSun);
        glEnable(GL_FOG);
        glPopMatrix();

        /**
         * Render all active chunks.
         */
        for (int x = 0; x < Configuration.viewingDistanceInChunks.x; x++) {
            for (int y = 0; y < Configuration.viewingDistanceInChunks.y; y++) {
                for (int z = 0; z < Configuration.viewingDistanceInChunks.z; z++) {
                    Chunk c = chunks[x][y][z];

                    if (c != null) {
                        c.render();
                    }
                }
            }
        }
    }

    /*
     * Update everything within the world (e.g. the chunks).
     */
    @Override
    public void update(long delta) {
        Chunk c = null;

        for (int i = 0; i < 32; i++) {
            synchronized (chunkUpdateQueueDL) {
                c = chunkUpdateQueueDL.peek();
            }

            if (c != null) {
                c.generateDisplayList();

                synchronized (chunkUpdateQueueDL) {
                    chunkUpdateQueueDL.remove(c);
                }
            }
        }
    }

    public void generateForest() {

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Generating a forest. Please stand by.");

        for (int x = 0; x < Configuration.viewingDistanceInChunks.x * Chunk.chunkDimensions.x; x++) {
            for (int y = 0; y < Configuration.viewingDistanceInChunks.y * Chunk.chunkDimensions.y; y++) {
                for (int z = 0; z < Configuration.viewingDistanceInChunks.z * Chunk.chunkDimensions.z; z++) {
                    if (getBlock(x, y, z) == 0x1) {
                        if (rand.nextFloat() > 0.9984f) {
                            if (rand.nextBoolean()) {
                                generateTree(x, y + 1, z);
                            } else {
                                generatePineTree(x, y + 1, z);
                            }
                        }
                    }
                }
            }
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished generating forest.");
    }

    public void generateTree(int posX, int posY, int posZ) {

        int height = rand.nextInt() % 2 + 6;

        // Generate tree trunk
        for (int i = 0; i < height; i++) {
            setBlock(posX, posY + i, posZ, 0x5);
        }

        // Generate the treetop
        for (int y = height - 2; y < height + 2; y += 1) {
            for (int x = -2; x < 3; x++) {
                for (int z = -2; z < 3; z++) {
                    setBlock(posX + x, posY + y, posZ + z, 0x6);
                }
            }
        }
    }

    public void generatePineTree(int posX, int posY, int posZ) {

        int height = rand.nextInt() % 2 + 12;

        // Generate tree trunk
        for (int i = 0; i < height; i++) {
            setBlock(posX, posY + i, posZ, 0x5);
        }

        // Generate the treetop
        for (int y = height / 4; y < height; y += 2) {
            for (int x = -(height / 2 - y / 2); x <= (height / 2 - y / 2); x++) {
                for (int z = -(height / 2 - y / 2); z <= (height / 2 - y / 2); z++) {
                    if (rand.nextFloat() < 0.95 && !(x == 0 && z == 0)) {
                        setBlock(posX + x, posY + y, posZ + z, 0x6);
                    }
                }
            }
        }
    }

    /**
     * Returns true if the given position is filled with a block.
     */
    public boolean isHitting(int x, int y, int z) {
        int chunkPosX = calcChunkPosX(x) % (int) Configuration.viewingDistanceInChunks.x;
        int chunkPosY = calcChunkPosY(y) % (int) Configuration.viewingDistanceInChunks.y;
        int chunkPosZ = calcChunkPosZ(z) % (int) Configuration.viewingDistanceInChunks.z;

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosY = calcBlockPosY(y, chunkPosY);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        try {
            Chunk c = chunks[chunkPosX][chunkPosY][chunkPosZ];
            return (c.getBlock(blockPosX, blockPosY, blockPosZ) > 0);
        } catch (Exception e) {
            return false;
        }
    }

    private int calcChunkPosX(int x) {
        return (x / (int) Chunk.chunkDimensions.x);
    }
<<<<<<< HEAD

    private int calcChunkPosY(int y) {
        return (y / (int) Chunk.chunkDimensions.y);
    }

    private int calcChunkPosZ(int z) {
        return (z / (int) Chunk.chunkDimensions.z);
    }

    private int calcBlockPosX(int x1, int x2) {
        x1 = x1 % ((int) Configuration.viewingDistanceInChunks.x * (int) Chunk.chunkDimensions.x);
        return (x1 - (x2 * (int) Chunk.chunkDimensions.x));
    }

    private int calcBlockPosY(int y1, int y2) {
        y1 = y1 % ((int) Configuration.viewingDistanceInChunks.y * (int) Chunk.chunkDimensions.y);
        return (y1 - (y2 * (int) Chunk.chunkDimensions.y));
    }

    private int calcBlockPosZ(int z1, int z2) {
        z1 = z1 % ((int) Configuration.viewingDistanceInChunks.z * (int) Chunk.chunkDimensions.z);
        return (z1 - (z2 * (int) Chunk.chunkDimensions.z));
    }

    public float getDaylight() {
        return daylight;
    }

    /**
     * TODO
     */
    public Player getPlayer() {
        return player;
    }

=======

    private int calcChunkPosY(int y) {
        return (y / (int) Chunk.chunkDimensions.y);
    }

    private int calcChunkPosZ(int z) {
        return (z / (int) Chunk.chunkDimensions.z);
    }

    private int calcBlockPosX(int x1, int x2) {
        x1 = x1 % ((int) Configuration.viewingDistanceInChunks.x * (int) Chunk.chunkDimensions.x);
        return (x1 - (x2 * (int) Chunk.chunkDimensions.x));
    }

    private int calcBlockPosY(int y1, int y2) {
        y1 = y1 % ((int) Configuration.viewingDistanceInChunks.y * (int) Chunk.chunkDimensions.y);
        return (y1 - (y2 * (int) Chunk.chunkDimensions.y));
    }

    private int calcBlockPosZ(int z1, int z2) {
        z1 = z1 % ((int) Configuration.viewingDistanceInChunks.z * (int) Chunk.chunkDimensions.z);
        return (z1 - (z2 * (int) Chunk.chunkDimensions.z));
    }

    public float getDaylight() {
        return daylight;
    }

    /**
     * TODO
     */
    public Player getPlayer() {
        return player;
    }

>>>>>>> 0e9889760be50faada1af2b9337c2e5f395de758
    public Vector3f getDaylightColor() {
        return new Vector3f((getDaylight() - 0.5f), getDaylight() - 0.25f, getDaylight());
    }

    /**
     * @return the worldGenerated
     */
    public boolean isWorldGenerated() {
        return worldGenerated;
    }

    /**
     * @param worldGenerated the worldGenerated to set
     */
    public void setWorldGenerated(boolean worldGenerated) {
        this.worldGenerated = worldGenerated;
    }

    /**
     * Sets the type of a block at a given position.
     */
    public final void setBlock(int x, int y, int z, int type) {
        int chunkPosX = calcChunkPosX(x) % (int) Configuration.viewingDistanceInChunks.x;
        int chunkPosY = calcChunkPosY(y) % (int) Configuration.viewingDistanceInChunks.y;
        int chunkPosZ = calcChunkPosZ(z) % (int) Configuration.viewingDistanceInChunks.z;

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosY = calcBlockPosY(y, chunkPosY);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        try {
            Chunk c = chunks[chunkPosX][chunkPosY][chunkPosZ];

            // Check if the chunk is valid
            if (c.getPosition().x != calcChunkPosX(x) || c.getPosition().y != calcChunkPosY(y) || c.getPosition().z != calcChunkPosZ(z)) {
                return;
            }

            // Generate or update the corresponding chunk
            c.setBlock(blockPosX, blockPosY, blockPosZ, type);

            queueChunkForUpdate(c);
        } catch (Exception e) {
            return;
        }
    }

    /**
     * Returns a block at a position by looking up the containing chunk.
     */
    public final int getBlock(int x, int y, int z) {
        int chunkPosX = calcChunkPosX(x) % (int) Configuration.viewingDistanceInChunks.x;
        int chunkPosY = calcChunkPosY(y) % (int) Configuration.viewingDistanceInChunks.y;
        int chunkPosZ = calcChunkPosZ(z) % (int) Configuration.viewingDistanceInChunks.z;

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosY = calcBlockPosY(y, chunkPosY);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        try {
            Chunk c = chunks[chunkPosX][chunkPosY][chunkPosZ];

            if (c.getPosition().x == calcChunkPosX(x) && c.getPosition().y == calcChunkPosY(y) && c.getPosition().z == calcChunkPosZ(z)) {
                return c.getBlock(blockPosX, blockPosY, blockPosZ);
            }
        } catch (Exception e) {
        }

        return -1;
    }

    /**
     * TODO.
     */
    public final float getLight(int x, int y, int z) {
        int chunkPosX = calcChunkPosX(x) % (int) Configuration.viewingDistanceInChunks.x;
        int chunkPosY = calcChunkPosY(y) % (int) Configuration.viewingDistanceInChunks.y;
        int chunkPosZ = calcChunkPosZ(z) % (int) Configuration.viewingDistanceInChunks.z;

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosY = calcBlockPosY(y, chunkPosY);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        try {
            Chunk c = chunks[chunkPosX][chunkPosY][chunkPosZ];
            if (c.getPosition().x == calcChunkPosX(x) && c.getPosition().y == calcChunkPosY(y) && c.getPosition().z == calcChunkPosZ(z)) {
                return c.getLight(blockPosX, blockPosY, blockPosZ);
            }
        } catch (Exception e) {
        }

        return -1f;
    }

<<<<<<< HEAD
    public final float getSunlight(int x, int y, int z) {
        int chunkPosX = calcChunkPosX(x) % (int) Configuration.viewingDistanceInChunks.x;
        int chunkPosY = calcChunkPosY(y) % (int) Configuration.viewingDistanceInChunks.y;
        int chunkPosZ = calcChunkPosZ(z) % (int) Configuration.viewingDistanceInChunks.z;

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosY = calcBlockPosY(y, chunkPosY);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        try {
            Chunk c = chunks[chunkPosX][chunkPosY][chunkPosZ];
            if (c.getPosition().x == calcChunkPosX(x) && c.getPosition().y == calcChunkPosY(y) && c.getPosition().z == calcChunkPosZ(z)) {
                return c.getSunlight(blockPosX, blockPosY, blockPosZ);
            }
        } catch (Exception e) {
        }

        return -1f;
    }

    private int calcPlayerChunkOffsetX() {
        return (int) ((player.getPosition().x - Helper.getInstance().calcPlayerOrigin().x) / Chunk.chunkDimensions.x);
    }

    private int calcPlayerChunkOffsetY() {
        return (int) ((player.getPosition().y - Helper.getInstance().calcPlayerOrigin().y) / Chunk.chunkDimensions.y);
    }

=======
    private int calcPlayerChunkOffsetX() {
        return (int) ((player.getPosition().x - Helper.getInstance().calcPlayerOrigin().x) / Chunk.chunkDimensions.x);
    }

    private int calcPlayerChunkOffsetY() {
        return (int) ((player.getPosition().y - Helper.getInstance().calcPlayerOrigin().y) / Chunk.chunkDimensions.y);
    }

>>>>>>> 0e9889760be50faada1af2b9337c2e5f395de758
    private int calcPlayerChunkOffsetZ() {
        return (int) ((player.getPosition().z - Helper.getInstance().calcPlayerOrigin().z) / Chunk.chunkDimensions.z);
    }

    private void updateInfWorld() {

        for (int x = 0; x < Configuration.viewingDistanceInChunks.x; x++) {
            for (int y = 0; y < Configuration.viewingDistanceInChunks.y; y++) {
                for (int z = 0; z < Configuration.viewingDistanceInChunks.z; z++) {
                    Chunk c = chunks[x][y][z];

                    if (c != null) {
                        Vector3f pos = new Vector3f(x, y, z);

                        int multZ = (int) calcPlayerChunkOffsetZ() / (int) Configuration.viewingDistanceInChunks.z + 1;

                        if (z < calcPlayerChunkOffsetZ() % Configuration.viewingDistanceInChunks.z) {
                            pos.z += Configuration.viewingDistanceInChunks.z * multZ;
                        } else {
                            pos.z += Configuration.viewingDistanceInChunks.z * (multZ - 1);
                        }

                        int multX = (int) calcPlayerChunkOffsetX() / (int) Configuration.viewingDistanceInChunks.x + 1;

                        if (x < calcPlayerChunkOffsetX() % Configuration.viewingDistanceInChunks.x) {
                            pos.x += Configuration.viewingDistanceInChunks.x * multX;
                        } else {
                            pos.x += Configuration.viewingDistanceInChunks.x * (multX - 1);
                        }

                        if (c.getPosition().x != pos.x || c.getPosition().z != pos.z) {
                            c.setPosition(pos);
                            c.generate();
                            c.populate();

                            queueChunkForUpdate(c);
                        }

                    }
                }
            }
        }
    }

    public void updateAllChunks() {
        for (int x = 0; x < Configuration.viewingDistanceInChunks.x; x++) {
            for (int y = 0; y < Configuration.viewingDistanceInChunks.y; y++) {
                for (int z = 0; z < Configuration.viewingDistanceInChunks.z; z++) {
                    Chunk c = chunks[x][y][z];
                    queueChunkForUpdate(c);
                }
            }
        }
    }

    /**
     * @return the pGen1
     */
    public PerlinNoise getpGen1() {
        return pGen1;
    }

    /**
     * @return the pGen2
     */
    public PerlinNoise getpGen2() {
        return pGen2;
    }

    /**
     * @return the pGen3
     */
    public PerlinNoise getpGen3() {
        return pGen3;
    }

    public String chunkUpdateStatus() {
        return String.format("U: %d UDL: %d", chunkUpdateQueue.size(), chunkUpdateQueueDL.size());
    }

    private void queueChunkForUpdate(Chunk c) {
        if (c != null) {
            // Add all neighbors
            ArrayList<Chunk> cs = new ArrayList<Chunk>();
            cs.add(c);

            try {
                cs.add(chunks[(int) c.getPosition().x + 1][(int) c.getPosition().y][(int) c.getPosition().z]);
            } catch (Exception e) {
            }

            try {
                cs.add(chunks[(int) c.getPosition().x - 1][(int) c.getPosition().y][(int) c.getPosition().z]);
            } catch (Exception e) {
            }

            try {
                cs.add(chunks[(int) c.getPosition().x][(int) c.getPosition().y][(int) c.getPosition().z + 1]);
            } catch (Exception e) {
            }

            try {
                cs.add(chunks[(int) c.getPosition().x][(int) c.getPosition().y][(int) c.getPosition().z - 1]);
            } catch (Exception e) {
            }

            synchronized (chunkUpdateQueueDL) {
                for (Chunk cc : cs) {
                    if (!chunkUpdateQueue.contains(cc) && cc != null) {
                        chunkUpdateQueue.add(cc);
                    }
                }
            }
        }
    }
}
