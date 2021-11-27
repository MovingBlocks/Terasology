// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.joml.Vector3ic;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.monitoring.ThreadActivity;
import org.terasology.engine.monitoring.ThreadMonitor;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.pipeline.stages.ChunkTask;
import org.terasology.engine.world.chunks.pipeline.stages.ChunkTaskProvider;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Manages execution of chunk processing.
 * <p>
 * {@link Chunk}s will processing on stages {@link ChunkProcessingPipeline#addStage}
 */
public class ChunkProcessingPipeline {

    private static final int NUM_TASK_THREADS = 2;
    private static final int CHUNKS_AT_ONCE = 8;
    private static final Logger logger = LoggerFactory.getLogger(ChunkProcessingPipeline.class);

    private final List<ChunkTaskProvider> stages = Lists.newArrayList();

    private final Function<Vector3ic, Chunk> chunkProvider;
    private final Map<Vector3ic, ChunkProcessingInfo> chunkProcessingInfoMap = Maps.newConcurrentMap();
    private final List<Subscription> subs = new ArrayList<>();

    private final Scheduler scheduler;

    private final Object completeSignal = new Object();
    private Queue<ChunkProcessingInfo> processing = Queues.newConcurrentLinkedQueue();

    /**
     * Create ChunkProcessingPipeline.
     */
    public ChunkProcessingPipeline(Function<Vector3ic, Chunk> chunkProvider, Flux<Chunk> chunkStream) {
        this(chunkProvider, chunkStream, Schedulers.newParallel("chunk processing", NUM_TASK_THREADS));
    }

    /**
     * @param scheduler The scheduler to use for running chunk processing threads.
     */
    public ChunkProcessingPipeline(Function<Vector3ic, Chunk> chunkProvider, Flux<Chunk> chunkStream, Scheduler scheduler) {
        this.chunkProvider = chunkProvider;
        this.scheduler = scheduler;
        Flux<Chunk> stream = chunkStream.subscribeOn(scheduler);
        for (int i = 0; i < NUM_TASK_THREADS; i++) {
            stream.subscribe(new BaseSubscriber<Chunk>() {
                private final List<Chunk> buffer = new ArrayList<>();
                private Subscription sub;

                @Override
                public void hookOnSubscribe(Subscription sub) {
                    this.sub = sub;
                    subs.add(sub);
                }

                @Override
                public void hookOnNext(Chunk chunk) {
                    buffer.add(chunk);
                    if (buffer.size() >= CHUNKS_AT_ONCE) {
                        processNewChunks(buffer);
                        buffer.clear();
                        sub.request(CHUNKS_AT_ONCE);
                    }
                }

                @Override
                public void hookOnComplete() {
                    processNewChunks(buffer);
                    subs.remove(sub);
                    if (subs.isEmpty()) {
                        synchronized (completeSignal) {
                            completeSignal.notifyAll();
                        }
                    }
                }
            });
        }
    }

    /**
     * Notify the pipeline that new chunks are available, so if any worker threads were suspended, they should be resumed.
     */
    public void notifyUpdate() {
        // We can't use a foreach because `request` could trigger `hookOnComplete`, which modifies `subs`
        for (int i = subs.size() - 1; i >= 0; i--) {
            subs.get(i).request(CHUNKS_AT_ONCE);
        }
    }

    private void processNewChunks(List<Chunk> chunks) {
        for (Chunk chunk : chunks) {
            Vector3ic position = chunk.getPosition();
            if (chunkProcessingInfoMap.containsKey(position)) {
                logger.warn("Requested processing chunk that was already processing");
            }

            ChunkProcessingInfo chunkProcessingInfo = new ChunkProcessingInfo(position);
            chunkProcessingInfo.setChunk(chunk);
            chunkProcessingInfo.nextStage(stages);
            chunkProcessingInfo.makeChunkTask();
            processing.add(chunkProcessingInfo);
            chunkProcessingInfoMap.put(position, chunkProcessingInfo);
        }
        processingInfoReactor();
    }

    private void processingInfoReactor() {
        List<ChunkProcessingInfo> defer = Lists.newArrayList();
        ChunkProcessingInfo info;
        while ((info = processing.poll()) != null) {
            ChunkTask task = info.getChunkTask();
            if (task != null) {
                boolean satisfied = true;
                List<Chunk> providedChunks = new ArrayList<>(10);
                for (Vector3ic pos : task.getRequirements()) {
                    Chunk chunk = getChunkBy(info.getChunkTaskProvider(), pos);
                    // If we don't have all the requirements generated yet, skip it
                    if (chunk == null) {
                        satisfied = false;
                        break;
                    }
                    providedChunks.add(chunk);
                }

                if (satisfied) {
                    try {
                        try (ThreadActivity ignored = ThreadMonitor.startThreadActivity(task.getName())) {
                            info.setChunk(task.apply(providedChunks));
                        }
                        info.resetTaskState();
                        if (info.hasNextStage(stages)) {
                            info.nextStage(stages);
                            info.makeChunkTask();
                            processing.add(info);
                        } else {
                            cleanup(info);
                        }
                    } catch (Exception e) {
                        String stageName =
                                info.getChunkTaskProvider() == null
                                        ? "Generation or Loading"
                                        : info.getChunkTaskProvider().getName();
                        logger.error(
                                String.format("ChunkTask at position %s and stage [%s] catch error: ",
                                        info.getPosition(), stageName),
                                e);
                        cleanup(info);
                    }
                } else {
                    defer.add(info);
                }
            } else {
                if (info.hasNextStage(stages)) {
                    info.nextStage(stages);
                    info.makeChunkTask();
                    processing.add(info);
                } else {
                    cleanup(info);
                }
            }
        }
        processing.addAll(defer);
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
        for (Subscription s : subs) {
            s.cancel();
        }
        scheduler.dispose();
        chunkProcessingInfoMap.keySet().forEach(this::stopProcessingAt);
        chunkProcessingInfoMap.clear();
    }

    public void restart() {
        chunkProcessingInfoMap.keySet().forEach(this::stopProcessingAt);
        chunkProcessingInfoMap.clear();
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
        processing.remove(removed);

        Chunk chunk = removed.getChunk();
        if (chunk != null) {
            chunk.dispose();
        }
    }

    /**
     * Cleanuping Chunk processing after done.
     *
     * @param chunkProcessingInfo chunk to cleanup
     */
    private void cleanup(ChunkProcessingInfo chunkProcessingInfo) {
        chunkProcessingInfoMap.remove(chunkProcessingInfo.getPosition(), chunkProcessingInfo);
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
    public Set<Vector3ic> getProcessingPositions() {
        return chunkProcessingInfoMap.keySet();
    }
}
