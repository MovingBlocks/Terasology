// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Vector3i;
import org.terasology.utilities.concurrency.TaskMaster;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.pipeline.tasks.ChunkTaskListenerWrapper;

import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final Map<Chunk, Deque<Function<Chunk, ChunkTask>>> chunkNextStages = new ConcurrentHashMap<>(); // TODO
    // use better collection.

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
     * Add stage to pipeline. If stage instance of {@link ChunkTaskListener} - it's will be register as listener.
     *
     * @param stage function for ChunkTask generating by Chunk.
     * @return self for Fluent api.
     */
    public ChunkProcessingPipeline addStage(Function<Chunk, ChunkTask> stage) {
        stages.add(stage);
        if (stage instanceof ChunkTaskListener) {
            addListener((ChunkTaskListener) stage);
        }
        return this;
    }

    /**
     * Register chink task listener.
     *
     * @param listener listener.
     * @return self for Fluent api.
     */
    public ChunkProcessingPipeline addListener(ChunkTaskListener listener) {
        chunkTaskListeners.add(listener);
        return this;
    }

    /**
     * Run generator task and then run pipeline processing with it.
     *
     * @param generatorTask ChunkTask which provides new chunk to pipeline
     */
    public void invokeGeneratorTask(SupplierChunkTask generatorTask) {
        doTask(new ChunkTaskListenerWrapper(generatorTask, (chunkTask) -> {
            // check that we haven't many tasks on one position.
            if (chunkNextStages
                    .keySet()
                    .stream()
                    .map(Chunk::getPosition)
                    .noneMatch((p) -> p.equals(chunkTask.getChunk().getPosition()))) {
                invokePipeline(chunkTask.getChunk());
            }
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
        if (nextStage == null) {
            chunkNextStages.remove(chunk);
            return;
        }
        invokeStage(chunk, nextStage);
    }

    public void shutdown() {
        chunkNextStages.clear();
        chunkProcessor.shutdown(new ShutdownChunkTask(), false);
    }

    public void restart() {
        chunkNextStages.clear();
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
    
    public void remove(Chunk chunk) {
        chunkNextStages.remove(chunk);
    }

    /**
     * Processing chunks.
     *
     * @return Map of positions and chunks which currently in processing.
     */
    public Map<Vector3i, Chunk> getProcessingChunks() {
        return chunkNextStages.keySet().stream().collect(Collectors.toMap(k -> k.getPosition(), Function.identity()));
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
