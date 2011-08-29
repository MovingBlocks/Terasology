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
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkCache {

    private final TreeMap<Integer, Chunk> _chunkCache = new TreeMap<Integer, Chunk>();
    private final FastList<Chunk> _chunkCacheList = new FastList<Chunk>(2048);

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

        // Delete the oldest element if the cache size is exceeded
        if (_chunkCacheList.size() > capacity()) {
            Collections.sort(_chunkCacheList);
            Chunk chunkToDeltete = _chunkCacheList.removeLast();

            if (chunkToDeltete != null) {
                // Save the chunk before removing it from the cache
                _chunkCache.remove(chunkToDeltete.getChunkId());
                chunkToDeltete.writeChunkToDisk();
            }
        }

        // Init a new chunk
        c = _parent.prepareNewChunk(x, z);

        _chunkCache.put(c.getChunkId(), c);
        _chunkCacheList.add(c);

        return c;
    }

    /**
     * Returns true if the given chunk is present in the cache.
     *
     * @param c The chunk
     * @return True if the chunk is present in the chunk cache
     */
    public boolean isChunkCached(Chunk c) {
        return loadChunk((int) c.getPosition().x, (int) c.getPosition().z) != null;
    }

    /**
     * Tries to load a chunk from the cache. Returns null if no
     * chunk is found.
     *
     * @param x X-coordinate
     * @param z Z-coordinate
     * @return The loaded chunk
     */
    public Chunk loadChunk(int x, int z) {
        return _chunkCache.get(MathHelper.cantorize(x, z));
    }

    /**
     * @param key
     * @return
     */
    public Chunk getChunkByKey(int key) {
        return _chunkCache.get(key);
    }

    /**
     * Writes all chunks to disk.
     */
    public void writeAllChunksToDisk() {
        _parent.suspendUpdateThread();
        /*
         * Wait until the update thread is suspended.
         */
        while (_parent.isUpdateThreadRunning()) {
            // Do nothing
        }
        for (Chunk c : _chunkCache.values()) {
            c.writeChunkToDisk();
        }
        _parent.resumeUpdateThread();
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
    int capacity() {
        return (Configuration.getSettingNumeric("V_DIST_X").intValue() * Configuration.getSettingNumeric("V_DIST_Z").intValue()) * 2;
    }
}
