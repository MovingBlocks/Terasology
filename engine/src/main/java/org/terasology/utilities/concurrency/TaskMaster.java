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

package org.terasology.utilities.concurrency;

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
 */
public final class TaskMaster<T extends Task> {
    private static final Logger logger = LoggerFactory.getLogger(TaskMaster.class);

    private BlockingQueue<T> taskQueue;
    private ExecutorService executorService;
    private int threads;
    private boolean running;
    private String name;

    private TaskMaster(String name, int threads, BlockingQueue<T> queue) {
        this.name = name;
        this.threads = threads;
        if (threads <= 0) {
            throw new IllegalArgumentException("Must have at least one thread.");
        }
        taskQueue = queue;
        restart();
    }

    public static <T extends Task> TaskMaster<T> createFIFOTaskMaster(String name, int threads) {
        return new TaskMaster<>(name, threads, new LinkedBlockingQueue<>());
    }

    public static <T extends Task & Comparable<? super T>> TaskMaster<T> createPriorityTaskMaster(String name, int threads, int queueSize) {
        return new TaskMaster<>(name, threads, new PriorityBlockingQueue<>(queueSize));
    }

    public static <T extends Task> TaskMaster<T> createPriorityTaskMaster(String name, int threads, int queueSize, Comparator<T> comparator) {
        return new TaskMaster<>(name, threads, new PriorityBlockingQueue<>(queueSize, comparator));
    }

    public static <T extends Task> TaskMaster<T> createDynamicPriorityTaskMaster(String name, int threads, Comparator<T> comparator) {
        return new TaskMaster<>(name, threads, new DynamicPriorityBlockingQueue<>(comparator));
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

}
