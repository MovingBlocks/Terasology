// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.tasks;

import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.pipeline.AbstractChunkTask;
import org.terasology.world.chunks.pipeline.ChunkTask;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Free-form Chunk task for external handling.
 * <p>
 * Like notify or take chunk outside of pipeline.
 * <p>
 * Use as {@link java.util.stream.Stream#peek(Consumer)} or {@link java.util.stream.Stream#forEach(Consumer)} if it at
 * the end of pipeline.
 */
public class NotifyChunkTask extends AbstractChunkTask {

    private final String notifierName;
    private final ChunkPipelineTaskListener chunkPipelineListener;

    public NotifyChunkTask(Chunk chunk, String notifierName, ChunkPipelineTaskListener chunkPipelineListener) {
        super(chunk);
        this.notifierName = notifierName;
        this.chunkPipelineListener = chunkPipelineListener;
    }

    public static Function<Chunk, ChunkTask> stage(String notifierName, ChunkPipelineTaskListener listener) {
        return (chunk) -> new NotifyChunkTask(chunk, notifierName, listener);
    }

    @Override
    public String getName() {
        return notifierName;
    }

    @Override
    public void run() {
        chunkPipelineListener.fire(chunk);
    }
}
