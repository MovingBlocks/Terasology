// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.concurrency;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Manages execution of tasks on a queue.
 *
 * TaskMasters execute Tasks on separate threads, meaning that long running tasks can be performed without affecting
 * rendering or other processing on the main thread. The usual caveats regarding threading and Events, Components,
 * and Entities apply to the Tasks being processed.
 * <p>
 * Create TaskMaster instances using the static "create" helper methods, then use {@link #offer(Task)} to add tasks to
 * the queue. In most cases the simple FIFO TaskMaster is good enough. However, you can create a prioritized queue by
 * implementing {@link Comparable} in your {@link Task} implementations.
 * <p>
 * When you create a TaskMaster, it is important to shut it down after you're finished with it, generally in the
 * shutdown method of a ComponentSystem. A basic usage example follows:
 * <br>
 * <pre>
 * {@literal
 * TaskMaster<MyBaseTask> taskMaster = TaskMaster.createFIFOTaskMaster("MyTaskMaster", 1);
 * taskMaster.offer(new MyFooTask());
 * taskMaster.offer(new MyBarTask());
 * taskMaster.shutdown(new ShutdownTask());
 * }
 * </pre>
 *
 * @see Task
 * @see #createFIFOTaskMaster(String, int)
 * @see #createPriorityTaskMaster(String, int, int)
 */
public final class TaskMaster<T extends Task> {
    private static final Logger logger = LoggerFactory.getLogger(TaskMaster.class);

    private final BlockingQueue<T> taskQueue;
    private ExecutorService executorService;
    private final int threads;
    private boolean running;
    private final String name;

    private TaskMaster(String name, int threads, BlockingQueue<T> queue) {
        this.name = name;
        this.threads = threads;
        if (threads <= 0) {
            throw new IllegalArgumentException("Must have at least one thread.");
        }
        taskQueue = queue;
        restart();
    }

    /**
     * Creates a FIFO taskmaster which simply reads from a task queue in order
     */
    public static <T extends Task> TaskMaster<T> createFIFOTaskMaster(String name, int threads) {
        return new TaskMaster<>(name, threads, new LinkedBlockingQueue<>());
    }

    /**
     * Creates a prioritized taskmaster which uses {@link Comparable} Tasks to establish priority. The <em>least</em>
     * task (according to the Comparable interface) is processed first.
     */
    public static <T extends Task & Comparable<? super T>> TaskMaster<T> createPriorityTaskMaster(String name, int threads, int queueSize) {
        return new TaskMaster<>(name, threads, new PriorityBlockingQueue<>(queueSize));
    }

    public static <T extends Task> TaskMaster<T> createPriorityTaskMaster(String name, int threads, int queueSize, Comparator<T> comparator) {
        return new TaskMaster<>(name, threads, new PriorityBlockingQueue<>(queueSize, comparator));
    }

    /**
     * Offers a task to this task master. This does not block, but may fail if the queue is full.
     *
     * @param task
     * @return Whether the task was successfully added to the queue.
     */
    public boolean offer(T task) {
        return taskQueue.offer(task);
    }

    /**
     * Adds a task to this task master. This blocks until the task can be added if the queue is full.
     *
     * @param task
     */
    public void put(T task) throws InterruptedException {
        taskQueue.put(task);
    }

    public void shutdown(T shutdownTask, boolean awaitComplete) {
        if (!shutdownTask.isTerminateSignal()) {
            throw new IllegalArgumentException("Expected task to provide terminate signal");
        }
        if (!awaitComplete) {
            taskQueue.drainTo(Lists.newArrayList());
        }
        for (int i = 0; i < threads; ++i) {
            try {
                taskQueue.offer(shutdownTask, 250, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Failed to enqueue shutdown request", e);
            }
        }
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(20, TimeUnit.SECONDS)) {
                    logger.warn("Timed out awaiting thread termination");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted awaiting chunk thread termination");
                executorService.shutdownNow();
            }
            return null;
        });
        running = false;
    }

    public void restart() {
        if (!running) {
            executorService = Executors.newFixedThreadPool(threads);
            for (int i = 0; i < threads; ++i) {
                executorService.execute(new TaskProcessor<>(name + "-" + i, taskQueue));
            }
            running = true;
        }
    }

    /**
     * Get the {@link ExecutorService} underlying this TaskMaster. Note that by default the service will have a
     * {@link TaskProcessor} enqueued for each thread. In order to use the ExecutorService directly you will need to
     * {@link TaskMaster#offer(Task)} a {@link ShutdownTask} as shown:
     * <p>
     * {@code taskMaster.offer(new ShutdownTask());}
     * @return the {@link ExecutorService} used by this instance
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }
}
