// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.pipeline.stages.ChunkTaskProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Manages execution of chunk processing.
 * <p>
 * {@link Chunk}s will processing on stages {@link ChunkProcessingPipeline#addStage}
 */
public class ChunkProcessingPipeline {

    private static final int NUM_TASK_THREADS = 4;
    private static final Logger logger = LoggerFactory.getLogger(ChunkProcessingPipeline.class);

    private final List<ChunkTaskProvider> stages = Lists.newArrayList();
    private final ThreadPoolExecutor executor;
    private final Map<Vector3ic, CompletableFuture<Chunk>> processingPositions = Maps.newConcurrentMap();
    private int threadIndex;

    /**
     * Create ChunkProcessingPipeline.
     */
    public ChunkProcessingPipeline() {
        executor = new ThreadPoolExecutor(
                NUM_TASK_THREADS,
                NUM_TASK_THREADS, 0L,
                TimeUnit.MILLISECONDS,
                Queues.newArrayBlockingQueue(1024),
                this::threadFactory,
                this::rejectQueueHandler) {
            @Override
            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
                RunnableFuture<T> newTaskFor = super.newTaskFor(callable);
                return new PositionFuture<>(newTaskFor, ((PositionalCallable) callable).getPosition());
            }
        };
    }

    private Thread threadFactory(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName("Chunk-Processing-" + threadIndex++);
        return thread;
    }

    private void rejectQueueHandler(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
        logger.error("Cannot run {}  because queue is full", runnable);
    }

    /**
     * Add stage to pipeline.
     *
     * @param stage function for ChunkTask generating by Chunk.
     * @return self for Fluent api.
     */
    public ChunkProcessingPipeline addStage(ChunkTaskProvider stage) {
        stages.add(stage);
        return this;
    }

    /**
     * Run generator task and then run pipeline processing with it.
     * <p>
     * Additionally add technical stages for cleaning pipeline after chunk processing and handles errors in stages.
     *
     * @param generatorTask ChunkTask which provides new chunk to pipeline
     * @return Future of chunk processing.
     */
    public Future<Chunk> invokeGeneratorTask(Vector3i position, Supplier<Chunk> generatorTask) {
        CompletableFuture<Chunk> future = processingPositions.get(position);
        if (future == null) {
            future = CompletableFuture.supplyAsync(generatorTask, executor);
            for (ChunkTaskProvider stage : stages) {
                future = future.thenApplyAsync(stage.createChunkTask(position), executor);
            }
            processingPositions.put(position, future);
            future.thenAcceptAsync(chunk -> processingPositions.remove(chunk.getPosition()), executor);
        }

        return future;
    }

    /**
     * Send chunk to processing pipeline. If chunk not processing yet then pipeline will be setted. If chunk processed
     * then chunk will be processing in next stage;
     *
     * @param chunk chunk to process.
     */
    public Future<Chunk> invokePipeline(Chunk chunk) {
        return invokeGeneratorTask(chunk.getPosition(new Vector3i()), () -> chunk);
    }

    public void shutdown() {
        processingPositions.clear();
        executor.shutdown();
        executor.getQueue().clear();
    }

    public void restart() {
        processingPositions.clear();
        executor.getQueue().clear();
    }

    /**
     * Stop processing chunk at position.
     *
     * @param pos position of chunk to stop processing.
     */
    public void stopProcessingAt(Vector3ic pos) {
        Future<Chunk> future = processingPositions.remove(pos);
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * Check is position processing.
     *
     * @param pos position for check
     * @return true if position processing, false otherwise
     */
    public boolean isPositionProcessing(Vector3ic pos) {
        return processingPositions.containsKey(pos);
    }

    /**
     * Get processing positions.
     *
     * @return copy of processing positions
     */
    public List<Vector3ic> getProcessingPosition() {
        return new LinkedList<>(processingPositions.keySet());
    }

    /**
     * Dummy callable for passthru position for {@link java.util.concurrent.ThreadPoolExecutor}#newTaskFor
     */
    private static final class PositionalCallable implements Callable<Chunk> {
        private final Callable<Chunk> callable;
        private final Vector3ic position;

        private PositionalCallable(Callable<Chunk> callable, Vector3ic position) {
            this.callable = callable;
            this.position = position;
        }

        public Vector3ic getPosition() {
            return position;
        }

        @Override
        public Chunk call() throws Exception {
            return callable.call();
        }
    }
}
