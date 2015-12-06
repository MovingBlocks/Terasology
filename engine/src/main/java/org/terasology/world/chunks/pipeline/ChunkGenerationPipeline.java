/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.world.chunks.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.concurrency.TaskMaster;

import java.util.Comparator;

/**
 */
public class ChunkGenerationPipeline {
    private static final int NUM_TASK_THREADS = 8;
    private static final Logger logger = LoggerFactory.getLogger(ChunkGenerationPipeline.class);

    private TaskMaster<ChunkTask> chunkGenerator;

    public ChunkGenerationPipeline(Comparator<ChunkTask> taskComparator) {
        chunkGenerator = TaskMaster.createDynamicPriorityTaskMaster("Chunk-Generator", NUM_TASK_THREADS, taskComparator);
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
