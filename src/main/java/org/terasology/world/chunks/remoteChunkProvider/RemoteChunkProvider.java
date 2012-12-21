package org.terasology.world.chunks.remoteChunkProvider;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.EntityRef;
import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.ChunkRegionListener;

import java.util.Map;

/**
 * @author Immortius
 */
// TODO: Have the remote chunk provider generate lighting on received chunks (so it doesn't have to be generated)
public class RemoteChunkProvider implements ChunkProvider {

    private Map<Vector3i, Chunk> chunkCache = Maps.newHashMap();

    public void receiveChunk(Chunk chunk) {
        chunkCache.put(chunk.getPos(), chunk);
    }

    public void invalidateChunks(Vector3i pos) {
        chunkCache.remove(pos);
    }

    @Override
    public void update() {

    }

    @Override
    public boolean isChunkAvailable(Vector3i pos) {
        return chunkCache.containsKey(pos);
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        return chunkCache.get(new Vector3i(x, y, z));
    }

    @Override
    public Chunk getChunk(Vector3i chunkPos) {
        return chunkCache.get(chunkPos);
    }

    @Override
    public float size() {
        return chunkCache.size();
    }

    @Override
    public void dispose() {

    }

    @Override
    public void setWorldEntity(EntityRef entity) {

    }

    @Override
    public void addRegionEntity(EntityRef entity, int distance) {
    }

    @Override
    public void addRegionEntity(EntityRef entity, int distance, ChunkRegionListener listener) {
    }

    @Override
    public void removeRegionEntity(EntityRef entity) {

    }
}
