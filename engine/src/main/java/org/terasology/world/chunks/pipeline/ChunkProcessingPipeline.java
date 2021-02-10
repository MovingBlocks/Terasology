// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.monitoring.ThreadActivity;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.rendering.world.viewDistance.ViewDistance;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.chunks.Chunk;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Manages execution of chunk processing.
 * <p>
 * {@link Chunk}s will processing on stages {@link ChunkProcessingPipeline#addStage}
 */
public class ChunkProcessingPipeline {

    private static final int NUM_TASK_THREADS = 4;
    private static final Logger logger = LoggerFactory.getLogger(ChunkProcessingPipeline.class);

    private final List<UnaryOperator<Chunk>> stages = Lists.newArrayList();
    private final ThreadPoolExecutor executor;
    private final Map<Vector3ic, CompletableFuture<Chunk>> processingPositions = Maps.newConcurrentMap();
    private int threadIndex;

    /**
     * Create ChunkProcessingPipeline.
     */
    public ChunkProcessingPipeline() {
        int chunksCount = new BlockRegion(0, 0, 0).expand(ViewDistance.MEGA.getChunkDistance()).volume();
        executor = new ThreadPoolExecutor(
                NUM_TASK_THREADS,
                NUM_TASK_THREADS, 0L,
                TimeUnit.MILLISECONDS,
                Queues.newArrayBlockingQueue(chunksCount),
                this::threadFactory,
                this::rejectQueueHandler);
    }

    private static UnaryOperator<Chunk> wrapWithLogging(String name, UnaryOperator<Chunk> stage) {
        return chunk -> {
            try (ThreadActivity ignored = ThreadMonitor.startThreadActivity(name)) {
                return stage.apply(chunk);
            }
        };
    }

    private static UnaryOperator<Chunk> convertToFunction(Consumer<Chunk> stage) {
        return chunk -> {
            stage.accept(chunk);
            return chunk;
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
     * @param name stage name for display in thread manager.
     * @param stage function for processing chunk at this stage.
     * @return self for Fluent api.
     */
    public ChunkProcessingPipeline addStage(String name, UnaryOperator<Chunk> stage) {
        stages.add(wrapWithLogging(name, stage));
        return this;
    }

    /**
     * Add stage to pipeline.
     *
     * @param name stage name for display in thread manager.
     * @param peekStage function for processing chunk at this stage.
     * @return self for Fluent api.
     */
    public ChunkProcessingPipeline addStage(String name, Consumer<Chunk> peekStage) {
        stages.add(wrapWithLogging(name, convertToFunction(peekStage)));
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
    public Future<Chunk> invokeGeneratorTask(Vector3ic position, Supplier<Chunk> generatorTask) {
        CompletableFuture<Chunk> future = processingPositions.get(position);
        if (future == null) {
            future = CompletableFuture.supplyAsync(generatorTask, executor);
            for (UnaryOperator<Chunk> stage : stages) {
                future = future.thenApplyAsync(stage, executor);
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
        return invokeGeneratorTask(chunk.getPosition(), () -> chunk);
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
}
