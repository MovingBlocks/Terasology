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

    private final FastMap<Integer, Chunk> _chunkCache = new FastMap<Integer, Chunk>(capacity()).shared();
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
        // Try to load the chunk from the cache


        int chunkId = Chunk.idForPosition(new Vector3f(x, 0, z));
        Chunk c = _chunkCache.get(chunkId);

        // We got a chunk! Already! Great!
        if (c != null) {
            return c;
        }

        // Init a new chunk
        c = loadChunkFromDisk(chunkId);

        if (c == null) {
            c = new Chunk(_parent, new Vector3f(x, 0, z));
        }

        _chunkCache.put(c.getChunkId(), c);
        c.setCached(true);

        return c;
    }

    public void freeCacheSpace() {
        if (_chunkCache.size() <= capacity()) {
            return;
        }

        FastList<Chunk> cachedChunks = new FastList<Chunk>(_chunkCache.values());
        Collections.sort(cachedChunks);

        while (_chunkCache.size() > capacity()) {
            Chunk chunkToDelete = cachedChunks.removeLast();
            _chunkCache.remove(chunkToDelete.getChunkId());

            chunkToDelete.setCached(false);
            writeChunkToDisk(chunkToDelete);

            chunkToDelete.freeBuffers();
        }
    }

    /**
     * Writes all chunks to disk and disposes them.
     */
    public void saveAndDisposeAllChunks() {
        for (FastMap.Entry<Integer, Chunk> e = _chunkCache.head(), end = _chunkCache.tail(); (e = e.getNext()) != end; ) {
            Chunk c = e.getValue();

            c.setCached(false);
            writeChunkToDisk(c);

            c.freeBuffers();
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
        return (Configuration.getSettingNumeric("V_DIST_X").intValue() * Configuration.getSettingNumeric("V_DIST_Z").intValue()) + 1024;
    }

    private void writeChunkToDisk(Chunk c) {
        if (Blockmania.getInstance().isSandboxed())
            return;

        if (c.isFresh()) {
            return;
        }

        File dir = new File(_parent.getWorldSavePath());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Blockmania.getInstance().getLogger().log(Level.SEVERE, "Could not create save directory.");
                return;
            }
        }

        File f = new File((String.format("%s/%d.bc", _parent.getWorldSavePath(), c.getChunkId())));

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

    private Chunk loadChunkFromDisk(int id) {
        File f = new File((String.format("%s/%d.bc", _parent.getWorldSavePath(), id)));

        if (!f.exists())
            return null;

        try {
            FileInputStream fileIn = new FileInputStream(f);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            Chunk result = (Chunk) in.readObject();
            result.setPosition(new Vector3f(Chunk.posXForId(id), 0, Chunk.posZForId(id)));
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
