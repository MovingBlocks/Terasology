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

package org.terasology.scheduling;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.concurrency.DynamicPriorityBlockingQueue;

import java.util.Comparator;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;


/** Manages the submission of tasks for a specific purpose.  While this class is not directly responsible for the number of worker
  * threads executing (see {@link TaskManager}), it does limit the number of concurrent tasks it submits to those workers, conserving
  * resources and allowing its tasks to be prioritized relative to each other.
  *
  * @param T
  *    The subclass of {@link Task} managed by the object.
  **/
public final class TaskMaster<T extends Task> {
    /** The default amount of time (in milliseconds) to wait for tasks to complete on shutdown.
      **/
    public static final long DEFAULT_COMPLETION_WAIT_TIME_MS = 20_000L;

    private static final Logger logger = LoggerFactory.getLogger(TaskMaster.class);


    private final TaskManager taskManager = CoreRegistry.get(TaskManager.class);
    private final Lock        lock        = new ReentrantLock();
    private final Condition   notFull     = lock.newCondition();
    private final Condition   tasksDone   = lock.newCondition();
    private       boolean     shuttingDown;
    private       int         generation;

    private final String               name;
    private final int                  submitLimit;
    private final int                  threadPriority;
    private final Deque<String>        processorNames;
    private final BlockingQueue<T>     taskQueue;
    private final Map<Work, Future<?>> submittedTasks = Maps.newHashMap();


    private TaskMaster(String name, int submitLimit, BlockingQueue<T> taskQueue, int threadPriority) {
        Preconditions.checkArgument(submitLimit > 0, "Must have at least one thread.");

        this.name           = name;
        this.submitLimit    = submitLimit;
        this.threadPriority = threadPriority;
        this.taskQueue      = taskQueue;

        processorNames = Queues.newArrayDeque();
        initProcessorNames();

        taskManager.restart();
    }

    public static <T extends Task>
    TaskMaster<T> createFIFOTaskMaster(String name, int threads, int threadPriority) {
        return new TaskMaster<>(name, threads, Queues.newLinkedBlockingQueue(), threadPriority);
    }

    public static <T extends Task>
    TaskMaster<T> createFIFOTaskMaster(String name, int threads) {
        return new TaskMaster<>(name, threads, Queues.newLinkedBlockingQueue(), Thread.NORM_PRIORITY);
    }

    public static <T extends Task & Comparable<? super T>>
    TaskMaster<T> createPriorityTaskMaster(String name, int threads, int queueSize, int threadPriority) {
        return new TaskMaster<>(name, threads, new PriorityBlockingQueue<>(queueSize), threadPriority);
    }

    public static <T extends Task & Comparable<? super T>>
    TaskMaster<T> createPriorityTaskMaster(String name, int threads, int queueSize) {
        return new TaskMaster<>(name, threads, new PriorityBlockingQueue<>(queueSize), Thread.NORM_PRIORITY);
    }

    public static <T extends Task>
    TaskMaster<T> createPriorityTaskMaster(String name, int threads, int queueSize, Comparator<T> comparator, int threadPriority) {
        return new TaskMaster<>(name, threads, new PriorityBlockingQueue<T>(queueSize, comparator), threadPriority);
    }

    public static <T extends Task>
    TaskMaster<T> createPriorityTaskMaster(String name, int threads, int queueSize, Comparator<T> comparator) {
        return new TaskMaster<>(name, threads, new PriorityBlockingQueue<T>(queueSize, comparator), Thread.NORM_PRIORITY);
    }

    public static <T extends Task>
    TaskMaster<T> createDynamicPriorityTaskMaster(String name, int threads, Comparator<T> comparator, int threadPriority) {
        return new TaskMaster<>(name, threads, new DynamicPriorityBlockingQueue<T>(comparator), threadPriority);
    }

    public static <T extends Task>
    TaskMaster<T> createDynamicPriorityTaskMaster(String name, int threads, Comparator<T> comparator) {
        return new TaskMaster<>(name, threads, new DynamicPriorityBlockingQueue<T>(comparator), Thread.NORM_PRIORITY);
    }


    /**
     * Offers a task to this task master. This does not block, but may fail if the queue is full.
     *
     * @param task
     * @return Whether the task was successfully added to the queue.
     */
    public boolean offer(T task) {
        final ExecutorService threadPool = taskManager.getThreadPool();
        if (threadPool == null) {
            return false;
        }

        lock.lock();
        try {
            if (shuttingDown) {
                return false;
            }

            if (!taskQueue.offer(task)) {
               return false;
            }

            final String procName = processorNames.pollFirst();
            if (procName != null) {
                final Work w = new Work(taskQueue.remove(), procName);
                submittedTasks.put(w, threadPool.submit(w));
            }

            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds a task to this task master. This blocks until the task can be added if the queue is full.
     *
     * @param task
     */
    public void put(T task) throws InterruptedException {
        final ExecutorService threadPool = taskManager.getThreadPool();
        Preconditions.checkState(threadPool != null, "Threads aren't running");

        lock.lockInterruptibly();
        try {
            if (shuttingDown) {
                throw new RejectedExecutionException("TaskMaster is shutting down");
            }

            while (!taskQueue.offer(task)) {
               notFull.await();
            }

            final String procName = processorNames.pollFirst();
            if (procName != null) {
                final Work w = new Work(taskQueue.remove(), procName);
                submittedTasks.put(w, threadPool.submit(w));
            }
        } finally {
            lock.unlock();
        }
    }

    /** Orderly shutdown of submitted tasks.
      *
      * @param waitTimeInMs
      *     If positive, waits this many milliseconds for tasks to finish executing.  Warns but completes normally on timeout.
      * @param waitForScheduledOnly
      *     If true, all submitted tasks not already scheduled in the thread pool are discarded.
      **/
    public void shutdown(long waitTimeInMs, boolean waitForScheduledOnly) {
        long waitTimeInNs = TimeUnit.MILLISECONDS.toNanos(waitTimeInMs);

        lock.lock();
        try {
            if (shuttingDown) {
                if (!waitForScheduledOnly) {
                    taskQueue.clear();
                }
                try {
                    while (waitTimeInNs > 0L && shuttingDown) {
                        waitTimeInNs = tasksDone.awaitNanos(waitTimeInNs);
                    }
                    if (shuttingDown) {
                        logger.warn("Timed out in secondary task shutdown wait ({})", name);
                    }
                } catch (InterruptedException ex) {
                    logger.warn("Interrupted in secondary task shutdown wait ({})", name);
                }
                return;
            }

            shuttingDown = true;
            ++generation;

            if (waitForScheduledOnly) {
                taskQueue.clear();
            }

            try {
                while (waitTimeInNs > 0L && (!submittedTasks.isEmpty() || !taskQueue.isEmpty())) {
                    waitTimeInNs = tasksDone.awaitNanos(waitTimeInNs);
                }
                if (!submittedTasks.isEmpty() || !taskQueue.isEmpty()) {
                    logger.warn("Timed out awaiting task processing completion ({})", name);
                }
            } catch (InterruptedException ex) {
                logger.error("Interrupted awaiting task processing completion", ex);
            }

            for (Future<?> f : submittedTasks.values()) {
                f.cancel(true);
            }

            processorNames.clear();
            taskQueue.clear();
            submittedTasks.clear();

            shuttingDown = false;

            tasksDone.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /** Equivalent to {@code shutdown(DEFAULT_COMPLETION_WAIT_TIME_MS, waitForScheduledOnly)}.
      **/
    public void shutdown(boolean awaitComplete, boolean waitForScheduledOnly) {
        shutdown((awaitComplete) ? DEFAULT_COMPLETION_WAIT_TIME_MS : 0L, waitForScheduledOnly);
    }

    /** Equivalent to {@code shutdown(waitComplete, false)}.
      **/
    public void shutdown(boolean awaitComplete) {
        shutdown((awaitComplete) ? DEFAULT_COMPLETION_WAIT_TIME_MS : 0L, false);
    }

    public void restart() {
        lock.lock();
        try {
            try {
                while (shuttingDown) {
                    tasksDone.await();
                }
            } catch (InterruptedException ex) {
                throw new IllegalStateException("Task manager restart iterrupted while still shutting down (" + name + ")", ex);
            }

            initProcessorNames();
            scheduleNext(null);
        } finally {
            lock.unlock();
        }
    }


    private void initProcessorNames() {
        for (int i = 0; i < submitLimit; ++i) {
           processorNames.addLast(name + "-" + i);
        }
    }

    /** Guaranteed scheduling of a task if one is already in the task queue.
      * The lock must already be held.
      *
      * @param processorName
      *    The thread (processor) name being recycled from a task that is just
      *    completing, or null if there is none.
      **/
    private void scheduleNext(String processorName) {
        final ExecutorService threadPool = taskManager.getThreadPool();
        if (threadPool == null) {
            return;
        }

        String procName = processorName;
        if (procName == null) {
           procName = processorNames.pollFirst();
           if (procName == null) {
               return;
           }
        }

        final Task task = taskQueue.poll();
        if (task == null) {
            processorNames.addFirst(procName);
            return;
        }
        notFull.signal();

        final Work w = new Work(task, procName);
        submittedTasks.put(w, threadPool.submit(w));
    }


    private class Work implements Runnable {
        final String   threadName;
        final Runnable activity;
        final int      gen;

        Work(Task task, String threadName) {
            this.threadName = threadName;
            activity        = taskManager.newActivity(task, threadName, threadPriority);
            this.gen        = generation;
        }

        @Override
        public void run() {
            try {
               activity.run();
            } finally {
               lock.lock();
               try {
                   submittedTasks.remove(this);

                   if (!shuttingDown && gen == generation) {
                       scheduleNext(threadName);
                   }

                   if (submittedTasks.isEmpty()) {
                       tasksDone.signalAll();
                   }
               } finally {
                   lock.unlock();
               }
            }
        }
    }
}
