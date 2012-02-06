/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.logic.world;

import org.terasology.game.Terasology;
import org.terasology.logic.manager.ConfigurationManager;
import org.terasology.utilities.MathHelper;

import javax.vecmath.Vector3d;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Provides a dynamic cache for accessing chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class LocalChunkCache implements ChunkProvider {

    private static final boolean SAVE_CHUNKS = (Boolean) ConfigurationManager.getInstance().getConfig().get("System.saveChunks");
    private static final int CACHE_SIZE = (Integer) ConfigurationManager.getInstance().getConfig().get("System.chunkCacheSize");

    private static boolean _running = false;

    private final ConcurrentHashMap<Integer, Chunk> _chunkCache = new ConcurrentHashMap<Integer, Chunk>();
    private final LocalWorldProvider _parent;

    /**
     * Init. a new local chunk cache.
     *
     * @param parent The parent
     */
    public LocalChunkCache(LocalWorldProvider parent) {
        _parent = parent;
    }

    /**
     * Loads a specified chunk from the cache or from the disk.
     * <p/>
     * NOTE: This method ALWAYS returns a valid chunk since a new chunk is generated if none of the present
     * chunks fit the request.
     *
     * @param x X-coordinate of the chunk
     * @param z Z-coordinate of the chunk
     * @return The chunk
     */
    public Chunk loadOrCreateChunk(int x, int z) {
        int chunkId = MathHelper.cantorize(MathHelper.mapToPositive(x), MathHelper.mapToPositive(z));

        // Try to load the chunk from the cache
        Chunk c = _chunkCache.get(chunkId);

        // We got a chunk! Already! Great!
        if (c != null) {
            return c;
        }

        // Okay, seems like we've got some more stuff to do...
        Vector3d chunkPos = new Vector3d(x, 0, z);

        // Try to load the chunk from the disk
        c = loadChunkFromDisk(chunkPos);

        // Check if chunk has been loaded, otherwise create fresh chunk from scratch
        if (c == null) {
            c = new Chunk(_parent, chunkPos);
        }

        // Cache the chunk...
        _chunkCache.put(chunkId, c);

        // ... and finally return it
        return c;
    }

    /**
     * Removes old chunks from the cache if the size limit has been reached.
     */
    public void flushCache() {
        if (_running || _chunkCache.size() <= CACHE_SIZE)
            return;

        _running = true;

        Runnable r = new Runnable() {
            public void run() {
                ArrayList<Chunk> cachedChunks = new ArrayList<Chunk>(_chunkCache.values());
                Collections.sort(cachedChunks);

                if (cachedChunks.size() > CACHE_SIZE) {
                    Chunk chunkToDelete = cachedChunks.remove(cachedChunks.size() - 1);
                    // Write the chunk to disk (but do not remove it from the cache just jet)
                    writeChunkToDisk(chunkToDelete);
                    // When the chunk is written, finally remove it from the cache
                    _chunkCache.values().remove(chunkToDelete);

                    chunkToDelete.dispose();
                }

                _running = false;
            }
        };

        Terasology.getInstance().submitTask("Flush Chunk Cache", r);
    }

    /**
     * Writes all chunks to disk and disposes them.
     */
    public void dispose() {
        Runnable r = new Runnable() {
            public void run() {
                for (Chunk c : _chunkCache.values()) {
                    writeChunkToDisk(c);
                    c.dispose();
                }

                _chunkCache.clear();
            }
        };

        Terasology.getInstance().submitTask("Dispose Chunk", r);
    }

    /**
     * Writes a given chunk to the disk.
     *
     * @param c The chunk to save
     */
    private void writeChunkToDisk(Chunk c) {
        if (!SAVE_CHUNKS) {
            return;
        }

        File dirPath = new File(_parent.getWorldSavePath() + "/" + c.getChunkSavePath());
        if (!dirPath.exists()) {
            if (!dirPath.mkdirs()) {
                Terasology.getInstance().getLogger().log(Level.SEVERE, "Could not create save directory.");
                return;
            }
        }

        File f = new File(_parent.getWorldSavePath() + "/" + c.getChunkSavePath() + "/" + c.getChunkFileName());

        try {
            FileOutputStream fileOut = new FileOutputStream(f);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(c);
            out.close();
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tries to load a chunk at the given position from disk.
     *
     * @param chunkPos The position of the chunk
     * @return The loaded chunk, null if none was found
     */
    private Chunk loadChunkFromDisk(Vector3d chunkPos) {
        File f = new File(_parent.getWorldSavePath() + "/" + Chunk.getChunkSavePathForPosition(chunkPos) + "/" + Chunk.getChunkFileNameForPosition(chunkPos));

        if (!f.exists())
            return null;

        try {
            FileInputStream fileIn = new FileInputStream(f);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            Chunk result = (Chunk) in.readObject();
            result.setParent(_parent);

            in.close();
            fileIn.close();

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns the amount of cached chunks available in the cache.
     *
     * @return The amount of chunks
     */
    public int size() {
        return _chunkCache.size();
    }
}
