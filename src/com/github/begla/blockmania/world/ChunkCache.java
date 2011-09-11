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
import javolution.util.FastMap;

import java.util.Collections;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkCache {

    private final FastMap<Integer, Chunk> _chunkCache = new FastMap<Integer, Chunk>(capacity());
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
    public Chunk loadOrCreateChunk(int x, int z) {
        // Try to load the chunk from the cache
        Chunk c;

        synchronized (this) {
            c = _chunkCache.get(Integer.valueOf(MathHelper.cantorize(x, z)));
        }

        // We got a chunk! Already! Great!
        if (c != null) {
            return c;
        }

        // Init a new chunk
        c = _parent.prepareNewChunk(x, z);

        synchronized (this) {
            _chunkCache.put(c.getChunkId(), c);
            c.setCached(true);
        }

        return c;
    }

    public void freeCache() {
        if (_chunkCache.size() <= capacity()) {
            return;
        }

        FastList<Chunk> cachedChunks;

        synchronized (this) {
            cachedChunks = new FastList<Chunk>(_chunkCache.values());
        }
        Collections.sort(cachedChunks);

        while (_chunkCache.size() > capacity()) {
            Chunk chunkToDelete = cachedChunks.removeLast();

            synchronized (this) {
                _chunkCache.remove(chunkToDelete.getChunkId());
                chunkToDelete.setCached(false);
            }

            chunkToDelete.writeChunkToDisk();
            chunkToDelete.disposeChunk();
        }
    }

    /**
     * @param key
     * @return
     */
    public Chunk getChunkByKey(int key) {
        Chunk result;

        synchronized (this) {
            result = _chunkCache.get(Integer.valueOf(key));
        }

        return result;
    }

    /**
     * Writes all chunks to disk.
     */
    public void writeAllChunksToDisk() {
        synchronized (this) {
            for (Chunk c : _chunkCache.values()) {
                c.writeChunkToDisk();
            }
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
    public static int capacity() {
        return (Configuration.getSettingNumeric("V_DIST_X").intValue() * Configuration.getSettingNumeric("V_DIST_Z").intValue()) + 1024;
    }
}
