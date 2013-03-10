package org.terasology.world.chunks.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.Region3i;
import org.terasology.utilities.concurrency.TaskMaster;
import org.terasology.world.chunks.GeneratingChunkProvider;
import org.terasology.world.generator.core.ChunkGeneratorManager;

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

    private ChunkGeneratorManager generator;
    private GeneratingChunkProvider provider;

    public ChunkGenerationPipeline(GeneratingChunkProvider provider, ChunkGeneratorManager generatorManager, Comparator<ChunkTask> taskComparator) {
        this.provider = provider;
        this.generator = generatorManager;
        chunkReviewer = TaskMaster.<ChunkRequest>createPriorityTaskMaster(NUM_REVIEW_THREADS, 64);
        chunkGenerator = TaskMaster.createPriorityTaskMaster(NUM_TASK_THREADS, 128, taskComparator);
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
        chunkReviewer.shutdown(new ChunkRequest(this, provider, ChunkRequest.Type.EXIT, Region3i.EMPTY));
        chunkGenerator.shutdown(new ShutdownChunkTask());
    }

    public ChunkGeneratorManager getChunkGeneratorManager() {
        return generator;
    }


}
