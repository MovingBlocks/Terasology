// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.world.chunks.Chunk;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A specialised alternative to {@link java.util.concurrent.ExecutorCompletionService},
 * used for submitting chunk tasks and queuing their results.
 *
 * Whilst this class adheres to the {@link CompletionService} interface, use of the class's
 * {@link #submit(Callable, Vector3ic)} overload is preferred over those inherited from the interface.
 */
public class ChunkExecutorCompletionService implements CompletionService<Chunk> {
    private static final Vector3ic EMPTY_VECTOR3I = new Vector3i();
    private final ThreadPoolExecutor threadPoolExecutor;
    private final BlockingQueue<Future<Chunk>> completionQueue;

    public ChunkExecutorCompletionService(ThreadPoolExecutor threadPoolExecutor, BlockingQueue<Future<Chunk>> completionQueue) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.completionQueue = completionQueue;
    }

    /**
     * Submits a task to be executed.
     * @param callable the task to submit
     *
     * @deprecated Use {@link #submit(Callable, Vector3ic)} instead
     */
    @Override
    @Deprecated
    public Future<Chunk> submit(Callable<Chunk> callable) {
        RunnableFuture<Chunk> task = new ChunkFutureWithCompletion(callable, EMPTY_VECTOR3I);
        threadPoolExecutor.execute(task);
        return task;
    }

    /**
     * Submits a chunk task to be executed.
     * @param callable the chunk task to execute.
     * @param position the position of the chunk.
     * @return the submitted task.
     */
    public Future<Chunk> submit(Callable<Chunk> callable, Vector3ic position) {
        RunnableFuture<Chunk> task = new ChunkFutureWithCompletion(callable, position);
        threadPoolExecutor.execute(task);
        return task;
    }

    /**
     * Submits a task to be executed.
     * @param runnable the task to run.
     * @param value the value to return upon task completion.
     *
     * @deprecated Use {@link #submit(Callable, Vector3ic)} instead
     */
    @Override
    @Deprecated
    public Future<Chunk> submit(Runnable runnable, Chunk value) {
        RunnableFuture<Chunk> task = new ChunkFutureWithCompletion(runnable, value, EMPTY_VECTOR3I);
        threadPoolExecutor.execute(task);
        return task;
    }

    /**
     * Retrieves a completed task from the queue.
     * @return a completed task.
     * @throws InterruptedException if interrupted whilst waiting on the queue.
     */
    @Override
    public Future<Chunk> take() throws InterruptedException {
        return completionQueue.take();
    }

    /**
     * Retrieves a completed task from the queue if not empty.
     * @return a completed task, or null if there are no tasks in the queue.
     */
    @Override
    public Future<Chunk> poll() {
        return completionQueue.poll();
    }

    /**
     * Retrieves a completed task from the queue if not empty.
     * @param l the timeout duration before returning null.
     * @param timeUnit the time units of the timeout duration.
     *
     * @return a completed task, or null if there are no tasks in the queue.
     */
    @Override
    public Future<Chunk> poll(long l, TimeUnit timeUnit) throws InterruptedException {
        return completionQueue.poll(l, timeUnit);
    }

    private final class ChunkFutureWithCompletion extends PositionFuture<Chunk> {
        ChunkFutureWithCompletion(Callable callable, Vector3ic position) {
            super(callable, position);
        }

        ChunkFutureWithCompletion(Runnable runnable, Chunk result, Vector3ic position) {
            super(runnable, result, position);
        }

        @Override
        protected void done() {
            super.done();
            try {
                completionQueue.put(this);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
