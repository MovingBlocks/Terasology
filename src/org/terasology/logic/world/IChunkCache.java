package org.terasology.logic.world;

public interface IChunkCache {
    public Chunk get(int id);
    public void put(Chunk c);
    public float size();
    void dispose();
}
