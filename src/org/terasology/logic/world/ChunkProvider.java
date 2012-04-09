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
import org.terasology.logic.manager.Config;
import org.terasology.math.Vector3i;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Provides chunks from cache if possible (in memory or hdd).
 * If requested chunk is not cached it will be generated on the fly.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkProvider implements IChunkProvider {
    private static final boolean SAVE_CHUNKS = Config.getInstance().isSaveChunks();
    private static final int CACHE_SIZE = Config.getInstance().getChunkCacheSize();

    private static boolean _running = false;

    private final ConcurrentHashMap<Integer, Chunk> _nearChunkCache = new ConcurrentHashMap<Integer, Chunk>();
    private IChunkCache _farChunkCache;
    private final LocalWorldProvider _parent;

    public ChunkProvider(LocalWorldProvider parent) {
        _parent = parent;
        initFarChunkCache();
    }

    public void initFarChunkCache(){
        File f = new File(_parent.getObjectSavePath() + "/" + _parent.getTitle());
        if (!f.exists()) {
            _farChunkCache = new ChunkCacheGZip();
            return;
        }
        try {
            FileInputStream fileIn = new FileInputStream(f);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            _farChunkCache = (ChunkCacheGZip) in.readObject();
            in.close();
            fileIn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isChunkAvailable(int x, int y, int z) {
        int id = new Vector3i(x,y,z).hashCode();
        Chunk c = _nearChunkCache.get(id);
        return c != null;
    }

    public Chunk getChunk(int x, int y, int z) {
        int id = new Vector3i(x,y,z).hashCode();
        Chunk c = _nearChunkCache.get(id);
        if (c != null) {
            return c;
        }
        c = _farChunkCache.get(id);
        if (c == null) {
            c = new Chunk(_parent, x, y, z);
        }

        _nearChunkCache.put(id, c);
        //HACK! move local world provider out of chunk!
        c.setParent(_parent);
        return c;
    }

    public void flushCache() {
        if (_running || _nearChunkCache.size() <= CACHE_SIZE)
            return;

        _running = true;

        Runnable r = new Runnable() {
            public void run() {
                ArrayList<Chunk> cachedChunks = new ArrayList<Chunk>(_nearChunkCache.values());
                Collections.sort(cachedChunks);

                if (cachedChunks.size() > CACHE_SIZE) {
                    Chunk chunkToDelete = cachedChunks.remove(cachedChunks.size() - 1);
                    // Write the chunk to disk (but do not remove it from the cache just jet)
                    _farChunkCache.put(chunkToDelete);
                    // When the chunk is written, finally remove it from the cache
                    _nearChunkCache.values().remove(chunkToDelete);

                    chunkToDelete.dispose();
                }

                _running = false;
            }
        };

        Terasology.getInstance().submitTask("Flush Chunk Cache", r);
    }

    public void dispose() {
        Runnable r = new Runnable() {
            public void run() {

                for (Chunk c : _nearChunkCache.values()) {
                    if(SAVE_CHUNKS){
                        _farChunkCache.put(c);
                    }
                    c.dispose();
                }
                _nearChunkCache.clear();
                File dirPath = new File(_parent.getObjectSavePath());
                if (!dirPath.exists()) {
                    if (!dirPath.mkdirs()) {
                        Terasology.getInstance().getLogger().log(Level.SEVERE, "Could not create save directory.");
                        return;
                    }
                }

                File f = new File(_parent.getObjectSavePath() + '/' + _parent.getTitle());

                try {
                    FileOutputStream fileOut = new FileOutputStream(f);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(_farChunkCache);
                    out.close();
                    fileOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Terasology.getInstance().submitTask("Dispose Chunk", r);
    }

    public float size() {
        return _farChunkCache.size();
    }
}
