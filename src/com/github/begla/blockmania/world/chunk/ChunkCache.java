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
import com.github.begla.blockmania.main.Configuration;
import com.github.begla.blockmania.utilities.MathHelper;
import com.github.begla.blockmania.world.WorldProvider;
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
public final class ChunkCache {

    private final FastMap<Integer, Chunk> _chunkCache = new FastMap<Integer, Chunk>(128000).shared();
    private final WorldProvider _parent;

    /**
     * @param parent
     */
    public ChunkCache(WorldProvider parent) {
        _parent = parent;
    }

    /**
     * Loads a specified chunk from cache or from the disk.
     * <p/>
     * NOTE: This method ALWAYS returns a valid chunk (if positive x and z values are provided)
     * since a new chunk is generated if none of the present chunks fit the request.
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
        c.setCached(true);

        return c;
    }

    public void freeCacheSpace() {
        if (_chunkCache.size() <= capacity()) {
            return;
        }

        FastList<Chunk> cachedChunks = new FastList<Chunk>(_chunkCache.values());
        Collections.sort(cachedChunks);

        if (_chunkCache.size() > capacity()) {
            Chunk chunkToDelete = cachedChunks.getLast();
            // Prevent further updates to this chunk
            chunkToDelete.setCached(false);
            // Write the chunk to disk (but do not remove it from the cache just now)
            writeChunkToDisk(chunkToDelete);
            // When the chunk is written, finally remove it from the cache
            _chunkCache.values().remove(chunkToDelete);
        }
    }

    /**
     * Writes all chunks to disk and disposes them.
     */
    public void saveAndDisposeAllChunks() {
        for (Chunk c : _chunkCache.values()) {
            c.setCached(false);
            writeChunkToDisk(c);
        }

        _chunkCache.clear();
    }

    /**
     * @return
     */
    public int size() {
        return _chunkCache.size();
    }

    /**
     * @return
     */
    public static int capacity() {
        return (Configuration.getSettingNumeric("V_DIST_X").intValue() * Configuration.getSettingNumeric("V_DIST_Z").intValue() + 1024);
    }

    private void writeChunkToDisk(Chunk c) {
        if (Blockmania.getInstance().isSandboxed())
            return;

        if (c.isFresh()) {
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
}
