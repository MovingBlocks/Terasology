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
import org.terasology.math.Region3i;
import org.terasology.utilities.concurrency.TaskMaster;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.world.generator.WorldGenerator;

import java.util.Comparator;

/**
 * @author Immortius
 */
public class ChunkGenerationPipeline {
    private static final int NUM_REVIEW_THREADS = 1;
    private static final int NUM_TASK_THREADS = 8;
    private static final Logger logger = LoggerFactory.getLogger(ChunkGenerationPipeline.class);

    private TaskMaster<ChunkRequest> chunkReviewer;
    private TaskMaster<ChunkTask> chunkGenerator;

    private WorldGenerator generator;
    private GeneratingChunkProvider provider;

    public ChunkGenerationPipeline(GeneratingChunkProvider provider, WorldGenerator generator, Comparator<ChunkTask> taskComparator) {
        this.provider = provider;
        this.generator = generator;
        chunkReviewer = TaskMaster.createPriorityTaskMaster("Chunk-Reviewer", NUM_REVIEW_THREADS, 64);
        chunkGenerator = TaskMaster.createPriorityTaskMaster("Chunk-Generator", NUM_TASK_THREADS, 128, taskComparator);
    }

    public void requestReview(Region3i region) {
        try {
            chunkReviewer.put(new ChunkRequest(this, provider, ChunkRequest.Type.REVIEW, region));
        } catch (InterruptedException e) {
            logger.error("Failed to enqueue review request for region {}", region, e);
        }
    }

    public void requestProduction(Region3i region) {
        try {
            chunkReviewer.put(new ChunkRequest(this, provider, ChunkRequest.Type.PRODUCE, region));
        } catch (InterruptedException e) {
            logger.error("Failed to enqueue production request for region {}", region, e);
        }
    }

    public void doTask(ChunkTask task) {
        try {
            chunkGenerator.put(task);
        } catch (InterruptedException e) {
            logger.error("Failed to enqueue task {}", task, e);
        }
    }

    public void shutdown() {
        chunkReviewer.shutdown(new ChunkRequest(this, provider, ChunkRequest.Type.EXIT, Region3i.EMPTY), false);
        chunkGenerator.shutdown(new ShutdownChunkTask(), false);
    }

    public WorldGenerator getWorldGenerator() {
        return generator;
    }


}
