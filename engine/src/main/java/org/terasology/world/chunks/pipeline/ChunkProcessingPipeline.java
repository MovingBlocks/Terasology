// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import com.google.common.collect.Sets;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.concurrency.TaskMaster;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.pipeline.tasks.ChunkTaskListenerWrapper;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Manages execution of chunk tasks on a queue.
 * <p>
 * {@link ChunkTask}s are executing in background threads. {@link ChunkTask}s are executing by priority via {@link
 * Comparable}. {@link Chunk}s will processing on stages {@link ChunkProcessingPipeline#addStage}
 */
public class ChunkProcessingPipeline implements ChunkTaskListener {
    private static final int NUM_TASK_THREADS = 8;
    private static final Logger logger = LoggerFactory.getLogger(ChunkProcessingPipeline.class);

    private final TaskMaster<ChunkTask> chunkProcessor;
    private final List<Function<Chunk, ChunkTask>> stages = new LinkedList<>();
    private final List<ChunkTaskListener> chunkTaskListeners = new LinkedList<>();
    private final List<ChunkInvalidationListener> chunkInvalidationListeners = new LinkedList<>();
    private final Map<Chunk, Deque<Function<Chunk, ChunkTask>>> chunkNextStages = new ConcurrentHashMap<>();
    private final Set<org.joml.Vector3i> processingPositions = Sets.newConcurrentHashSet();
    private final Set<org.joml.Vector3i> invalidatedPositions = Sets.newConcurrentHashSet();

    /**
     * Create ChunkProcessingTaskMaster.
     *
     * @param taskComparator using by TaskMaster for priority ordering task.
     */
    public ChunkProcessingPipeline(Comparator<ChunkTask> taskComparator) {
        chunkProcessor = TaskMaster.createDynamicPriorityTaskMaster("Chunk-Processing", NUM_TASK_THREADS,
                taskComparator);
    }

    /**
     * Add stage to pipeline. If stage instance of {@link ChunkTaskListener} - it's will be register as listener. If
     * stage instance of {@link ChunkInvalidationListener} - it's will be register as listener.
     *
     * @param stage function for ChunkTask generating by Chunk.
     * @return self for Fluent api.
     */
    public ChunkProcessingPipeline addStage(Function<Chunk, ChunkTask> stage) {
        stages.add(stage);
        if (stage instanceof ChunkTaskListener) {
            addListener((ChunkTaskListener) stage);
        }
        if (stage instanceof ChunkInvalidationListener) {
            addListener((ChunkInvalidationListener) stage);
        }
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
    public ChunkProcessingPipeline addListener(ChunkInvalidationListener listener) {
        chunkInvalidationListeners.add(listener);
        return this;
    }

    /**
     * Run generator task and then run pipeline processing with it.
     *
     * @param generatorTask ChunkTask which provides new chunk to pipeline
     */
    public void invokeGeneratorTask(SupplierChunkTask generatorTask) {
        if (processingPositions.contains(generatorTask.getPosition())) {
            return;
        }
        processingPositions.add(generatorTask.getPosition());
        doTask(new ChunkTaskListenerWrapper(generatorTask, (chunkTask) -> {
            invokePipeline(chunkTask.getChunk());
        }));
    }

    /**
     * Send chunk to processing pipeline. If chunk not processing yet then pipeline will be setted. If chunk processed
     * then chunk will be processing in next stage;
     *
     * @param chunk chunk to process.
     */
    public void invokePipeline(Chunk chunk) {
        if (chunk == null) {
            return;
        }
        Function<Chunk, ChunkTask> nextStage =
                chunkNextStages.computeIfAbsent(chunk, c -> new LinkedList<>(stages)).poll();
        Vector3i position = chunk.getPosition(new Vector3i());

        if (chunk.isReady() || invalidatedPositions.remove(position) || nextStage == null) {
            processingPositions.remove(position);
            chunkNextStages.remove(chunk);
            chunkInvalidationListeners.forEach(l -> l.onInvalidation(position));
            return;
        }
        invokeStage(chunk, nextStage);
    }

    public void shutdown() {
        chunkNextStages.clear();
        processingPositions.clear();
        chunkProcessor.shutdown(new ShutdownChunkTask(), false);
    }

    public void restart() {
        chunkNextStages.clear();
        processingPositions.clear();
        chunkProcessor.restart();
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
        chunkInvalidationListeners.forEach(l -> l.onInvalidation(pos));
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

    /**
     * Wrap chunktask with this as listener and do it.
     *
     * @param task task which wrapping.
     */
    void doTaskWrapper(ChunkTask task) {
        ChunkTask wrapper = new ChunkTaskListenerWrapper(task, this);
        doTask(wrapper);
    }

    private void doTask(ChunkTask task) {
        try {
            logger.debug("Start processing task :" + task);
            chunkProcessor.put(task);
        } catch (InterruptedException e) {
            logger.error("Failed to enqueue task {}", task, e);
        }
    }

    private void invokeStage(Chunk chunk, Function<Chunk, ChunkTask> stage) {
        doTaskWrapper(stage.apply(chunk));
    }
}
