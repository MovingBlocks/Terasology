// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import com.google.api.client.util.Lists;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionIterable;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.pipeline.stages.FunctionalStage;
import org.terasology.world.chunks.pipeline.stages.ProcessingStage;
import org.terasology.world.chunks.pipeline.stages.StageProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Manages execution of chunk tasks on a queue.
 * <p>
 * {@link ChunkTask}s are executing in background threads.
 * <p>
 * {@link ChunkTask}s are executing by priority via {@link Comparable}.
 * <p>
 * {@link Chunk}s will processing on stages {@link ChunkProcessingPipeline#addStage}
 */
public class ChunkProcessingPipeline {

    private static final int NUM_TASK_THREADS = 8;
    private static final Logger logger = LoggerFactory.getLogger(ChunkProcessingPipeline.class);

    private final ExecutorService chunkProcessor;
    private final Function<Vector3ic, Chunk> chunkProvider;
    private final List<ProcessingStage> stages = Lists.newArrayList();
    private final List<ChunkRemoveFromPipelineListener> chunkRemoveFromPipelineListeners = new LinkedList<>();
    private final Table<Vector3ic, ProcessingStage, CompletableFuture<Chunk>> processingTable
            = HashBasedTable.create();
    private final Set<Vector3ic> processingPosition = Sets.newConcurrentHashSet();
    private final Set<Vector3ic> invalidatedPositions = Sets.newConcurrentHashSet();
    private final Table<Vector3ic, ProcessingStage, CompletableFuture<Chunk>> waitingNewFutures =
            HashBasedTable.create();

    /**
     * Create ChunkProcessingPipeline.
     */
    public ChunkProcessingPipeline(Function<Vector3ic, Chunk> chunkProvider) {
        this.chunkProvider = chunkProvider;

        chunkProcessor = new ForkJoinPool(NUM_TASK_THREADS,
                ChunkProcessingPipeline::threadFactory,
                ChunkProcessingPipeline::onRejectExecution,
                true);
    }

    private static ForkJoinWorkerThread threadFactory(ForkJoinPool pool) {
        final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        worker.setName("Chunk-Processing-" + worker.getPoolIndex());
        return worker;
    }

    private static void onRejectExecution(Thread thread, Throwable throwable) {
        logger.error("Cannot execute task: {}", throwable, throwable);
    }

    /**
     * Add stage to pipeline.
     *
     * @param stage function for ChunkTask generating by Chunk.
     * @return self for Fluent api.
     */
    public ChunkProcessingPipeline addStage(ProcessingStage stage) {
        stages.add(stage);
        return this;
    }


    /**
     * Register chunk task listener.
     *
     * @param listener listener.
     * @return self for Fluent api.
     */
    public ChunkProcessingPipeline addListener(ChunkRemoveFromPipelineListener listener) {
        chunkRemoveFromPipelineListeners.add(listener);
        return this;
    }

    /**
     * Run generator task and then run pipeline processing with it.
     *
     * @param generatorTask ChunkTask which provides new chunk to pipeline
     */
    public Future<Chunk> invokeGeneratorTask(Vector3i position, Supplier<Chunk> generatorTask) {
        processingPosition.add(position);
        CompletableFuture<Chunk> completableFuture = CompletableFuture.supplyAsync(generatorTask, chunkProcessor);

        for (ProcessingStage stage : stages) {
            FunctionalStage functionalStage;
            if (stage instanceof FunctionalStage) {
                functionalStage = (FunctionalStage) stage;
            } else if (stage instanceof StageProvider) {
                StageProvider stageProvider = (StageProvider) stage;
                Collection<CompletableFuture<Chunk>> nearbyFutures = getNearbyPositions(position)
                        .map(p -> getFutureAt(p, stage))
                        .collect(Collectors.toList());

                functionalStage = stageProvider.apply(nearbyFutures);
            } else {
                throw new UnsupportedOperationException("Pipeline not handling this type of Processing Stage");
            }
            CompletableFuture<Chunk> waiting = waitingNewFutures.get(position, stage);
            if (waiting != null) {
                completableFuture.thenAcceptAsync(waiting::complete, chunkProcessor);
            }

            completableFuture = functionalStage.apply(chunkProcessor, completableFuture);

            processingTable.put(position, stage, completableFuture);
        }
        completableFuture = clean(completableFuture);
        return completableFuture.handleAsync((c, e) -> {
            if (e != null) {
                logger.error("Task ended with error", e);
            }
            return c;
        }, chunkProcessor);
    }

    private CompletableFuture<Chunk> clean(CompletableFuture<Chunk> completableFuture) {
        return completableFuture.thenApplyAsync(c -> {
            Vector3i pos = c.getPosition(new Vector3i());
            Set<ProcessingStage> columns = new HashSet<>(processingTable.row(pos).keySet());
            columns.forEach(column -> processingTable.remove(pos, column));
            processingPosition.remove(pos);
            return c;
        }, chunkProcessor);
    }

    private Stream<Vector3ic> getNearbyPositions(Vector3i pos) {
        return StreamSupport.stream(BlockRegionIterable.region(new BlockRegion(
                pos.x - 1, pos.y - 1, pos.z - 1,
                pos.x + 1, pos.y + 1, pos.z + 1
        )).build().spliterator(), false).map(Vector3i::new);
    }

    private CompletableFuture<Chunk> getFutureAt(Vector3ic pos, ProcessingStage currentStage) {
        Chunk chunk = chunkProvider.apply(pos);
        if (chunk != null) {
            return CompletableFuture.completedFuture(chunk);
        }
        if (isPositionProcessing(pos)) {
            ProcessingStage previosStage = stages.get(stages.indexOf(currentStage) - 1);
            CompletableFuture<Chunk> futureFromPreviosStage = processingTable.get(pos, previosStage);
            if (futureFromPreviosStage != null) {
                return futureFromPreviosStage;
            }
        }
        CompletableFuture<Chunk> waitingFuture = waitingNewFutures.get(pos, currentStage);
        if (waitingFuture == null) {
            CompletableFuture<Chunk> candidate = new CompletableFuture<>();
            waitingNewFutures.put(pos, currentStage, candidate);
            return candidate;
        } else {
            return waitingFuture;
        }
    }

    /**
     * Send chunk to processing pipeline. If chunk not processing yet then pipeline will be setted. If chunk processed
     * then chunk will be processing in next stage;
     *
     * @param chunk chunk to process.
     */
    public Future<Chunk> invokePipeline(Chunk chunk) {
        return invokeGeneratorTask(chunk.getPosition(new Vector3i()), () -> chunk);
    }

    public void shutdown() {
        processingTable.clear();
        processingPosition.clear();
        chunkProcessor.shutdown();
    }

    public void restart() {
        processingTable.clear();
        processingPosition.clear();
    }

    /**
     * Stop processing chunk at position.
     *
     * @param pos position of chunk to stop processing.
     */
    public void stopProcessingAt(Vector3i pos) {
        invalidatedPositions.add(pos);
        Set<ProcessingStage> columns = new HashSet<>(processingTable.row(pos).keySet());
        columns.forEach(column -> processingTable.remove(pos, column));
        processingPosition.remove(pos);
        chunkRemoveFromPipelineListeners.forEach(l -> l.onRemove(pos));
    }

    /**
     * Check is position processing.
     *
     * @param pos position for check
     * @return true if position processing, false otherwise
     */
    public boolean isPositionProcessing(Vector3ic pos) {
        return processingPosition.contains(pos);
    }

    /**
     * Get processing positions.
     *
     * @return copy of processing positions
     */
    public List<Vector3ic> getProcessingPosition() {
        return new LinkedList<>(processingPosition);
    }
}
