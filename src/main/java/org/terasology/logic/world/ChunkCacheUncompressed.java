package org.terasology.logic.world;


import org.terasology.math.Vector3i;

import java.util.concurrent.ConcurrentHashMap;

public class ChunkCacheUncompressed implements IChunkCache {
    ConcurrentHashMap<Vector3i, Chunk> _map = new ConcurrentHashMap<Vector3i, Chunk>();
    int _sizeInByte = 0;

    public ChunkCacheUncompressed(){

    }

    public Chunk get(Vector3i id) {
        return _map.get(id);
    }

    public void put(Chunk c) {
        _map.put(c.getPos(), c);
    }

    public float size() {
        return 0;
    }

    public void dispose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
