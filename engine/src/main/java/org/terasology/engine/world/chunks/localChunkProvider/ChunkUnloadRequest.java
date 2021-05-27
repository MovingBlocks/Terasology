// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks.localChunkProvider;

import org.terasology.engine.utilities.concurrency.Task;
import org.terasology.engine.world.chunks.Chunk;

public class ChunkUnloadRequest implements Task {

    private Chunk chunk;
    private LocalChunkProvider chunkProvider;
    private boolean shutdown;

    public ChunkUnloadRequest(Chunk chunk, LocalChunkProvider localChunkProvider) {
        this.chunk = chunk;
        this.chunkProvider = localChunkProvider;
    }

    public ChunkUnloadRequest() {
        shutdown = true;
    }

    @Override
    public String getName() {
        return "Unload chunk";
    }

    @Override
    public void run() {
        if (!shutdown) {
            chunkProvider.gatherBlockPositionsForDeactivate(chunk);
        }
    }

    @Override
    public boolean isTerminateSignal() {
        return shutdown;
    }
}
