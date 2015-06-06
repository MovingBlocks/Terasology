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

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terasology.monitoring.ThreadActivity;
import org.terasology.monitoring.ThreadMonitor;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Concrete TaskManager implementation.
 */
public class TaskManagerImpl implements TaskManager {
   private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);


   private final    Lock                        lock = new ReentrantLock();
   private volatile int                         poolSize;
   private volatile ScheduledThreadPoolExecutor service;
   private volatile ScheduledExecutorService    threadPool;


   /**
    * Constructs a task manager and starts its thread pool.
    *
    * @param poolSize The (initial) number of worker threads in the thread pool.
    */
   public TaskManagerImpl(int poolSize) {
      Preconditions.checkArgument(poolSize > 0, "Non-positive pool size %s", poolSize);

      this.poolSize = poolSize;

      startThreads();
   }


   @Override
   public Runnable newActivity(Runnable runnable, String threadName, int threadPriority, String activityName) {
      return new RunnableActivity(runnable, new ActivityInfo(threadName, threadPriority, activityName));
   }

   @Override
   public Runnable newActivity(Task task, String threadName, int threadPriority) {
      return new RunnableActivity(task, new ActivityInfo(threadName, threadPriority, task.getName()));
   }

   @Override
   public <V> Callable<V> newActivity(Callable<V> callable, String threadName, int threadPriority, String activityName) {
      return new CallableActivity(callable, new ActivityInfo(threadName, threadPriority, activityName));
   }

   @Override
   public <V> Callable<V> newActivity(Runnable runnable, V result, String threadName, int threadPriority, String activityName) {
      return new CallableActivity(Executors.callable(runnable, result), new ActivityInfo(threadName, threadPriority, activityName));
   }

   @Override
   public Runnable newActivity(Runnable runnable, String activityName) {
      return newActivity(runnable, null, Thread.NORM_PRIORITY, activityName);
   }

   @Override
   public Runnable newActivity(Task task) {
      return newActivity(task, null, Thread.NORM_PRIORITY);
   }

   @Override
   public <V> Callable<V> newActivity(Callable<V> callable, String activityName) {
      return newActivity(callable, null, Thread.NORM_PRIORITY, activityName);
   }

   @Override
   public <V> Callable<V> newActivity(Runnable runnable, V result, String activityName) {
      return newActivity(runnable, result, null, Thread.NORM_PRIORITY, activityName);
   }

   @Override
   public ScheduledExecutorService getThreadPool() {
      return threadPool;
   }

   @Override
   public int getPoolSize() {
      return poolSize;
   }

   @Override
   public void setPoolSize(int value) {
      Preconditions.checkArgument(value > 0, "Non-positive pool size %s", value);

      lock.lock();
      try {
         poolSize = value;
         if (service != null) {
            service.setCorePoolSize(value);
         }
      } finally {
         lock.unlock();
      }
   }

   @Override
   public void shutdown(long gracefulWaitInMs, long extraWaitInMs) {
      ScheduledThreadPoolExecutor s;

      lock.lock();
      try {
         s = service;
         service    = null;
         threadPool = null;
      } finally {
         lock.unlock();
      }

      if (s != null) {
         final ScheduledThreadPoolExecutor svc = s;

         AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            svc.shutdown();
            try {
               if (!svc.awaitTermination(gracefulWaitInMs, TimeUnit.MILLISECONDS)) {
                  logger.warn("Timed out waiting for completion of scheduled tasks.");

                  svc.shutdownNow();
                  if (!svc.awaitTermination(extraWaitInMs, TimeUnit.MILLISECONDS)) {
                     logger.warn("Timed out awaiting thread termination.");
                  }
               }
            } catch (InterruptedException ex) {
               logger.warn("Thread pool shutdown interrupted.");
               svc.shutdownNow();
               Thread.currentThread().interrupt();
            }

            return null;
         });
      }
   }

   @Override
   public void shutdown() {
      shutdown(DEFAULT_GRACEFUL_SHUTDOWN_TIME_MS,
               DEFAULT_EXTRA_SHUTDOWN_TIME_MS);
   }

   @Override
   public void restart() {
      lock.lock();
      try {
         if (service == null) {
            startThreads();
         }
      } finally {
         lock.unlock();
      }
   }


   private void startThreads() {
      service = new ScheduledThreadPoolExecutor(poolSize);

      service.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
      service.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
      service.setRemoveOnCancelPolicy(true);

      threadPool = Executors.unconfigurableScheduledExecutorService(service);
   }


   /**
    * Keeps track of thread priority, thread name, and thread monitoring for future tasks.
    */
   private static final class ActivityInfo {
      private final String threadName;
      private final int    threadPriority;
      private final String activityName;

      ActivityInfo(String threadName, int threadPriority, String activityName) {
         this.threadName     = threadName;
         this.threadPriority = threadPriority;
         this.activityName   = activityName;
      }
   }

   /**
    * An RAAI class that sets priority and optionally sets thread name and thread monitoring while it is open.
    */
   private static final class Activity implements AutoCloseable {
      private final   Thread         thread;
      private final   String         oldName;
      private final   int            oldPriority;
      private final   ThreadActivity activity;
      private boolean                closed;

      /**
       * Constructs a new activity.
       *
       * @param threadName     The name to give the thread while the task runs, or null to keep the thread's default name.
       * @param threadPriority The priority to assign the thread while the task runs.
       * @param activityName   The name of the activity for monitoring, or null if the task should not be monitored.
       */
      Activity(ActivityInfo info) {
         final String tName = info.threadName;
         final String aName = info.activityName;

         thread      = Thread.currentThread();
         oldName     = (tName != null) ? thread.getName() : null;
         oldPriority = thread.getPriority();
         activity    = (aName != null) ? ThreadMonitor.startThreadActivity(aName) : null;
         closed      = false;

         thread.setPriority(info.threadPriority);
         if (tName != null) {
            thread.setName(tName);
         }
      }

      @Override
      public void close() {
         if (closed) {
            return;
         }

         if (Thread.currentThread() != thread) {
            throw new RuntimeException("Activity closed on wrong thread.");
         }

         activity.close();

         if (oldName != null) {
            thread.setName(oldName);
         }
         thread.setPriority(oldPriority);
      }
   }

   private static final class RunnableActivity implements Runnable {
      private Runnable     runnable;
      private ActivityInfo info;

      RunnableActivity(Runnable runnable, ActivityInfo info) {
         this.runnable = runnable;
         this.info     = info;
      }

      @Override
      public void run() {
         try (Activity raai = new Activity(info)) {
            runnable.run();
         }
      }
   }

   private static final class CallableActivity<V> implements Callable<V> {
      private Callable<V>  callable;
      private ActivityInfo info;

      CallableActivity(Callable<V> callable, ActivityInfo info) {
         this.callable = callable;
         this.info     = info;
      }

      @Override
      public V call() throws Exception {
         try (Activity raai = new Activity(info)) {
            return callable.call();
         }
      }
   }
}
