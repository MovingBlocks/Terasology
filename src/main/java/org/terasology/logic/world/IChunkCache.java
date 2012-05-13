package org.terasology.logic.world;

import org.terasology.math.Vector3i;

public interface IChunkCache {
    public Chunk get(Vector3i id);
    public void put(Chunk c);
    public float size();
    void dispose();
}
