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
 * Chunk tasks are executing in background threads. Chunk tasks are executeng by priority via {@link Comparable}.
 */
public class ChunkProcessingPipeline implements ChunkTaskListener {
    private static final int NUM_TASK_THREADS = 8;
    private static final Logger logger = LoggerFactory.getLogger(ChunkProcessingPipeline.class);

    private final TaskMaster<ChunkTask> chunkProcessor;
    private final List<Function<Chunk, ChunkTask>> stages = new LinkedList<>();
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
     * Run generator task and then run pipeline with it.
     * @param generatorTask ChunkTask which provides new chunk to pipeline
     */
    public void invokeGeneratorTask(SupplierChunkTask generatorTask) {
        doTask(new ChunkTaskListenerWrapper(generatorTask, (chunkTask) -> {
            // check that we haven't many tasks on one position.
            if (chunkNextStages
                    .keySet()
                    .stream()
                    .map(Chunk::getPosition)
                    .noneMatch((p)-> p.equals(chunkTask.getChunk().getPosition()))) {
                invokePipeline(chunkTask.getChunk());
            }
        }));
    }

    public void invokePipeline(Chunk chunk) {
        Function<Chunk, ChunkTask> nextStage =
                chunkNextStages.computeIfAbsent(chunk, c -> new LinkedList<>(stages)).poll();
        if (nextStage == null) {
            chunkNextStages.remove(chunk);
            return;
        }
        invokeStage(chunk, nextStage);
    }

    public ChunkProcessingPipeline addStage(Function<Chunk, ChunkTask> stage) {
        stages.add(stage);
        return this;
    }

    private void invokeStage(Chunk chunk, Function<Chunk, ChunkTask> stage) {
        doTaskWrapper(stage.apply(chunk));
    }

    private void doTaskWrapper(ChunkTask task) {
        ChunkTask wrapper = new ChunkTaskListenerWrapper(task, this);
        doTask(wrapper);
    }

    public void doTask(ChunkTask task) {
        try {
            chunkProcessor.put(task);
        } catch (InterruptedException e) {
            logger.error("Failed to enqueue task {}", task, e);
        }
    }

    public void shutdown() {
        chunkNextStages.clear();
        chunkProcessor.shutdown(new ShutdownChunkTask(), false);
    }

    public void restart() {
        chunkNextStages.clear();
        chunkProcessor.restart();
    }

    @Override
    public void onDone(ChunkTask chunkTask) {
        if (chunkTask.needsRepeat()) {
            doTaskWrapper(chunkTask);
        } else {
            invokePipeline(chunkTask.getChunk());
        }
    }

    public Map<Vector3i, Chunk> getProcessingChunks() {
        return chunkNextStages.keySet().stream().collect(Collectors.toMap(k -> k.getPosition(), Function.identity()));
    }
}
