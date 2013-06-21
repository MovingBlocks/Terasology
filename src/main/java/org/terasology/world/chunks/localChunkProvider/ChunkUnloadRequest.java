package org.terasology.world.chunks.localChunkProvider;

import org.terasology.utilities.concurrency.Task;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkStore;

/**
 * @author Immortius
 */
public class ChunkUnloadRequest implements Task {

    private Chunk chunk;
    private LocalChunkProvider chunkProvider;
    private boolean shutdown = false;

    public ChunkUnloadRequest(Chunk chunk, LocalChunkProvider localChunkProvider) {
        this.chunk = chunk;
        this.chunkProvider = localChunkProvider;
    }

    public ChunkUnloadRequest() {
        shutdown = true;
    }

    @Override
    public void enact() {
        if (!shutdown) {
            chunkProvider.gatherBlockPositionsForDeactivate(chunk);
        }
    }

    @Override
    public boolean isTerminateSignal() {
        return shutdown;
    }
}
