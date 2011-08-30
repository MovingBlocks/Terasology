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
package com.github.begla.blockmania.world;

import com.github.begla.blockmania.Configuration;
import com.github.begla.blockmania.utilities.MathHelper;
import javolution.util.FastList;

import java.util.Collections;
import java.util.TreeMap;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkCache {

    private final TreeMap<Integer, Chunk> _chunkCache = new TreeMap<Integer, Chunk>();
    private final World _parent;

    /**
     * @param _parent
     */
    public ChunkCache(World _parent) {
        this._parent = _parent;
    }

    /**
     * Loads a specified chunk from cache or queues a new chunk for generation.
     * <p/>
     * NOTE: This method ALWAYS returns a valid chunk (if positive x and z values are provided)
     * since a new chunk is generated if none of the present chunks fit the request.
     *
     * @param x X-coordinate of the chunk
     * @param z Z-coordinate of the chunk
     * @return The chunk
     */
    public synchronized Chunk loadOrCreateChunk(int x, int z) {
        // Catch negative values
        if (x < 0 || z < 0) {
            return null;
        }

        // Try to load the chunk from the cache
        Chunk c = _chunkCache.get(MathHelper.cantorize(x, z));

        // We got a chunk! Already! Great!
        if (c != null) {
            return c;
        }

        // Init a new chunk
        c = _parent.prepareNewChunk(x, z);
        _chunkCache.put(c.getChunkId(), c);

        return c;
    }

    public synchronized void freeCache() {
        if (_chunkCache.size() <= capacity()) {
            return;
        }

        FastList<Chunk> cachedChunks = new FastList(_chunkCache.values());
        Collections.sort(cachedChunks);

        while (cachedChunks.size() > capacity()) {
            Chunk chunkToDelete = cachedChunks.removeLast();
            _chunkCache.remove(chunkToDelete.getChunkId());

            if (!Configuration.getSettingBoolean("DISABLE_SAVING"))
                chunkToDelete.writeChunkToDisk();
        }
    }

    /**
     * @param key
     * @return
     */
    public synchronized Chunk getChunkByKey(int key) {
        return _chunkCache.get(key);
    }

    /**
     * Writes all chunks to disk.
     */
    public synchronized void writeAllChunksToDisk() {
        for (Chunk c : _chunkCache.values()) {
            c.writeChunkToDisk();
        }
    }

    /**
     * @return
     */
    public synchronized int size() {
        return _chunkCache.size();
    }

    /**
     * @return
     */
    static int capacity() {
        return (Configuration.getSettingNumeric("V_DIST_X").intValue() * Configuration.getSettingNumeric("V_DIST_Z").intValue()) + 512;
    }
}
