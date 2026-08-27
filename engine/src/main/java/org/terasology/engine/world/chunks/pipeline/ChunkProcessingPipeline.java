// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.monitoring.ThreadActivity;
import org.terasology.engine.monitoring.ThreadMonitor;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.pipeline.stages.ChunkTask;
import org.terasology.engine.world.chunks.pipeline.stages.ChunkTaskProvider;
import org.terasology.engine.world.chunks.pipeline.stages.SingleChunkTask;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.google.common.primitives.Ints.constrainToRange;

/**
 * Manages execution of chunk processing.
 * <p>
 * {@link Chunk}s will processing on stages {@link ChunkProcessingPipeline#addStage}
 */
public class ChunkProcessingPipeline {

    @SuppressWarnings("UnstableApiUsage")
    private static final int DEFAULT_TASK_THREADS = constrainToRange(
            Runtime.getRuntime().availableProcessors() - 2, 1, 4);
    private static final Logger logger = LoggerFactory.getLogger(ChunkProcessingPipeline.class);

    /**
     * How long the reactor waits for a stage to complete before checking whether the pipeline has
     * gone idle. See {@link #skipBlockedStages()}.
     */
    private static final long POLL_INTERVAL_MS = 5000;
    /**
     * Consecutive idle polls (i.e. {@code POLL_INTERVAL_MS * IDLE_POLLS_BEFORE_SKIP} of pipeline-wide
     * silence) before positions with an unmet requirement are forced past their current stage.
     */
    private static final int IDLE_POLLS_BEFORE_SKIP = 2;

    private final List<ChunkTaskProvider> stages = Lists.newArrayList();
    private final Thread reactor;
    private final ChunkExecutorCompletionService chunkProcessor;
    private final ThreadPoolExecutor executor;
    private final Function<Vector3ic, Chunk> chunkProvider;
    private final Map<Vector3ic, ChunkProcessingInfo> chunkProcessingInfoMap = Maps.newConcurrentMap();
    /**
     * Positions currently waiting on a requirement that isn't available yet. A position lands here
     * whether the requirement is merely late (still being processed, or not yet requested but about
     * to be) or will genuinely never arrive (outside anything ever requested) - those two cases look
     * identical at the moment a requirement is found missing. {@link #skipBlockedStages()} is what
     * tells them apart, by waiting to see whether the whole pipeline stays quiet long enough that
     * "still coming" stops being plausible.
     */
    private final Set<Vector3ic> blockedPositions = Sets.newConcurrentHashSet();
    /** Reactor-thread-only; counts consecutive poll timeouts with nothing completing anywhere. */
    private int consecutiveIdlePolls;
    private int threadIndex;

    /**
     * Create ChunkProcessingPipeline.
     */
    public ChunkProcessingPipeline(int chunkThreads, Function<Vector3ic, Chunk> chunkProvider, Comparator<Future<Chunk>> comparable) {
        this.chunkProvider = chunkProvider;

        int taskThreads = (chunkThreads == 0) ? DEFAULT_TASK_THREADS : chunkThreads;
        executor = new ThreadPoolExecutor(
                taskThreads,
                taskThreads, 0L,
                TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue(800, comparable),
                this::threadFactory,
                this::rejectQueueHandler);
        logger.debug("allocated {} threads", taskThreads);
        chunkProcessor = new ChunkExecutorCompletionService(executor,
                new PriorityBlockingQueue<>(800, comparable));
        reactor = new Thread(this::chunkTaskHandler);
        reactor.setDaemon(true);
        reactor.setName("Chunk-Processing-Reactor");
        reactor.start();
    }

    /**
     * Reactor thread. Handles all ChunkTask dependency logic and running.
     */
    private void chunkTaskHandler() {
        try {
            while (!executor.isTerminated()) {
                PositionFuture<Chunk> future =
                        (PositionFuture<Chunk>) chunkProcessor.poll(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
                if (future == null) {
                    if (!blockedPositions.isEmpty() && ++consecutiveIdlePolls >= IDLE_POLLS_BEFORE_SKIP) {
                        skipBlockedStages();
                    }
                    continue;
                }
                consecutiveIdlePolls = 0;
                ChunkProcessingInfo chunkProcessingInfo = chunkProcessingInfoMap.get(future.getPosition());
                if (chunkProcessingInfo == null) {
                    continue; // chunk processing was cancelled.
                }
                onStageDone(future, chunkProcessingInfo);
            }
        } catch (InterruptedException e) {
            if (!executor.isTerminated()) {
                logger.error("Reactor thread was interrupted", e);
            }
            reactor.interrupt();
        }
    }

    private void onStageDone(PositionFuture<Chunk> future, ChunkProcessingInfo chunkProcessingInfo) throws InterruptedException {
        try {
            chunkProcessingInfo.resetTaskState();
            chunkProcessingInfo.setChunk(future.get());

            //Move by stage.
            if (chunkProcessingInfo.hasNextStage(stages)) {
                chunkProcessingInfo.nextStage(stages);
                chunkProcessingInfo.makeChunkTask();
                // Only this one info can newly have a pending (chunkTask set, no future yet) state -
                // resetTaskState() just cleared it, and makeChunkTask() is the only place that sets it.
                processChunkInfo(chunkProcessingInfo);
            } else {
                // haven't next stage
                chunkProcessingInfo.endProcessing();
                cleanup(chunkProcessingInfo);
            }
            // Either branch may have just satisfied a neighbour's requirement (reached a stage far
            // enough along, or finished and become visible via chunkProvider) - retry everyone who
            // was blocked on that, rather than rescanning every in-flight chunk.
            retryBlockedPositions();

        } catch (ExecutionException e) {
            String stageName =
                    chunkProcessingInfo.getChunkTaskProvider() == null
                            ? "Generation or Loading"
                            : chunkProcessingInfo.getChunkTaskProvider().getName();
logger.error("ChunkTask at position {} and stage [{}] catch error: ", chunkProcessingInfo.getPosition(), stageName, e); //NOPMD
            chunkProcessingInfo.getExternalFuture().setException(e);
        } catch (CancellationException ignored) {
        }
    }

    /**
     * Retry every position blocked on a missing requirement. A position only ever lands here because
     * some other chunk hadn't reached the stage (or existence) it needed - and this fires right after
     * a stage completion or chunk finish, which is the only thing that can have changed that.
     */
    private void retryBlockedPositions() {
        if (blockedPositions.isEmpty()) {
            return;
        }
        for (Vector3ic pos : Lists.newArrayList(blockedPositions)) {
            ChunkProcessingInfo info = chunkProcessingInfoMap.get(pos);
            if (info != null) {
                processChunkInfo(info);
            }
        }
    }

    private void processChunkInfo(ChunkProcessingInfo info) {
        if (info.getChunkTask() == null) {
            return;
        }
        if (info.getCurrentFuture() != null) {
            return;
        }
        ChunkTask chunkTask = info.getChunkTask();
        List<Vector3ic> requirements = chunkTask.getRequirements();
        List<Chunk> requiredChunks = Lists.newArrayListWithCapacity(requirements.size());
        boolean blocked = false;
        for (Vector3ic pos : requirements) {
            Chunk chunk = getChunkBy(info.getChunkTaskProvider(), pos);
            if (chunk != null) {
                requiredChunks.add(chunk);
            } else {
                // Could be being processed but not far enough along yet, could be not requested yet
                // but about to be, or could be a position nothing will ever request. Those look
                // identical here; skipBlockedStages() is what tells them apart.
                blocked = true;
                break;
            }
        }
        if (blocked) {
            blockedPositions.add(info.getPosition());
            return;
        }
        blockedPositions.remove(info.getPosition());
        info.setCurrentFuture(runTask(chunkTask, requiredChunks));
    }

    /**
     * Force every position still waiting on a requirement past its current stage, once the whole
     * pipeline has gone quiet for {@link #IDLE_POLLS_BEFORE_SKIP} consecutive polls.
     * <p>
     * Multi-chunk stages ask for a neighbourhood around their own position - {@link
     * org.terasology.engine.world.propagation.light.LightMerger#requiredChunks LightMerger} wants the
     * full 3x3x3. At the edge of the loaded world some of those neighbours were never requested by
     * anyone, so they are in neither the chunk cache nor this pipeline and never will be. Widening
     * the loaded region cannot fix that: the wider region just has its own outer shell with the same
     * problem. There is always a boundary.
     * <p>
     * A blocked position isn't necessarily stuck like that, though - it might just be waiting on a
     * neighbour that was requested moments ago and hasn't finished its own earlier stages yet. Acting
     * the instant a requirement is found missing would wrongly cut that wait short. So this only fires
     * after sustained pipeline-wide silence: if nothing anywhere has completed for multiple poll
     * intervals, whatever's still blocked is not "about to arrive" in any meaningful sense, genuinely
     * unobtainable or not.
     * <p>
     * A skipped chunk is passed through its stage unchanged. For light merging that means its outward
     * faces keep whatever lighting the earlier per-chunk stages produced; when a neighbour is loaded
     * later, that neighbour's own merge propagates across the shared face and corrects it. Being
     * ready with imperfect edge lighting beats never becoming ready at all, which is what happened
     * before - those chunks sat in {@link #chunkProcessingInfoMap} forever, and anything that later
     * needed them (a chunk reload, say) inherited the stall.
     */
    private void skipBlockedStages() {
        consecutiveIdlePolls = 0;
        List<Vector3ic> toSkip = Lists.newArrayList(blockedPositions);
        blockedPositions.clear();
        for (Vector3ic pos : toSkip) {
            ChunkProcessingInfo info = chunkProcessingInfoMap.get(pos);
            if (info != null && info.getCurrentFuture() == null) {
                skipStage(info);
            }
        }
    }

    private void skipStage(ChunkProcessingInfo info) {
        ChunkTaskProvider stage = info.getChunkTaskProvider();
        logger.debug("Skipping stage [{}] for chunk {}: still missing part of its required neighbourhood "
                        + "after {}ms of pipeline-wide idle",
                stage == null ? "?" : stage.getName(), info.getPosition(), POLL_INTERVAL_MS * IDLE_POLLS_BEFORE_SKIP);
        Chunk chunk = info.getChunk();
        ChunkTask passThrough = new SingleChunkTask(
                (stage == null ? "?" : stage.getName()) + " (skipped)", info.getPosition(), UnaryOperator.identity());
        info.setCurrentFuture(runTask(passThrough, Collections.singletonList(chunk)));
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

    private Future<Chunk> runTask(ChunkTask task, List<Chunk> chunks) {
        return chunkProcessor.submit(() -> {
            try (ThreadActivity ignored = ThreadMonitor.startThreadActivity(task.getName())) {
                return task.apply(chunks);
            }
        }, task.getPosition());
    }

    private Thread threadFactory(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName("Chunk-Processing-" + threadIndex++);
        return thread;
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void rejectQueueHandler(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
        logger.error("Cannot run {}  because queue is full", runnable);
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

    /**
     * Run generator task and then run pipeline processing with it.
     * <p>
     * Additionally add technical stages for cleaning pipeline after chunk processing and handles errors in stages.
     *
     * @param generatorTask ChunkTask which provides new chunk to pipeline
     * @return Future of chunk processing.
     */
    public ListenableFuture<Chunk> invokeGeneratorTask(Vector3ic position, Supplier<Chunk> generatorTask) {
        Preconditions.checkState(!stages.isEmpty(), "ChunkProcessingPipeline must to have at least one stage");
        ChunkProcessingInfo chunkProcessingInfo = chunkProcessingInfoMap.get(position);
        if (chunkProcessingInfo != null) {
            return chunkProcessingInfo.getExternalFuture();
        } else {
            SettableFuture<Chunk> exitFuture = SettableFuture.create();
            chunkProcessingInfo = new ChunkProcessingInfo(position, exitFuture);
            chunkProcessingInfoMap.put(position, chunkProcessingInfo);
            chunkProcessingInfo.setCurrentFuture(chunkProcessor.submit(generatorTask::get, position));
            return exitFuture;
        }
    }

    /**
     * Send chunk to processing pipeline. If chunk not processing yet then pipeline will be setted. If chunk processed
     * then chunk will be processing in next stage;
     *
     * @param chunk chunk to process.
     */
    public Future<Chunk> invokePipeline(Chunk chunk) {
        return invokeGeneratorTask(chunk.getPosition(), () -> chunk);
    }

    public void shutdown() {
        executor.shutdown();
        chunkProcessingInfoMap.keySet().forEach(this::stopProcessingAt);
        chunkProcessingInfoMap.clear();
        executor.getQueue().clear();
        reactor.interrupt();
    }

    public void restart() {
        chunkProcessingInfoMap.clear();
        executor.getQueue().clear();
        chunkProcessingInfoMap.keySet().forEach(this::stopProcessingAt);
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

        removed.getExternalFuture().cancel(true);

        Future<Chunk> currentFuture = removed.getCurrentFuture();
        if (currentFuture != null) {
            currentFuture.cancel(true);
        }

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
    public Iterable<Vector3ic> getProcessingPosition() {
        return chunkProcessingInfoMap.keySet();
    }
}
