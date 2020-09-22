// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.SettableFuture;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.monitoring.ThreadActivity;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.pipeline.stages.ChunkTask;
import org.terasology.world.chunks.pipeline.stages.ChunkTaskProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Manages execution of chunk processing.
 * <p>
 * {@link Chunk}s will processing on stages {@link ChunkProcessingPipeline#addStage}
 */
public class ChunkProcessingPipeline {

    private static final int NUM_TASK_THREADS = 8;
    private static final Logger logger = LoggerFactory.getLogger(ChunkProcessingPipeline.class);

    private final ExecutorCompletionService<Chunk> chunkProcessor;
    private final Function<Vector3ic, Chunk> chunkProvider;
    private final Thread reactor;
    private final List<ChunkTaskProvider> stages = Lists.newArrayList();
    private final ThreadPoolExecutor executor;
    private final Map<Vector3ic, Chunk> positions = Maps.newConcurrentMap();
    private final Map<Chunk, ChunkTaskProvider> currentStages = Maps.newConcurrentMap();
    private final Map<ChunkTask, ChunkTaskProvider> pendingChunkTasks = Maps.newConcurrentMap();
    private final Map<Vector3ic, SettableFuture<Chunk>> exitFutures = Maps.newConcurrentMap();
    private final Set<Vector3ic> processingPosition = Sets.newConcurrentHashSet();
    private int threadIndex;


    /**
     * Create ChunkProcessingPipeline.
     */
    public ChunkProcessingPipeline(Function<Vector3ic, Chunk> chunkProvider) {
        this.chunkProvider = chunkProvider;

        executor = new ThreadPoolExecutor(//TODO return Comparable.
                NUM_TASK_THREADS,
                NUM_TASK_THREADS, 0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                this::threadFactory,
                this::rejectQueueHandler);
        chunkProcessor = new ExecutorCompletionService<>(executor);
        reactor = new Thread(this::chunkTaskHandler);
        reactor.setName("Chunk-Processing-Reactor");
        reactor.start();
    }

    /**
     * Reactor thread. Handles all ChunkTask dependency logic and running.
     */
    private void chunkTaskHandler() {
        try {
            while (!executor.isTerminated()) {
                Future<Chunk> future = chunkProcessor.take();
                try {
                    Chunk chunk = future.get();
                    ChunkTaskProvider nextChunkProvider = getNextChunkTaskProvider(chunk);
                    handleNextChunkProvider(chunk, nextChunkProvider);
                    // Process pending chunk task
                    processChunkTasks();
                } catch (ExecutionException e) {
                    logger.error("ChunkTask catch error: ", e); // TODO fetch Position and stage.
                } catch (Exception e) {
                    logger.error("Exception catched:", e);
                }
            }
        } catch (InterruptedException e) {
            logger.error("Reactor thread was interrupted", e);
            reactor.interrupt();
        }
    }

    /**
     * Handle next chunk task provider.
     * <p>
     * If {@code nextChunkProvider} is non {@code null} - create {@link ChunkTask} for processing.
     * <p>
     * If {@code nextChunkProvider} is {@code null} - end processing for chunk and cleanup pipeline.
     *
     * @param chunk processing chunk
     * @param nextChunkProvider next chunk task provider.
     */
    private void handleNextChunkProvider(Chunk chunk, ChunkTaskProvider nextChunkProvider) {
        Vector3i position = chunk.getPosition(new Vector3i());
        if (nextChunkProvider != null) {
            // set new chunk stage and create next new stage's task
            currentStages.put(chunk, nextChunkProvider);
            pendingChunkTasks.put(nextChunkProvider.createChunkTask(position), nextChunkProvider);
        } else {
            // exit for chunk and clean pipeline after chunk.
            SettableFuture<Chunk> chunkSettableFuture = exitFutures.get(position);
            if (chunkSettableFuture != null) {
                chunkSettableFuture.set(chunk);
                cleanup(chunk);
            }
        }
    }

    /**
     * Get next {@link ChunkTaskProvider} for chunk.
     * <p>
     * If it is new chunk in processing (after generation) - first stage.
     * <p>
     * If chunk passing last stage - returns {@code null}
     *
     * @param chunk processing chunk
     * @return next {@link ChunkTaskProvider} if found, null otherwise.
     */
    private ChunkTaskProvider getNextChunkTaskProvider(Chunk chunk) {
        ChunkTaskProvider nextChunkProvider;
        ChunkTaskProvider chunkTaskProvider = currentStages.get(chunk);
        if (chunkTaskProvider == null) {
            // If it new chunk in processing (received from generator)
            nextChunkProvider = stages.get(0);
            positions.put(chunk.getPosition(new Vector3i()), chunk);
        } else {
            // else if this old chunk, get next stage
            int stageIndex = stages.indexOf(chunkTaskProvider);
            if (stageIndex + 1 < stages.size()) {
                nextChunkProvider = stages.get(stageIndex + 1);
            } else {
                // haven't next stage, time to exit
                nextChunkProvider = null;
            }
        }
        return nextChunkProvider;
    }

    private void processChunkTasks() {
        for (Map.Entry<ChunkTask, ChunkTaskProvider> entry : pendingChunkTasks.entrySet()) {
            ChunkTask chunkTask = entry.getKey();
            ChunkTaskProvider chunkTaskProvider = entry.getValue();
            Set<Vector3ic> requirements = chunkTask.getRequirements();

            Set<Chunk> gatheredChunks = requirements.stream()
                    .map(pos -> getChunkBy(chunkTaskProvider, pos))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (gatheredChunks.size() == requirements.size()) {

                runTask(chunkTask, gatheredChunks);
                pendingChunkTasks.remove(chunkTask, chunkTaskProvider);
            }
        }
    }

    private Chunk getChunkBy(ChunkTaskProvider requiredStage, Vector3ic position) {
        Chunk chunk = chunkProvider.apply(position);
        if (chunk == null) {
            Chunk candidate = positions.get(position);
            if (candidate == null) {
                return null;
            }
            ChunkTaskProvider candidateCurrentStage = currentStages.get(candidate);
            if (stages.indexOf(candidateCurrentStage) >= stages.indexOf(requiredStage)) {
                chunk = candidate;
            }
        }
        return chunk;
    }

    private Future<Chunk> runTask(ChunkTask task, Set<Chunk> chunks) {
        return chunkProcessor.submit(() -> {
            try (ThreadActivity ignored = ThreadMonitor.startThreadActivity(task.getName())) {
                return task.apply(chunks);
            }
        });
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
        if (processingPosition.contains(position)) {
            return exitFutures.get(position);
        }
        processingPosition.add(position);

        chunkProcessor.submit(generatorTask::get);

        SettableFuture<Chunk> exitFuture = SettableFuture.create();
        exitFutures.put(position, exitFuture);
        return exitFuture;
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
        executor.shutdown();
        processingPosition.forEach(this::stopProcessingAt);
        processingPosition.clear();
        pendingChunkTasks.clear();
        positions.clear();
        currentStages.clear();
        exitFutures.clear();
    }

    public void restart() {
        pendingChunkTasks.clear();
        executor.getQueue().clear();
        processingPosition.forEach(this::stopProcessingAt);
        processingPosition.clear();
        positions.clear();
        currentStages.clear();
        exitFutures.clear();
    }

    /**
     * Stop processing chunk at position.
     *
     * @param pos position of chunk to stop processing.
     */
    public void stopProcessingAt(Vector3ic pos) {
        processingPosition.remove(pos);
        Chunk chunk = positions.remove(pos);
        if (chunk != null) {
            chunk.dispose();
            currentStages.remove(chunk);
        }
        pendingChunkTasks.keySet().removeIf(chunkTask -> chunkTask.getPosition().equals(pos));
        exitFutures.remove(pos).cancel(true);
    }

    /**
     * Cleanuping Chunk processing after done.
     *
     * @param chunk chunk to cleanup
     */
    private void cleanup(Chunk chunk) {
        Vector3i position = chunk.getPosition(new Vector3i());
        processingPosition.remove(position);
        currentStages.remove(chunk);
        exitFutures.remove(position);
    }


    /**
     * Check is position processing.
     *
     * @param pos position for check
     * @return true if position processing, false otherwise
     */
    public boolean isPositionProcessing(Vector3ic pos) {
        return processingPosition.contains(pos);
    }

    /**
     * Get processing positions.
     *
     * @return copy of processing positions
     */
    public List<Vector3ic> getProcessingPosition() {
        return new LinkedList<>(processingPosition);
    }
}
