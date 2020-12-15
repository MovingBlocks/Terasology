// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.SettableFuture;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.monitoring.ThreadActivity;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.utilities.ReflectionUtil;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.pipeline.stages.ChunkTask;
import org.terasology.world.chunks.pipeline.stages.ChunkTaskProvider;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Manages execution of chunk processing.
 * <p>
 * {@link Chunk}s will processing on stages {@link ChunkProcessingPipeline#addStage}
 */
public class ChunkProcessingPipeline {

    private static final int NUM_TASK_THREADS = 4;
    private static final Logger logger = LoggerFactory.getLogger(ChunkProcessingPipeline.class);

    private final List<ChunkTaskProvider> stages = Lists.newArrayList();
    private final Thread reactor;
    private final CompletionService<Chunk> chunkProcessor;
    private final ThreadPoolExecutor executor;
    private final Function<Vector3ic, Chunk> chunkProvider;
    private final Map<Vector3ic, ChunkProcessingInfo> chunkProcessingInfoMap = Maps.newConcurrentMap();
    private int threadIndex;

    /**
     * Create ChunkProcessingPipeline.
     */
    public ChunkProcessingPipeline(Function<Vector3ic, Chunk> chunkProvider, Comparator<Future<Chunk>> comparable) {
        this.chunkProvider = chunkProvider;

        executor = new ThreadPoolExecutor(
                NUM_TASK_THREADS,
                NUM_TASK_THREADS, 0L,
                TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue(800, unwrappingComporator(comparable)),
                this::threadFactory,
                this::rejectQueueHandler) {
            @Override
            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
                RunnableFuture<T> newTaskFor = super.newTaskFor(callable);
                return new PositionFuture<>(newTaskFor, ((PositionalCallable) callable).getPosition());
            }
        };
        chunkProcessor = new ExecutorCompletionService<>(executor,
                new PriorityBlockingQueue<>(800, comparable));
        reactor = new Thread(this::chunkTaskHandler);
        reactor.setDaemon(true);
        reactor.setName("Chunk-Processing-Reactor");
        reactor.start();
    }

    /**
     * BlackMagic method: {@link ExecutorCompletionService} wraps task with QueueingFuture (private access)
     * there takes wrapped task for comparing in {@link ThreadPoolExecutor}
     */
    private Comparator unwrappingComporator(Comparator<Future<Chunk>> comparable) {
        return (o1, o2) -> {
                Object unwrapped1 = ReflectionUtil.readField(o1, "task");
                Object unwrapped2 = ReflectionUtil.readField(o2, "task");
                return comparable.compare((Future<Chunk>) unwrapped1, (Future<Chunk>) unwrapped2);
        };
    }

    /**
     * Reactor thread. Handles all ChunkTask dependency logic and running.
     */
    private void chunkTaskHandler() {
        try {
            while (!executor.isTerminated()) {
                PositionFuture<Chunk> future = (PositionFuture<Chunk>) chunkProcessor.take();
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
            } else {
                // haven't next stage
                chunkProcessingInfo.endProcessing();
                cleanup(chunkProcessingInfo);
            }
            processChunkTasks();

        } catch (ExecutionException e) {
            String stageName =
                    chunkProcessingInfo.getChunkTaskProvider() == null
                            ? "Generation or Loading"
                            : chunkProcessingInfo.getChunkTaskProvider().getName();
            logger.error(
                    String.format("ChunkTask at position %s and stage [%s] catch error: ",
                            chunkProcessingInfo.getPosition(), stageName),
                    e);
            chunkProcessingInfo.getExternalFuture().setException(e);
        }
    }

    private void processChunkTasks() {
        chunkProcessingInfoMap.values().stream()
                .filter(chunkProcessingInfo -> chunkProcessingInfo.getChunkTask() != null && chunkProcessingInfo.getCurrentFuture() == null)
                .forEach(chunkProcessingInfo -> {
                    ChunkTask chunkTask = chunkProcessingInfo.getChunkTask();
                    Set<Chunk> providedChunks = chunkTask.getRequirements().stream()
                            .map(pos -> getChunkBy(chunkProcessingInfo.getChunkTaskProvider(), pos))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    if (providedChunks.size() == chunkTask.getRequirements().size()) {
                        chunkProcessingInfo.setCurrentFuture(runTask(chunkTask, providedChunks));
                    }
                });
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

    private Future<Chunk> runTask(ChunkTask task, Set<Chunk> chunks) {
        return chunkProcessor.submit(new PositionalCallable(() -> {
            try (ThreadActivity ignored = ThreadMonitor.startThreadActivity(task.getName())) {
                return task.apply(chunks);
            }
        }, task.getPosition()));
    }

    private Thread threadFactory(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName("Chunk-Processing-" + threadIndex++);
        return thread;
    }

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
    public Future<Chunk> invokeGeneratorTask(Vector3i position, Supplier<Chunk> generatorTask) {
        Preconditions.checkState(!stages.isEmpty(), "ChunkProcessingPipeline must to have at least one stage");
        ChunkProcessingInfo chunkProcessingInfo = chunkProcessingInfoMap.get(position);
        if (chunkProcessingInfo != null) {
            return chunkProcessingInfo.getExternalFuture();
        } else {
            SettableFuture<Chunk> exitFuture = SettableFuture.create();
            chunkProcessingInfo = new ChunkProcessingInfo(position, exitFuture);
            chunkProcessingInfoMap.put(position, chunkProcessingInfo);
            chunkProcessingInfo.setCurrentFuture(chunkProcessor.submit(new PositionalCallable(generatorTask::get,
                    position)));
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
        return invokeGeneratorTask(chunk.getPosition(new Vector3i()), () -> chunk);
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
    public List<Vector3ic> getProcessingPosition() {
        return new LinkedList<>(chunkProcessingInfoMap.keySet());
    }

    /**
     * Dummy callable for passthru position for {@link java.util.concurrent.ThreadPoolExecutor}#newTaskFor
     */
    private static final class PositionalCallable implements Callable<Chunk> {
        private final Callable<Chunk> callable;
        private final Vector3ic position;

        private PositionalCallable(Callable<Chunk> callable, Vector3ic position) {
            this.callable = callable;
            this.position = position;
        }

        public Vector3ic getPosition() {
            return position;
        }

        @Override
        public Chunk call() throws Exception {
            return callable.call();
        }
    }
}
