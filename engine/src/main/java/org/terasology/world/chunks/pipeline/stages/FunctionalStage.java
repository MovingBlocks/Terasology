// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.stages;

import org.terasology.monitoring.ThreadActivity;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.world.chunks.Chunk;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class FunctionalStage implements BiFunction<Executor, CompletableFuture<Chunk>, CompletableFuture<Chunk>>,
        ProcessingStage {
    private final BiFunction<Executor, CompletableFuture<Chunk>, CompletableFuture<Chunk>> futureBiFunction;

    public FunctionalStage(BiFunction<Executor, CompletableFuture<Chunk>, CompletableFuture<Chunk>> futureUnaryOperator) {
        this.futureBiFunction = futureUnaryOperator;
    }

    public static FunctionalStage create(String name, Consumer<Chunk> processing) {
        return create(name, chunk -> {
            processing.accept(chunk);
            return chunk;
        });
    }

    public static FunctionalStage create(String name, UnaryOperator<Chunk> processing) {
        UnaryOperator<Chunk> wrappedWithThreadMonitor = chunk -> {
            ThreadActivity activity = ThreadMonitor.startThreadActivity(name);
            Chunk returnedChunk = processing.apply(chunk);
            activity.close();
            return returnedChunk;
        };
        return create(((executor, completableFuture) -> completableFuture.thenApplyAsync(wrappedWithThreadMonitor,
                executor)));
    }

    public static FunctionalStage create(BiFunction<Executor, CompletableFuture<Chunk>, CompletableFuture<Chunk>> futureProcessing) {
        return new FunctionalStage(futureProcessing);
    }

    @Override
    public CompletableFuture<Chunk> apply(Executor executor, CompletableFuture<Chunk> completableFuture) {
        return futureBiFunction.apply(executor, completableFuture);
    }
}
