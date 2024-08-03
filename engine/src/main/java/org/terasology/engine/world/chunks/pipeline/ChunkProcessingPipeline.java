// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.monitoring.ThreadActivity;
import org.terasology.engine.monitoring.ThreadMonitor;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.pipeline.stages.ChunkTask;
import org.terasology.engine.world.chunks.pipeline.stages.ChunkTaskProvider;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.primitives.Ints.constrainToRange;

/**
 * Manages execution of chunk processing.
 * <p>
 * {@link Chunk}s will processing on stages {@link ChunkProcessingPipeline#addStage}
 */
public class ChunkProcessingPipeline {

    @SuppressWarnings("UnstableApiUsage")
    private static final int DEFAULT_TASK_THREADS = constrainToRange(
            Runtime.getRuntime().availableProcessors() - 2, 1, 4);
    private static final Logger logger = LoggerFactory.getLogger(ChunkProcessingPipeline.class);

    private final List<ChunkTaskProvider> stages = Lists.newArrayList();
    private final Thread reactor;
    private final ChunkExecutorCompletionService chunkProcessor;
    private final ThreadPoolExecutor executor;
    private final Function<Vector3ic, Chunk> chunkProvider;
    private final Map<Vector3ic, ChunkProcessingInfo> chunkProcessingInfoMap = Maps.newConcurrentMap();
    private int threadIndex;

    /**
     * Create ChunkProcessingPipeline.
     */
    public ChunkProcessingPipeline(int chunkThreads, Function<Vector3ic, Chunk> chunkProvider, Comparator<Future<Chunk>> comparable) {
        this.chunkProvider = chunkProvider;

        int taskThreads = (chunkThreads == 0) ? DEFAULT_TASK_THREADS : chunkThreads;
        executor = new ThreadPoolExecutor(
                taskThreads,
                taskThreads, 0L,
                TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue(800, comparable),
                this::threadFactory,
                this::rejectQueueHandler);
        logger.debug("allocated {} threads", taskThreads);
        chunkProcessor = new ChunkExecutorCompletionService(executor,
                new PriorityBlockingQueue<>(800, comparable));
        reactor = new Thread(this::chunkTaskHandler);
        reactor.setDaemon(true);
        reactor.setName("Chunk-Processing-Reactor");
        reactor.start();
    }

    /**
     * Reactor thread. Handles all ChunkTask dependency logic and running.
     */
    private void chunkTaskHandler() {
        try {
            while (!executor.isTerminated()) {
                PositionFuture<Chunk> future = (PositionFuture<Chunk>) chunkProcessor.take();
                ChunkProcessingInfo chunkProcessingInfo = chunkProcessingInfoMap.get(future.getPosition());
                if (chunkProcessingInfo == null) {
                    continue; // chunk processing was cancelled.
                }
                onStageDone(future, chunkProcessingInfo);
            }
        } catch (InterruptedException e) {
            if (!executor.isTerminated()) {
                logger.error("Reactor thread was interrupted", e);
            }
            reactor.interrupt();
        }
    }

    private void onStageDone(PositionFuture<Chunk> future, ChunkProcessingInfo chunkProcessingInfo) throws InterruptedException {
        try {
            chunkProcessingInfo.resetTaskState();
            chunkProcessingInfo.setChunk(future.get());

            //Move by stage.
            if (chunkProcessingInfo.hasNextStage(stages)) {
                chunkProcessingInfo.nextStage(stages);
                chunkProcessingInfo.makeChunkTask();
            } else {
                // haven't next stage
                chunkProcessingInfo.endProcessing();
                cleanup(chunkProcessingInfo);
            }
            processChunkTasks();

        } catch (ExecutionException e) {
            String stageName =
                    chunkProcessingInfo.getChunkTaskProvider() == null
                            ? "Generation or Loading"
                            : chunkProcessingInfo.getChunkTaskProvider().getName();
logger.error("ChunkTask at position {} and stage [{}] catch error: ", chunkProcessingInfo.getPosition(), stageName, e); //NOPMD
            chunkProcessingInfo.getExternalFuture().setException(e);
        } catch (CancellationException ignored) {
        }
    }

    private void processChunkTasks() {
        for (ChunkProcessingInfo info : chunkProcessingInfoMap.values()) {
            processChunkInfo(info);
        }
    }

    private void processChunkInfo(ChunkProcessingInfo info) {
        if (info.getChunkTask() == null) {
            return;
        }
        if (info.getCurrentFuture() != null) {
            return;
        }
        ChunkTask chunkTask = info.getChunkTask();
        List<Vector3ic> requirements = chunkTask.getRequirements();
        List<Chunk> requiredChunks = Lists.newArrayListWithCapacity(requirements.size());
        for (Vector3ic pos : requirements) {
            Chunk chunk = getChunkBy(info.getChunkTaskProvider(), pos);
            if (chunk != null) {
                requiredChunks.add(chunk);
            } else {
                return;
            }
        }
        info.setCurrentFuture(runTask(chunkTask, requiredChunks));
    }

    private Chunk getChunkBy(ChunkTaskProvider requiredStage, Vector3ic position) {
        Chunk chunk = chunkProvider.apply(position);
        if (chunk == null) {
            ChunkProcessingInfo candidate = chunkProcessingInfoMap.get(position);
            if (candidate == null) {
                return null;
            }
            ChunkTaskProvider candidateCurrentStage = candidate.getChunkTaskProvider();
            if (stages.indexOf(candidateCurrentStage) >= stages.indexOf(requiredStage)) {
                chunk = candidate.getChunk();
            }
        }
        return chunk;
    }

    private Future<Chunk> runTask(ChunkTask task, List<Chunk> chunks) {
        return chunkProcessor.submit(() -> {
            try (ThreadActivity ignored = ThreadMonitor.startThreadActivity(task.getName())) {
                return task.apply(chunks);
            }
        }, task.getPosition());
    }

    private Thread threadFactory(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName("Chunk-Processing-" + threadIndex++);
        return thread;
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
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
    public ListenableFuture<Chunk> invokeGeneratorTask(Vector3ic position, Supplier<Chunk> generatorTask) {
        Preconditions.checkState(!stages.isEmpty(), "ChunkProcessingPipeline must to have at least one stage");
        ChunkProcessingInfo chunkProcessingInfo = chunkProcessingInfoMap.get(position);
        if (chunkProcessingInfo != null) {
            return chunkProcessingInfo.getExternalFuture();
        } else {
            SettableFuture<Chunk> exitFuture = SettableFuture.create();
            chunkProcessingInfo = new ChunkProcessingInfo(position, exitFuture);
            chunkProcessingInfoMap.put(position, chunkProcessingInfo);
            chunkProcessingInfo.setCurrentFuture(chunkProcessor.submit(generatorTask::get, position));
            return exitFuture;
        }
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
        executor.shutdown();
        chunkProcessingInfoMap.keySet().forEach(this::stopProcessingAt);
        chunkProcessingInfoMap.clear();
        executor.getQueue().clear();
        reactor.interrupt();
    }

    public void restart() {
        chunkProcessingInfoMap.clear();
        executor.getQueue().clear();
        chunkProcessingInfoMap.keySet().forEach(this::stopProcessingAt);
    }

    /**
     * Stop processing chunk at position.
     *
     * @param pos position of chunk to stop processing.
     */
    public void stopProcessingAt(Vector3ic pos) {
        ChunkProcessingInfo removed = chunkProcessingInfoMap.remove(pos);
        if (removed == null) {
            return;
        }

        removed.getExternalFuture().cancel(true);

        Future<Chunk> currentFuture = removed.getCurrentFuture();
        if (currentFuture != null) {
            currentFuture.cancel(true);
        }

        Chunk chunk = removed.getChunk();
        if (chunk != null) {
            chunk.dispose();
        }
    }

    /**
     * Cleanuping Chunk processing after done.
     *
     * @param chunkProcessingInfo chunk to cleanup
     */
    private void cleanup(ChunkProcessingInfo chunkProcessingInfo) {
        chunkProcessingInfoMap.remove(chunkProcessingInfo.getPosition(), chunkProcessingInfo);
    }

    /**
     * Check is position processing.
     *
     * @param pos position for check
     * @return true if position processing, false otherwise
     */
    public boolean isPositionProcessing(Vector3ic pos) {
        return chunkProcessingInfoMap.containsKey(pos);
    }

    /**
     * Get processing positions.
     *
     * @return copy of processing positions
     */
    public Iterable<Vector3ic> getProcessingPosition() {
        return chunkProcessingInfoMap.keySet();
    }
}
