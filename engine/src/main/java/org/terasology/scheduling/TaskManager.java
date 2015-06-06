/*
 * Copyright 2015 MovingBlocks
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

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;


/**
 * Manages the pool of worker threads used by (short-running) tasks throughout the game.
 */
public interface TaskManager {
   long DEFAULT_GRACEFUL_SHUTDOWN_TIME_MS = 15_000L;
   long DEFAULT_EXTRA_SHUTDOWN_TIME_MS    =  5_000L;


   /**
    * Creates and returns a Runnable that wraps the one given and sets thread priority and optionally thread name and
    * thread activity monitoring while it runs.
    *
    * @param runnable       The wrapped task.  May not be null.
    * @param threadName     The name to give the thread while the task runs, or null to keep the thread's default name.
    * @param threadPriority The priority to assign the thread while the task runs.
    * @param activityName   The name of the activity for monitoring, or null if the task should not be monitored.
    * @return               A new Runnable.
    */
   Runnable newActivity(Runnable runnable, String threadName, int threadPriority, String activityName);

   /**
    * Creates and returns a Runnable that wraps the Task given and sets thread priority, thread activity monitoring,
    * and optionally thread name while it runs.
    *
    * @param task           The wrapped task.  Its name is also used as the thread monitoring acitivity name.  May not be null.
    * @param threadName     The name to give the thread while the task runs, or null to keep the thread's default name.
    * @param threadPriority The priority to assign the thread while the task runs.
    * @return               A new Runnable.
    */
   Runnable newActivity(Task task, String threadName, int threadPriority);

   /**
    * Creates and returns a Callable that wraps the one given and sets thread priority and optionally thread name and
    * thread activity monitoring while it runs.
    *
    * @param callable       The wrapped task.  May not be null.
    * @param threadName     The name to give the thread while the task runs, or null to keep the thread's default name.
    * @param threadPriority The priority to assign the thread while the task runs.
    * @param activityName   The name of the activity for monitoring, or null if the task should not be monitored.
    * @return               A new Callable.
    */
   <V> Callable<V> newActivity(Callable<V> callable, String threadName, int threadPriority, String activityName);

   /**
    * Creates and returns a Callable that wraps the Runnable given, returns the given value when done, and sets
    * thread priority and optionally thread name and thread activity monitoring while it runs.
    *
    * @param runnable       The wrapped task.  May not be null.
    * @param result         The result value to return when done.  May be null.
    * @param threadName     The name to give the thread while the task runs, or null to keep the thread's default name.
    * @param threadPriority The priority to assign the thread while the task runs.
    * @param activityName   The name of the activity for monitoring, or null if the task should not be monitored.
    * @return               A new Callable.
    */
   <V> Callable<V> newActivity(Runnable runnable, V result, String threadName, int threadPriority, String activityName);

   /**
    * Equivalent to {@code newActivity(runnable, null, Thread.NORM_PRIORITY, activityName)}.
    *
    * @param runnable     The wrapped task.  May not be null.
    * @param activityName The name of the activity for monitoring, or null if the task should not be monitored.
    * @return             A new Runnable.
    */
   Runnable newActivity(Runnable runnable, String activityName);

   /**
    * Equivalent to {@code newActivity(task, null, Thread.NORM_PRIORITY)}.
    *
    * @param task The wrapped task.  Its name is also used as the thread monitoring acitivity name.  May not be null.
    * @return     A new Runnable.
    */
   Runnable newActivity(Task task);

   /**
    * Equivalent to {@code newActivity(callable, null, Thread.NORM_PRIORITY, activityName)}.
    *
    * @param callable     The wrapped task.  May not be null.
    * @param activityName The name of the activity for monitoring, or null if the task should not be monitored.
    * @return             A new Callable.
    */
   <V> Callable<V> newActivity(Callable<V> callable, String activityName);

   /**
    * Equivalent to {@code newActivity(runnable, result, null, Thread.NORM_PRIORITY, activityName)}.
    *
    * @param runnable     The wrapped task.  May not be null.
    * @param result       The result value to return when done.  May be null.
    * @param activityName The name of the activity for monitoring, or null if the task should not be monitored.
    * @return             A new Callable.
    */
   <V> Callable<V> newActivity(Runnable runnable, V result, String activityName);

   /**
    * Returns the ScheduledExecutorService thread pool for executing short-lived tasks and scheduling delayed or recurring tasks.
    *
    * @return The current ScheduledExecutorService.
    */
   ScheduledExecutorService getThreadPool();

   /**
    * Returns the number of worker threads allocated to the thread pool.
    *
    * @return the number of worker threads allocated to the thread pool.
    */
   int getPoolSize();

   /**
    * Sets the number of worker threads allocated to the (current or next allocated) thread pool.
    *
    * @param value The new number of worker threads.  Must be positive.
    */
   void setPoolSize(int value);

   /**
    * Shuts down worker threads and removes the thread pool.
    *
    * @param gracefulWaitInMs The time (in milliseconds) to wait for currently scheduled tasks to be completed and the worker threads to terminate.
    * @param extraWaitInMs    The time (in milliseconds) to wait for threads to terminate after f
    */
   void shutdown(long gracefulWaitInMs, long extraWaitInMs);

   /**
    * Equivalent to {@link #shutdown(long, long) shutdown(DEFAULT_GRACEFUL_SHUTDOWN_TIME_MS, DEFAULT_EXTRA_SHUTDOWN_TIME_MS)}.
    */
   void shutdown();

   /**
    * Creates a new thread pool and allows task execution to resume using new worker threads.
    */
   void restart();
}
