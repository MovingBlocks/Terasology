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
package com.github.begla.blockmania;

import java.io.IOException;
import static org.lwjgl.opengl.GL11.*;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.github.begla.blockmania.noise.PerlinNoise;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class World extends RenderObject {

    private static Texture _textureSun;
    private boolean _worldGenerated;
    private Player _player;
    private float _daylight = 0.95f;
    private Random _rand;
    // Used for updating/generating the world
    private Thread _updateThread;
    private Thread _worldThread;
    // The chunks to display
    private Chunk[][][] _chunks;
    // Update queue for generating the display lists
    private final ArrayBlockingQueue<Chunk> _chunkUpdateQueueDL = new ArrayBlockingQueue<Chunk>(512);
    private final ArrayBlockingQueue<Chunk> _chunkUpdateImportant = new ArrayBlockingQueue<Chunk>(512);
    private final ArrayBlockingQueue<Chunk> _chunkUpdateNormal = new ArrayBlockingQueue<Chunk>(512);
    private TreeMap<Integer, Chunk> _chunkCache = new TreeMap<Integer, Chunk>();
    private static final float SUN_SIZE = 60f;

    /*
     * Perlin noise generators used to generate the terrain, caves etc.
     */
    private PerlinNoise _pGen1;
    private PerlinNoise _pGen2;
    private PerlinNoise _pGen3;

    public World(String title, String seed, Player p) {
        this._player = p;
        _rand = new Random(seed.hashCode());
        _pGen1 = new PerlinNoise(_rand.nextInt());
        _pGen2 = new PerlinNoise(_rand.nextInt());
        _pGen3 = new PerlinNoise(_rand.nextInt());

        _chunks = new Chunk[(int) Configuration._viewingDistanceInChunks.x][(int) Configuration._viewingDistanceInChunks.y][(int) Configuration._viewingDistanceInChunks.z];

        _updateThread = new Thread(new Runnable() {

            @Override
            public void run() {

                long timeStart = System.currentTimeMillis();

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Generating chunks. Please wait.");

                for (int x = 0; x < Configuration._viewingDistanceInChunks.x; x++) {
                    for (int z = 0; z < Configuration._viewingDistanceInChunks.z; z++) {
                        Chunk c = loadOrCreateChunk(x, z);
                        _chunks[x][0][z] = c;
                        queueChunkForUpdate(c, 0);
                    }
                }

                _worldGenerated = true;
                _player.resetPlayer();

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "World updated ({0}s).", (System.currentTimeMillis() - timeStart) / 1000d);

                // Update queue for generating the light and vertex arrays
                PriorityBlockingQueue<Chunk> sortedUpdates = null;

                while (true) {

                    /**
                     * Create a sorted priority queue to update the chunks first,
                     * which are closest to the player.
                     */
                    sortedUpdates = new PriorityBlockingQueue<Chunk>();

                    for (Chunk c : _chunkUpdateNormal) {
                        sortedUpdates.add(c);
                    }

                    for (int i = 0; i < 4; i++) {
                        Chunk c = null;

                        if (_chunkUpdateImportant.size() > 0) {
                            c = _chunkUpdateImportant.poll();
                        } else {
                            c = sortedUpdates.poll();
                            _chunkUpdateNormal.remove(c);
                        }

                        if (c != null) {

                            c.generate();
                            c.calcLight();

                            // Update the light of the neighbors
                            Chunk[] neighbors = c.getNeighbors();

                            for (Chunk nc : neighbors) {
                                if (nc != null) {
                                    nc.generate();
                                    nc.calcLight();
                                }
                            }

                            for (Chunk nc : neighbors) {
                                if (nc != null) {
                                    nc.generateVertexArray();
                                    _chunkUpdateQueueDL.add(nc);
                                }
                            }

                            c.generateVertexArray();
                            _chunkUpdateQueueDL.add(c);
                        }
                    }
                }
            }
        });

        _worldThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    updateInfWorld();
                }
            }
        });
    }

    public void init() {
        try {
            _textureSun = TextureLoader.getTexture("PNG", new FileInputStream(Chunk.class.getResource("/com/github/begla/blockmania/images/sun.png").getPath()), GL_NEAREST);
        } catch (IOException ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }

        _updateThread.start();
        _worldThread.start();
    }

    @Override
    public void render() {

        /**
         * Draws the sun.
         */
        glPushMatrix();
        glTranslatef(_player.getPosition().x, Configuration._viewingDistanceInChunks.y * Chunk.CHUNK_DIMENSIONS.y * 1.25f, Configuration._viewingDistanceInChunks.z * Chunk.CHUNK_DIMENSIONS.z + _player.getPosition().z);
        glDisable(GL_FOG);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_TEXTURE_2D);
        _textureSun.bind();
        glColor4f(1f, 1f, 1f, 1.0f);

        glRotatef(-15f, 1f, 0f, 0f);

        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3f(-SUN_SIZE, SUN_SIZE, -SUN_SIZE);
        glTexCoord2f(1.f, 0.0f);
        glVertex3f(SUN_SIZE, SUN_SIZE, -SUN_SIZE);
        glTexCoord2f(1.f, 1.0f);
        glVertex3f(SUN_SIZE, -SUN_SIZE, -SUN_SIZE);
        glTexCoord2f(0.f, 1.0f);
        glVertex3f(-SUN_SIZE, -SUN_SIZE, -SUN_SIZE);
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);

        glEnable(GL_FOG);
        glPopMatrix();

        /**
         * Render all active chunks.
         */
        for (int x = 0; x < Configuration._viewingDistanceInChunks.x; x++) {
            for (int z = 0; z < Configuration._viewingDistanceInChunks.z; z++) {
                Chunk c = getChunk(x, 0, z);

                if (c != null) {
                    c.render();
                }
            }
        }
    }

    /*
     * Update everything within the world (e.g. the chunks).
     */
    @Override
    public void update(long delta) {
        Chunk c = _chunkUpdateQueueDL.poll();

        if (c != null) {
            c.generateDisplayList();
        }
    }

    public void generateForest() {
        for (int x = 0; x < Configuration._viewingDistanceInChunks.x * Chunk.CHUNK_DIMENSIONS.x; x++) {
            for (int y = 0; y < Configuration._viewingDistanceInChunks.y * Chunk.CHUNK_DIMENSIONS.y; y++) {
                for (int z = 0; z < Configuration._viewingDistanceInChunks.z * Chunk.CHUNK_DIMENSIONS.z; z++) {
                    if (getBlock(x, y, z) == 0x1) {
                        if (_rand.nextFloat() > 0.9984f) {
                            if (_rand.nextBoolean()) {
                                generateTree(x, y + 1, z, false);
                            } else {
                                generatePineTree(x, y + 1, z, false);
                            }
                        }
                    }
                }
            }
        }

        updateAllChunks();
    }

    public void generateTree(int posX, int posY, int posZ, boolean update) {

        int height = _rand.nextInt() % 2 + 6;

        // Generate tree trunk
        for (int i = 0; i < height; i++) {
            setBlock(posX, posY + i, posZ, 0x5, update);
        }

        // Generate the treetop
        for (int y = height - 2; y < height + 2; y += 1) {
            for (int x = -2; x < 3; x++) {
                for (int z = -2; z < 3; z++) {
                    if (!(x == -2 && z == -2) && !(x == 2 && z == 2) && !(x == -2 && z == 2) && !(x == 2 && z == -2)) {
                        setBlock(posX + x, posY + y, posZ + z, 0x6, update);
                    }
                }
            }
        }
    }

    public void generatePineTree(int posX, int posY, int posZ, boolean update) {

        int height = _rand.nextInt() % 4 + 12;

        // Generate tree trunk
        for (int i = 0; i < height; i++) {
            setBlock(posX, posY + i, posZ, 0x5, update);
        }

        // Generate the treetop
        for (int y = 0; y < 10; y += 2) {
            for (int x = -5 + y / 2; x <= 5 - y / 2; x++) {
                for (int z = -5 + y / 2; z <= 5 - y / 2; z++) {
                    if (!(x == 0 && z == 0)) {
                        setBlock(posX + x, posY + y + (height - 10), posZ + z, 0x6, update);
                    }
                }
            }
        }
    }

    /**
     * Returns true if the given position is filled with a block.
     */
    public boolean isHitting(int x, int y, int z) {
        int chunkPosX = calcChunkPosX(x) % (int) Configuration._viewingDistanceInChunks.x;
        int chunkPosY = calcChunkPosY(y) % (int) Configuration._viewingDistanceInChunks.y;
        int chunkPosZ = calcChunkPosZ(z) % (int) Configuration._viewingDistanceInChunks.z;

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosY = calcBlockPosY(y, chunkPosY);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        try {
            Chunk c = getChunk(chunkPosX, chunkPosY, chunkPosZ);
            return (c.getBlock(blockPosX, blockPosY, blockPosZ) > 0);
        } catch (Exception e) {
            return false;
        }
    }

    private int calcChunkPosX(int x) {
        return (x / (int) Chunk.CHUNK_DIMENSIONS.x);
    }

    private int calcChunkPosY(int y) {
        return (y / (int) Chunk.CHUNK_DIMENSIONS.y);
    }

    private int calcChunkPosZ(int z) {
        return (z / (int) Chunk.CHUNK_DIMENSIONS.z);
    }

    private int calcBlockPosX(int x1, int x2) {
        x1 = x1 % ((int) Configuration._viewingDistanceInChunks.x * (int) Chunk.CHUNK_DIMENSIONS.x);
        return (x1 - (x2 * (int) Chunk.CHUNK_DIMENSIONS.x));
    }

    private int calcBlockPosY(int y1, int y2) {
        y1 = y1 % ((int) Configuration._viewingDistanceInChunks.y * (int) Chunk.CHUNK_DIMENSIONS.y);
        return (y1 - (y2 * (int) Chunk.CHUNK_DIMENSIONS.y));
    }

    private int calcBlockPosZ(int z1, int z2) {
        z1 = z1 % ((int) Configuration._viewingDistanceInChunks.z * (int) Chunk.CHUNK_DIMENSIONS.z);
        return (z1 - (z2 * (int) Chunk.CHUNK_DIMENSIONS.z));
    }

    public float getDaylight() {
        return _daylight;
    }

    /**
     * TODO
     */
    public Player getPlayer() {
        return _player;
    }

    public Vector3f getDaylightColor() {
        return new Vector3f(getDaylight() - 0.3f, getDaylight() - 0.1f, getDaylight() - 0.1f);
    }

    /**
     * @return the worldGenerated
     */
    public boolean isWorldGenerated() {
        return _worldGenerated;
    }

    /**
     * Sets the type of a block at a given position.
     */
    public final void setBlock(int x, int y, int z, int type, boolean update) {
        int chunkPosX = calcChunkPosX(x) % (int) Configuration._viewingDistanceInChunks.x;
        int chunkPosY = calcChunkPosY(y) % (int) Configuration._viewingDistanceInChunks.y;
        int chunkPosZ = calcChunkPosZ(z) % (int) Configuration._viewingDistanceInChunks.z;

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosY = calcBlockPosY(y, chunkPosY);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        try {
            Chunk c = getChunk(chunkPosX, chunkPosY, chunkPosZ);

            // Check if the chunk is valid
            if (c.getPosition().x != calcChunkPosX(x) || c.getPosition().y != calcChunkPosY(y) || c.getPosition().z != calcChunkPosZ(z)) {
                c = loadOrCreateChunk(calcChunkPosX(x), calcChunkPosZ(z));
            }

            c.setBlock(blockPosX, blockPosY, blockPosZ, type);

            // Queue the chunk for update
            if (update) {
                queueChunkForUpdate(c, 1);
            }

        } catch (Exception e) {
        }
    }

    public final Chunk getChunk(int x, int y, int z) {
        Chunk c = null;

        try {
            c = _chunks[x % (int) Configuration._viewingDistanceInChunks.x][y % (int) Configuration._viewingDistanceInChunks.y][z % (int) Configuration._viewingDistanceInChunks.z];
        } catch (Exception e) {
        }

        return c;
    }

    /**
     * Returns a block at a position by looking up the containing chunk.
     */
    public final int getBlock(int x, int y, int z) {
        int chunkPosX = calcChunkPosX(x) % (int) Configuration._viewingDistanceInChunks.x;
        int chunkPosY = calcChunkPosY(y) % (int) Configuration._viewingDistanceInChunks.y;
        int chunkPosZ = calcChunkPosZ(z) % (int) Configuration._viewingDistanceInChunks.z;

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosY = calcBlockPosY(y, chunkPosY);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        try {
            Chunk c = getChunk(chunkPosX, chunkPosY, chunkPosZ);

            // Check if the chunk is valid
            if (c.getPosition().x != calcChunkPosX(x) || c.getPosition().y != calcChunkPosY(y) || c.getPosition().z != calcChunkPosZ(z)) {
                c = loadChunk(calcChunkPosX(x), calcChunkPosZ(z));
            }

            return c.getBlock(blockPosX, blockPosY, blockPosZ);
        } catch (Exception e) {
        }

        return 0;
    }

    /**
     * TODO.
     */
    public final float getLight(int x, int y, int z) {
        int chunkPosX = calcChunkPosX(x) % (int) Configuration._viewingDistanceInChunks.x;
        int chunkPosY = calcChunkPosY(y) % (int) Configuration._viewingDistanceInChunks.y;
        int chunkPosZ = calcChunkPosZ(z) % (int) Configuration._viewingDistanceInChunks.z;

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosY = calcBlockPosY(y, chunkPosY);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        try {
            Chunk c = getChunk(chunkPosX, chunkPosY, chunkPosZ);

            // Check if the chunk is valid
            if (c.getPosition().x != calcChunkPosX(x) || c.getPosition().y != calcChunkPosY(y) || c.getPosition().z != calcChunkPosZ(z)) {
                c = loadChunk(calcChunkPosX(x), calcChunkPosZ(z));
            }

            return c.getLight(blockPosX, blockPosY, blockPosZ);
        } catch (Exception e) {
        }

        return 0f;
    }

    /**
     * TODO.
     */
    public void setLight(int x, int y, int z, float intens) {
        int chunkPosX = calcChunkPosX(x) % (int) Configuration._viewingDistanceInChunks.x;
        int chunkPosY = calcChunkPosY(y) % (int) Configuration._viewingDistanceInChunks.y;
        int chunkPosZ = calcChunkPosZ(z) % (int) Configuration._viewingDistanceInChunks.z;

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosY = calcBlockPosY(y, chunkPosY);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        try {
            Chunk c = getChunk(chunkPosX, chunkPosY, chunkPosZ);

            // Check if the chunk is valid
            if (c.getPosition().x != calcChunkPosX(x) || c.getPosition().y != calcChunkPosY(y) || c.getPosition().z != calcChunkPosZ(z)) {
                c = loadOrCreateChunk(calcChunkPosX(x), calcChunkPosZ(z));
            }

            c.setLight(blockPosX, blockPosY, blockPosZ, intens);


        } catch (Exception e) {
        }
    }

    private int calcPlayerChunkOffsetX() {
        return (int) ((_player.getPosition().x - Helper.getInstance().calcPlayerOrigin().x) / Chunk.CHUNK_DIMENSIONS.x);
    }

    private int calcPlayerChunkOffsetY() {
        return (int) ((_player.getPosition().y - Helper.getInstance().calcPlayerOrigin().y) / Chunk.CHUNK_DIMENSIONS.y);
    }

    private int calcPlayerChunkOffsetZ() {
        return (int) ((_player.getPosition().z - Helper.getInstance().calcPlayerOrigin().z) / Chunk.CHUNK_DIMENSIONS.z);
    }

    private void updateInfWorld() {
        for (int x = 0; x < Configuration._viewingDistanceInChunks.x; x++) {
            for (int z = 0; z < Configuration._viewingDistanceInChunks.z; z++) {
                Chunk c = getChunk(x, 0, z);

                if (c != null) {
                    Vector3f pos = new Vector3f(x, 0, z);

                    int multZ = (int) calcPlayerChunkOffsetZ() / (int) Configuration._viewingDistanceInChunks.z + 1;

                    if (z < calcPlayerChunkOffsetZ() % Configuration._viewingDistanceInChunks.z) {
                        pos.z += Configuration._viewingDistanceInChunks.z * multZ;
                    } else {
                        pos.z += Configuration._viewingDistanceInChunks.z * (multZ - 1);
                    }

                    int multX = (int) calcPlayerChunkOffsetX() / (int) Configuration._viewingDistanceInChunks.x + 1;

                    if (x < calcPlayerChunkOffsetX() % Configuration._viewingDistanceInChunks.x) {
                        pos.x += Configuration._viewingDistanceInChunks.x * multX;
                    } else {
                        pos.x += Configuration._viewingDistanceInChunks.x * (multX - 1);
                    }

                    if (c.getPosition().x != pos.x || c.getPosition().z != pos.z) {
                        // Remove this chunk from further updates
                        _chunkUpdateNormal.remove(c);
                        // Try to load a cached version of the chunk
                        c = loadOrCreateChunk((int) pos.x, (int) pos.z);
                        // Replace the old chunk
                        _chunks[x][0][z] = c;
                        queueChunkForUpdate(c, 0);
                    }

                }
            }
        }
    }

    public void updateAllChunks() {
        for (int x = 0; x < Configuration._viewingDistanceInChunks.x; x++) {
            for (int y = 0; y < Configuration._viewingDistanceInChunks.y; y++) {
                for (int z = 0; z < Configuration._viewingDistanceInChunks.z; z++) {
                    Chunk c = getChunk(x, y, z);
                    queueChunkForUpdate(c, 0);
                }
            }
        }
    }

    public PerlinNoise getpGen1() {
        return _pGen1;
    }

    public PerlinNoise getpGen2() {
        return _pGen2;
    }

    public PerlinNoise getpGen3() {
        return _pGen3;
    }

    /*
     * Returns the vertices of a block at the given position.
     */
    public Vector3f[] verticesForBlockAt(int x, int y, int z) {
        Vector3f[] vertices = new Vector3f[8];

        vertices[0] = new Vector3f(x - .5f, y - .5f, z - .5f);
        vertices[1] = new Vector3f(x + .5f, y - .5f, z - .5f);
        vertices[2] = new Vector3f(x + .5f, y + .5f, z - .5f);
        vertices[3] = new Vector3f(x - .5f, y + .5f, z - .5f);

        vertices[4] = new Vector3f(x - .5f, y - .5f, z + .5f);
        vertices[5] = new Vector3f(x + .5f, y - .5f, z + .5f);
        vertices[6] = new Vector3f(x + .5f, y + .5f, z + .5f);
        vertices[7] = new Vector3f(x - .5f, y + .5f, z + .5f);

        return vertices;
    }

    /**
     * Calculates the intersection of a given ray originating from a specified point with
     * a block. Returns a list of intersections ordered by distance.
     */
    public ArrayList<RayFaceIntersection> rayBlockIntersection(int x, int y, int z, Vector3f origin, Vector3f ray) {
        /*
         * If the block is made out of air... panic and get out of here. Fast.
         */
        if (getBlock(x, y, z) == 0) {
            return null;
        }

        ArrayList<RayFaceIntersection> result = new ArrayList<RayFaceIntersection>();

        /**
         * Fetch all vertices of the specified block.
         */
        Vector3f[] vertices = verticesForBlockAt(x, y, z);
        Vector3f blockPos = new Vector3f(x, y, z);

        /*
         * Generate a new intersection for each side of the block.
         */

        // Front
        RayFaceIntersection is = rayFaceIntersection(blockPos, vertices[0], vertices[3], vertices[2], origin, ray);
        if (is != null) {
            result.add(is);
        }

        // Back
        is = rayFaceIntersection(blockPos, vertices[4], vertices[5], vertices[6], origin, ray);
        if (is != null) {
            result.add(is);
        }

        // Left
        is = rayFaceIntersection(blockPos, vertices[0], vertices[4], vertices[7], origin, ray);
        if (is != null) {
            result.add(is);
        }

        // Right
        is = rayFaceIntersection(blockPos, vertices[1], vertices[2], vertices[6], origin, ray);
        if (is != null) {
            result.add(is);
        }

        // Top
        is = rayFaceIntersection(blockPos, vertices[3], vertices[7], vertices[6], origin, ray);
        if (is != null) {
            result.add(is);
        }

        // Bottom
        is = rayFaceIntersection(blockPos, vertices[0], vertices[1], vertices[5], origin, ray);
        if (is != null) {
            result.add(is);
        }

        /*
         * Sort the intersections by distance.
         */
        Collections.sort(result);
        return result;
    }

    private RayFaceIntersection rayFaceIntersection(Vector3f blockPos, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f origin, Vector3f ray) {
        /**
         * Calculate the plane to intersect with.
         */
        Vector3f a = Vector3f.sub(v1, v0, null);
        Vector3f b = Vector3f.sub(v2, v0, null);
        Vector3f norm = Vector3f.cross(a, b, null);


        float d = -(norm.x * v0.x + norm.y * v0.y + norm.z * v0.z);

        /**
         * Calculate the distance on the ray, where the intersection occurs.
         */
        float t = -(norm.x * origin.x + norm.y * origin.y + norm.z * origin.z + d) / (Vector3f.dot(ray, norm));

        /**
         * Calc. the point of intersection.
         */
        Vector3f intersectPoint = new Vector3f(ray.x * t, ray.y * t, ray.z * t);
        Vector3f.add(intersectPoint, origin, intersectPoint);

        if (intersectPoint.x >= v0.x && intersectPoint.x <= v2.x && intersectPoint.y >= v0.y && intersectPoint.y <= v2.y && intersectPoint.z >= v0.z && intersectPoint.z <= v2.z) {
            return new RayFaceIntersection(blockPos, v0, v1, v2, d, t, origin, ray, intersectPoint);
        }

        return null;
    }

    private Chunk loadOrCreateChunk(int x, int z) {
        Chunk c = _chunkCache.get(Helper.getInstance().cantorize(x, z));

        if (c != null) {
            return c;
        }

        // Generate a new chunk, cache it and return it!
        c = new Chunk(this, new Vector3f(x, 0, z));
        _chunkCache.put(Helper.getInstance().cantorize(x, z), c);

        // Free some space
        while (_chunkCache.size() >= 512) {
            _chunkCache.pollFirstEntry();
        }

        return c;
    }

    private Chunk loadChunk(int x, int z) {
        return _chunkCache.get(Helper.getInstance().cantorize(x, z));
    }

    private void queueChunkForUpdate(Chunk c, int prio) {
        ArrayBlockingQueue queue = null;

        if (prio > 0) {
            queue = _chunkUpdateImportant;
        } else {
            queue = _chunkUpdateNormal;
        }

        queue.add(c);
    }

    @Override
    public String toString() {
        return String.format("world (cdl: %d, ci: %d, cn: %d, cache: %d)", _chunkUpdateQueueDL.size(), _chunkUpdateImportant.size(), _chunkUpdateNormal.size(), _chunkCache.size());
    }
}
