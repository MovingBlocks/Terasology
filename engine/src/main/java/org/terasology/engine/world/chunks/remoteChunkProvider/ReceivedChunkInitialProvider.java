// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.remoteChunkProvider;

import org.joml.Vector3ic;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.pipeline.InitialChunkProvider;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class ReceivedChunkInitialProvider implements InitialChunkProvider {
    private BlockingQueue<Chunk> chunkQueue;

    public ReceivedChunkInitialProvider(Comparator<Chunk> comparator) {
        chunkQueue = new PriorityBlockingQueue<>(800, comparator);
    }

    public void submit(Chunk chunk) {
        chunkQueue.add(chunk);
    }

    @Override
    public boolean hasNext() {
        return !chunkQueue.isEmpty();
    }

    @Override
    public synchronized Chunk next(Set<Vector3ic> currentlyGenerating) {
        return chunkQueue.poll();
    }
}
