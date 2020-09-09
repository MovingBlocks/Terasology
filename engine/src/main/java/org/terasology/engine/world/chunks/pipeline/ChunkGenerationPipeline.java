// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.utilities.concurrency.TaskMaster;

import java.util.Comparator;

/**
 *
 */
public class ChunkGenerationPipeline {
    private static final int NUM_TASK_THREADS = 8;
    private static final Logger logger = LoggerFactory.getLogger(ChunkGenerationPipeline.class);

    private final TaskMaster<ChunkTask> chunkGenerator;

    public ChunkGenerationPipeline(Comparator<ChunkTask> taskComparator) {
        chunkGenerator = TaskMaster.createDynamicPriorityTaskMaster("Chunk-Generator", NUM_TASK_THREADS,
                taskComparator);
    }

    public void doTask(ChunkTask task) {
        try {
            chunkGenerator.put(task);
        } catch (InterruptedException e) {
            logger.error("Failed to enqueue task {}", task, e);
        }
    }

    public void shutdown() {
        chunkGenerator.shutdown(new ShutdownChunkTask(), false);
    }

    public void restart() {
        chunkGenerator.restart();
    }

}
