// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.remoteChunkProvider;

import org.joml.Vector3ic;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.pipeline.InitialChunkProvider;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Passes chunks received from the server through into the `ChunkProcessingPipeline`.
 */
public class ReceivedInitialChunkProvider implements InitialChunkProvider {
    private final BlockingQueue<Chunk> chunkQueue;

    public ReceivedInitialChunkProvider(Comparator<Chunk> comparator) {
        chunkQueue = new PriorityBlockingQueue<>(800, comparator);
    }

    public void submit(Chunk chunk) {
        chunkQueue.add(chunk);
    }

    @Override
    public Optional<Chunk> next(Set<Vector3ic> currentlyGenerating) {
        // The queue handles synchronization
        return Optional.ofNullable(chunkQueue.poll());
    }
}
