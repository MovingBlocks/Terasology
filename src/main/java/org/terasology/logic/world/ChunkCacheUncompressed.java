package org.terasology.logic.world;


import java.util.concurrent.ConcurrentHashMap;

public class ChunkCacheUncompressed implements IChunkCache {
    ConcurrentHashMap<Integer, Chunk> _map = new ConcurrentHashMap<Integer, Chunk>();
    int _sizeInByte = 0;

    public ChunkCacheUncompressed(){

    }

    public Chunk get(int id) {
        return _map.get(id);
    }

    public void put(Chunk c) {
        _map.put(c.getId(), c);
    }

    public float size() {
        return 0;
    }

    public void dispose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
