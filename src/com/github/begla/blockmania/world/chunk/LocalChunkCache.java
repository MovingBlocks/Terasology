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
package com.github.begla.blockmania.world.chunk;

import com.github.begla.blockmania.main.Blockmania;
import com.github.begla.blockmania.main.ConfigurationManager;
import com.github.begla.blockmania.utilities.MathHelper;
import com.github.begla.blockmania.world.LocalWorldProvider;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.lwjgl.util.vector.Vector3f;

import java.io.*;
import java.util.Collections;
import java.util.logging.Level;

/**
 * Provides a dynamic cache for caching chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class LocalChunkCache implements ChunkProvider {

    private static boolean _running = false;
    /* ------ */
    private final FastMap<Integer, Chunk> _chunkCache = new FastMap<Integer, Chunk>().shared();
    private final LocalWorldProvider _parent;

    /**
     * @param parent
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

        Vector3f chunkPos = new Vector3f(x, 0, z);
        // Init a new chunk
        c = loadChunkFromDisk(chunkPos);

        if (c == null) {
            c = new Chunk(_parent, chunkPos);
        }

        _chunkCache.put(chunkId, c);

        return c;
    }

    public void freeUnusedSpace() {
        if (_running || _chunkCache.size() <= capacity())
            return;

        _running = true;

        Runnable r = new Runnable() {
            public void run() {
                FastList<Chunk> cachedChunks = new FastList<Chunk>(_chunkCache.values());
                Collections.sort(cachedChunks);

                while (cachedChunks.size() > capacity()) {
                    Chunk chunkToDelete = cachedChunks.removeLast();
                    // Write the chunk to disk (but do not remove it from the cache just jet)
                    writeChunkToDisk(chunkToDelete);
                    // When the chunk is written, finally remove it from the cache
                    _chunkCache.values().remove(chunkToDelete);

                    chunkToDelete.dispose();
                }

                _running = false;
            }
        };

        Blockmania.getInstance().getThreadPool().execute(r);
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

        Blockmania.getInstance().getThreadPool().submit(r);
    }

    private void writeChunkToDisk(Chunk c) {
        if (c.isFresh() || (Boolean) ConfigurationManager.getInstance().getConfig().get("System.saveChunks")) {
            return;
        }

        File dirPath = new File(_parent.getWorldSavePath() + "/" + c.getChunkSavePath());
        if (!dirPath.exists()) {
            if (!dirPath.mkdirs()) {
                Blockmania.getInstance().getLogger().log(Level.SEVERE, "Could not create save directory.");
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

    private Chunk loadChunkFromDisk(Vector3f chunkPos) {
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

    public int size() {
        return _chunkCache.size();
    }

    public static int capacity() {
        int viewingDistanceX = (Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.viewingDistanceX");
        int viewingDistanceZ = (Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.viewingDistanceZ");

        return (viewingDistanceX * viewingDistanceZ + 512);
    }
}
