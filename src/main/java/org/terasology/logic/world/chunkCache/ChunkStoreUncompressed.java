package org.terasology.logic.world.chunkCache;


import com.google.common.collect.Maps;
import org.terasology.logic.world.Chunk;
import org.terasology.logic.world.ChunkStore;
import org.terasology.math.Vector3i;

import java.util.concurrent.ConcurrentMap;

public class ChunkStoreUncompressed implements ChunkStore {
    ConcurrentMap<Vector3i, Chunk> map = Maps.newConcurrentMap();
    int _sizeInByte = 0;

    public ChunkStoreUncompressed() {

    }

    public Chunk get(Vector3i id) {
        return map.get(id);
    }

    public void put(Chunk c) {
        map.put(c.getPos(), c);
    }

    public float size() {
        return 0;
    }

    public void dispose() {
    }
}
