// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import com.google.common.collect.Sets;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.world.chunks.Chunk;

import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Manages execution of chunk tasks on a queue.
 * <p>
 * {@link ChunkTask}s are executing in background threads.
 * <p>
 * {@link ChunkTask}s are executing by priority via {@link Comparable}.
 * <p>
 * {@link Chunk}s will processing on stages {@link ChunkProcessingPipeline#addStage}
 */
public class ChunkProcessingPipeline implements ChunkTaskListener {
    private static final int NUM_TASK_THREADS = 8;
    private static final Logger logger = LoggerFactory.getLogger(ChunkProcessingPipeline.class);

    private final ExecutorService chunkProcessor;
    private final List<Function<Chunk, Chunk>> stages = new LinkedList<>();
    private final List<ChunkTaskListener> chunkTaskListeners = new LinkedList<>();
    private final List<ChunkRemoveFromPipelineListener> chunkRemoveFromPipelineListeners = new LinkedList<>();
    private final Map<Chunk, Deque<Function<Chunk, ChunkTask>>> chunkNextStages = new ConcurrentHashMap<>();
    private final Set<org.joml.Vector3i> processingPositions = Sets.newConcurrentHashSet();
    private final Set<org.joml.Vector3i> invalidatedPositions = Sets.newConcurrentHashSet();

    /**
     * Create ChunkProcessingPipeline.
     *
     * @param taskComparator using by TaskMaster for priority ordering task.
     */
    public ChunkProcessingPipeline(Comparator<Runnable> taskComparator) {
        chunkProcessor = new ThreadPoolExecutor(
                NUM_TASK_THREADS, NUM_TASK_THREADS,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                Executors.defaultThreadFactory(), ChunkProcessingPipeline::onRejectExecution);
    }

    private static void onRejectExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
        logger.error("Cannot execute task: " + runnable);
    }

    /**
     * An method that creates new threads on demand. {@link java.util.concurrent.ThreadFactory}
     *
     * @param firstRunnable a runnable to be executed by new thread instance
     * @return constructed thread, or {@code null} if the request to create a thread is rejected
     */
    private static Thread threadFactory(Runnable firstRunnable) {
        Thread thread = new Thread(firstRunnable, "Chunk-Processing");
        thread.setDaemon(true);
        return thread;
    }

    /**
     * Add stage to pipeline. If stage instance of {@link ChunkTaskListener} - it's will be register as listener. If
     * stage instance of {@link ChunkRemoveFromPipelineListener} - it's will be register as listener.
     *
     * @param stage function for ChunkTask generating by Chunk.
     * @return self for Fluent api.
     */
    public ChunkProcessingPipeline addStage(Function<Chunk, Chunk> stage) {
        stages.add(stage);
        return this;
    }

    public ChunkProcessingPipeline addStage(Consumer<Chunk> stage) {
        addStage((c) -> {
            stage.accept(c);
            return c;
        });
        return this;
    }

    /**
     * Register chunk task listener.
     *
     * @param listener listener.
     * @return self for Fluent api.
     */
    public ChunkProcessingPipeline addListener(ChunkTaskListener listener) {
        chunkTaskListeners.add(listener);
        return this;
    }

    /**
     * Register chunk task listener.
     *
     * @param listener listener.
     * @return self for Fluent api.
     */
    public ChunkProcessingPipeline addListener(ChunkRemoveFromPipelineListener listener) {
        chunkRemoveFromPipelineListeners.add(listener);
        return this;
    }

    /**
     * Run generator task and then run pipeline processing with it.
     *
     * @param generatorTask ChunkTask which provides new chunk to pipeline
     */
    public Future<Chunk> invokeGeneratorTask(Vector3i position, Supplier<Chunk> generatorTask) {
        processingPositions.add(position);
        CompletableFuture<Chunk> completableFuture = CompletableFuture.supplyAsync(generatorTask, chunkProcessor);

        for (Function<Chunk, Chunk> stage : stages) {
            completableFuture = completableFuture.thenApplyAsync(stage, chunkProcessor);
        }
        completableFuture.thenApplyAsync((c) -> {
            processingPositions.remove(c.getPosition(new Vector3i()));
            return c;
        }, chunkProcessor);
        return completableFuture;
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
        chunkNextStages.clear();
        processingPositions.clear();
        chunkProcessor.shutdown();
    }

    public void restart() {
        chunkNextStages.clear();
        processingPositions.clear();
    }

    /**
     * {@inheritDoc}
     *
     * @param chunkTask ChunkTask which done processing.
     */
    @Override
    public void onDone(ChunkTask chunkTask) {
        chunkTaskListeners.forEach((listener) -> listener.onDone(chunkTask));
        logger.debug("Task " + chunkTask + " done");
        invokePipeline(chunkTask.getChunk());
    }

    /**
     * Stop processing chunk at position.
     *
     * @param pos position of chunk to stop processing.
     */
    public void stopProcessingAt(Vector3i pos) {
        invalidatedPositions.add(pos);
        processingPositions.remove(pos);
        chunkRemoveFromPipelineListeners.forEach(l -> l.onRemove(pos));
    }

    /**
     * Check is position processing.
     *
     * @param pos position for check
     * @return true if position processing, false otherwise
     */
    public boolean isPositionProcessing(org.joml.Vector3i pos) {
        return processingPositions.contains(pos);
    }

    /**
     * Get processing positions.
     *
     * @return copy of processing positions
     */
    public List<org.joml.Vector3i> getProcessingPosition() {
        return new LinkedList<>(processingPositions);
    }
}
