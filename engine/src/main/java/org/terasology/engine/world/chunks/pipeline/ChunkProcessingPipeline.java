// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.monitoring.ThreadActivity;
import org.terasology.engine.monitoring.ThreadMonitor;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.pipeline.stages.ChunkTask;
import org.terasology.engine.world.chunks.pipeline.stages.ChunkTaskProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Manages execution of chunk processing.
 * <p>
 * {@link Chunk}s will processing on stages {@link ChunkProcessingPipeline#addStage}
 */
public class ChunkProcessingPipeline {

    private static final int NUM_TASK_THREADS = 2;
    private static final int NUM_CHUNKS_AT_ONCE = 8;
    private static final Logger logger = LoggerFactory.getLogger(ChunkProcessingPipeline.class);

    /**
     * This object can be `wait`ed on, and will notify all waiting threads when the provider is done generating chunks.
     * It's mostly meant for testing.
     */
    private final Object doneGeneratingSignal = new Object();
    private final AtomicInteger numAliveThreads = new AtomicInteger(0);
    /**
     * Another signal object, used to tell worker threads when new chunks become available.
     */
    private final Object newChunksSignal = new Object();
    private final InitialChunkProvider initialChunkProvider;
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
            Thread worker = new Thread(this::runPoolThread);
            worker.setDaemon(true);
            worker.setName("Chunk-Processing-" + i);
            worker.start();
        }
    }

    /**
     * Notify the pipeline that new chunks are available, so if any worker threads were suspended, they should be resumed.
     */
    public void notifyUpdate() {
        synchronized (newChunksSignal) {
            newChunksSignal.notifyAll();
        }
    }

    private void stopPoolThreads() {
        shouldStop.set(true);
        notifyUpdate();
    }

    /**
     * Wait until this pipeline is done generating and processing chunks, or timeoutMillis milliseconds, whichever is earlier.
     */
    public void waitUntilDone(long timeoutMillis) throws InterruptedException {
        synchronized (doneGeneratingSignal) {
            doneGeneratingSignal.wait(timeoutMillis);
        }
    }

    private void runPoolThread() {
        Preconditions.checkState(!stages.isEmpty(), "ChunkProcessingPipeline must have at least one stage");
        numAliveThreads.incrementAndGet();

        try {
            while (!shouldStop.get()) {
                // Prioritize advancing generation of chunks that were already started
                if (processChunkTasks()) {
                    continue;
                }

                // But if there aren't any chunks that can advance to the next stage right now, start some new chunks
                for (int i = 0; i < NUM_CHUNKS_AT_ONCE; i++) {
                    Optional<Chunk> chunk = initialChunkProvider.next(chunkProcessingInfoMap.keySet());
                    if (chunk.isEmpty()) {
                        // If i > 0, then this thread started some chunks earlier, so it's not done until they're processed
                        if (i > 0) {
                            continue;
                        }
                        if (shouldStop.get()) {
                            break;
                        }
                        // Don't signal that generation is done until all threads are done
                        if (numAliveThreads.decrementAndGet() == 0) {
                            synchronized (doneGeneratingSignal) {
                                doneGeneratingSignal.notifyAll();
                            }
                        }
                        synchronized (newChunksSignal) {
                            newChunksSignal.wait();
                        }
                        numAliveThreads.incrementAndGet();
                        continue;
                    }
                    Vector3ic position = chunk.get().getPosition();

                    ChunkProcessingInfo chunkProcessingInfo = new ChunkProcessingInfo(position);
                    chunkProcessingInfo.setChunk(chunk.get());
                    chunkProcessingInfo.nextStage(stages);
                    chunkProcessingInfo.makeChunkTask();
                    chunkProcessingInfoMap.put(position, chunkProcessingInfo);
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Chunk processing thread interrupted");
        } finally {
            numAliveThreads.decrementAndGet();
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

                List<Chunk> providedChunks = new ArrayList<>();
                boolean satisfied = true;
                for (Vector3ic pos : chunkTask.getRequirements()) {
                    Chunk chunk = getChunkBy(chunkProcessingInfo.getChunkTaskProvider(), pos);
                    // If we don't have all the requirements generated yet, skip it
                    if (chunk == null) {
                        satisfied = false;
                        break;
                    }
                    providedChunks.add(chunk);
                }

                // If another thread is running the task, just skip it
                if (satisfied && chunkProcessingInfo.lock.tryLock()) {
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
    public Iterable<Vector3ic> getProcessingPosition() {
        return chunkProcessingInfoMap.keySet();
    }
}
