// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.stages;

import org.joml.Vector3i;
import org.terasology.monitoring.ThreadActivity;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.world.chunks.Chunk;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Provides factory methods for functional-style creating {@link ProcessingStage}.
 */
public class FunctionalStage implements BiFunction<Executor, CompletableFuture<Chunk>, CompletableFuture<Chunk>>,
        ProcessingStage {
    private final BiFunction<Executor, CompletableFuture<Chunk>, CompletableFuture<Chunk>> futureBiFunction;

    FunctionalStage(BiFunction<Executor, CompletableFuture<Chunk>, CompletableFuture<Chunk>> futureUnaryOperator) {
        this.futureBiFunction = futureUnaryOperator;
    }

    /**
     * Create {@link ProcessingStage} for {@link org.terasology.world.chunks.pipeline.ChunkProcessingPipeline} with
     * peek-style function.
     *
     * @param name thread activity name - displays in {@link ThreadMonitor}
     * @param consumer function which takes Chunk and processed it.
     * @return ProcessingStage for {@link org.terasology.world.chunks.pipeline.ChunkProcessingPipeline}
     */
    public static ProcessingStage create(String name, Consumer<Chunk> consumer) {
        return create(name, chunk -> {
            consumer.accept(chunk);
            return chunk;
        });
    }

    /**
     * Create {@link ProcessingStage} for {@link org.terasology.world.chunks.pipeline.ChunkProcessingPipeline} with
     * function.
     *
     * @param name thread activity name - displays in {@link ThreadMonitor}
     * @param function takes Chunk and processed it, and can return another chunk.
     * @return ProcessingStage for {@link org.terasology.world.chunks.pipeline.ChunkProcessingPipeline}
     */
    public static ProcessingStage create(String name, UnaryOperator<Chunk> function) {
        UnaryOperator<Chunk> wrappedWithThreadMonitor = chunk -> {
            ThreadActivity activity = ThreadMonitor.startThreadActivity(name);
            Chunk returnedChunk = function.apply(chunk);
            activity.close();
            return returnedChunk;
        };
        return create(((executor, completableFuture) -> completableFuture.thenApplyAsync(wrappedWithThreadMonitor,
                executor)));
    }

    /**
     * Create {@link ProcessingStage} for {@link org.terasology.world.chunks.pipeline.ChunkProcessingPipeline} with
     * custom CompletableFuture creation.
     *
     * @param completableFutureProcessor function which takes {@link Executor}(for {@link CompletableFuture}'s
     *         *Async Methods) and previous stage's {@link CompletableFuture} from {@link
     *         org.terasology.world.chunks.pipeline.ChunkProcessingPipeline#invokeGeneratorTask(Vector3i, Supplier)}
     * @return ProcessingStage for {@link org.terasology.world.chunks.pipeline.ChunkProcessingPipeline}
     */
    public static ProcessingStage create(BiFunction<Executor, CompletableFuture<Chunk>, CompletableFuture<Chunk>> completableFutureProcessor) {
        return new FunctionalStage(completableFutureProcessor);
    }

    /**
     * {@inheritDoc}
     *
     * @param executor
     * @param completableFuture
     * @return
     */
    @Override
    public CompletableFuture<Chunk> apply(Executor executor, CompletableFuture<Chunk> completableFuture) {
        return futureBiFunction.apply(executor, completableFuture);
    }
}
