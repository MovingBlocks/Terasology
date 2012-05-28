package org.terasology.logic.newWorld.chunkCache;


import com.google.common.collect.Maps;
import org.terasology.logic.newWorld.NewChunk;
import org.terasology.logic.newWorld.NewChunkCache;
import org.terasology.math.Vector3i;

import java.util.concurrent.ConcurrentMap;

public class ChunkCacheUncompressed implements NewChunkCache {
    ConcurrentMap<Vector3i, NewChunk> map = Maps.newConcurrentMap();
    int _sizeInByte = 0;

    public ChunkCacheUncompressed() {

    }

    public NewChunk get(Vector3i id) {
        return map.get(id);
    }

    public void put(NewChunk c) {
        map.put(c.getPos(), c);
    }

    public float size() {
        return 0;
    }

    public void dispose() {
    }
}
