// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.SettableFuture;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.monitoring.ThreadActivity;
import org.terasology.engine.monitoring.ThreadMonitor;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.pipeline.stages.ChunkTask;
import org.terasology.engine.world.chunks.pipeline.stages.ChunkTaskProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages execution of chunk processing.
 * <p>
 * {@link Chunk}s will processing on stages {@link ChunkProcessingPipeline#addStage}
 */
public class ChunkProcessingPipeline {

    private static final int NUM_TASK_THREADS = 4;
    private static final int NUM_CHUNKS_AT_ONCE = 8;
    private static final Logger logger = LoggerFactory.getLogger(ChunkProcessingPipeline.class);

    private final InitialChunkProvider initialChunkProvider;
    private final Object waitForNewChunks = new Object();
    private final Thread[] poolThreads = new Thread[NUM_TASK_THREADS];
    private final AtomicBoolean shouldStop = new AtomicBoolean(false);

    private final List<ChunkTaskProvider> stages = Lists.newArrayList();
    private final Function<Vector3ic, Chunk> chunkProvider;
    private final Map<Vector3ic, ChunkProcessingInfo> chunkProcessingInfoMap = Maps.newConcurrentMap();

    /**
     * Create ChunkProcessingPipeline.
     */
    public ChunkProcessingPipeline(Function<Vector3ic, Chunk> chunkProvider, InitialChunkProvider initialChunkProvider) {
        this.chunkProvider = chunkProvider;
        this.initialChunkProvider = initialChunkProvider;
    }

    public void start() {
        for (int i = 0; i < NUM_TASK_THREADS; i++) {
            poolThreads[i] = new Thread(this::runPoolThread);
            poolThreads[i].setDaemon(true);
            poolThreads[i].setName("Chunk-Processing-" + i);
            poolThreads[i].start();
        }
    }

    public void notifyUpdate() {
        synchronized (waitForNewChunks) {
            waitForNewChunks.notifyAll();
        }
    }

    private void stopPoolThreads() {
        shouldStop.set(true);
        for (Thread thread : poolThreads) {
            thread.interrupt();
        }
    }

    private void runPoolThread() {
        Preconditions.checkState(!stages.isEmpty(), "ChunkProcessingPipeline must to have at least one stage");

        try {
            while (!shouldStop.get()) {
                // Prioritize advancing generation of chunks that were already started
                if (processChunkTasks()) {
                    continue;
                }

                // But if there aren't any chunks that can advance to the next stage right now, start some new chunks
                for (int i = 0; i < NUM_CHUNKS_AT_ONCE; i++) {
                    Chunk chunk;
                    synchronized (initialChunkProvider) {
                        if (!initialChunkProvider.hasNext()) {
                            synchronized (waitForNewChunks) {
                                waitForNewChunks.wait();
                            }
                            continue;
                        }
                        chunk = initialChunkProvider.next(chunkProcessingInfoMap.keySet());
                    }
                    if (chunk == null) {
                        continue;
                    }
                    Vector3ic position = chunk.getPosition();

                    ChunkProcessingInfo chunkProcessingInfo = new ChunkProcessingInfo(position);
                    chunkProcessingInfo.setChunk(chunk);
                    chunkProcessingInfo.nextStage(stages);
                    chunkProcessingInfo.makeChunkTask();
                    chunkProcessingInfoMap.put(position, chunkProcessingInfo);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @return whether any progress was made.
     */
    private boolean processChunkTasks() {
        boolean anyChanged = false;
        for (ChunkProcessingInfo chunkProcessingInfo : chunkProcessingInfoMap.values()) {
            ChunkTask chunkTask = chunkProcessingInfo.getChunkTask();
            if (chunkTask != null) {
                Set<Chunk> providedChunks = chunkTask.getRequirements().stream()
                        .map(pos -> getChunkBy(chunkProcessingInfo.getChunkTaskProvider(), pos))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (providedChunks.size() == chunkTask.getRequirements().size()) {
                    // If another thread is running the task, just skip it
                    if (chunkProcessingInfo.lock.tryLock()) {
                        try {
                            try (ThreadActivity ignored = ThreadMonitor.startThreadActivity(chunkTask.getName())) {
                                chunkProcessingInfo.setChunk(chunkTask.apply(providedChunks));
                            }
                            chunkProcessingInfo.resetTaskState();

                            if (chunkProcessingInfo.hasNextStage(stages)) {
                                chunkProcessingInfo.nextStage(stages);
                                chunkProcessingInfo.makeChunkTask();
                                anyChanged = true;
                            } else {
                                cleanup(chunkProcessingInfo);
                            }
                        } catch (Exception e) {
                            String stageName =
                                    chunkProcessingInfo.getChunkTaskProvider() == null
                                            ? "Generation or Loading"
                                            : chunkProcessingInfo.getChunkTaskProvider().getName();
                            logger.error(
                                    String.format("ChunkTask at position %s and stage [%s] catch error: ",
                                            chunkProcessingInfo.getPosition(), stageName),
                                    e);
                        } finally {
                            chunkProcessingInfo.lock.unlock();
                        }
                    }
                }
            } else {
                // It doesn't have a task, so either it needs to be advanced a stage, or it's done and needs to be removed
                if (chunkProcessingInfo.lock.tryLock()) {
                    try {
                        if (chunkProcessingInfo.hasNextStage(stages)) {
                            chunkProcessingInfo.nextStage(stages);
                            chunkProcessingInfo.makeChunkTask();
                            anyChanged = true;
                        } else {
                            cleanup(chunkProcessingInfo);
                        }
                    } finally {
                        chunkProcessingInfo.lock.unlock();
                    }
                }

            }
        }
        return anyChanged;
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

    public void shutdown() {
        stopPoolThreads();
        chunkProcessingInfoMap.keySet().forEach(this::stopProcessingAt);
        chunkProcessingInfoMap.clear();
    }

    public void restart() {
        shutdown();
        start();
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

        Chunk chunk = removed.getChunk();
        if (chunk != null) {
            chunk.dispose();
        }
    }

    /**
     * Cleanup Chunk processing after done.
     *
     * @param chunkProcessingInfo chunk to cleanup
     */
    private void cleanup(ChunkProcessingInfo chunkProcessingInfo) {
        synchronized (chunkProcessingInfoMap) {
            chunkProcessingInfoMap.remove(chunkProcessingInfo.getPosition(), chunkProcessingInfo);
        }
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
    public List<Vector3ic> getProcessingPosition() {
        return new LinkedList<>(chunkProcessingInfoMap.keySet());
    }
}
