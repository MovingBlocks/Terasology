// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.tasks;

import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.pipeline.ChunkTask;
import org.terasology.world.chunks.pipeline.ChunkTaskListener;

public class ChunkTaskListenerWrapper implements ChunkTask {
    private final ChunkTask chunkTask;
    private final ChunkTaskListener listener;

    public ChunkTaskListenerWrapper(ChunkTask chunkTask, ChunkTaskListener listener) {
        this.chunkTask = chunkTask;
        this.listener = listener;
    }

    @Override
    public Chunk getChunk() {
        return chunkTask.getChunk();
    }

    @Override
    public String getName() {
        return chunkTask.getName();
    }

    @Override
    public void run() {
        chunkTask.run();
        listener.onDone(chunkTask);
    }

    @Override
    public boolean isTerminateSignal() {
        return chunkTask.isTerminateSignal();
    }
}
