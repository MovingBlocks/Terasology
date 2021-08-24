// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.schedulers;

import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.scheduler.Scheduler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Single thread scheduler which capture thread full.
 * Usage:
 * <pre>
 * {@code
 *        ThreadCaptureScheduler scheduler = new ThreadCaptureScheduler(Thread.currentThread());
 *        scheduler.start(); // Thread will block there.
 *
 *        // many code ....
 *
 *       // somewhere in another thread.
 *        scheduler.dispose(); // there interupts thread and disposes scheduler
 * }
 * </pre>
 */
public class ThreadCaptureScheduler implements ThreadAwareScheduler {

    private static Logger logger = LoggerFactory.getLogger(ThreadCaptureScheduler.class);

    private final Thread capturedThread;
    private AtomicBoolean requestToDispose = new AtomicBoolean(false);
    private BlockingQueue<Runnable> tasks = Queues.newArrayBlockingQueue(200);

    public ThreadCaptureScheduler(Thread capturedThread) {
        this.capturedThread = capturedThread;
    }

    @Override
    public Disposable schedule(Runnable task) {
        Disposable dis = Disposables.single();
        tasks.offer(task);
        return dis;
    }


    @Override
    public Worker createWorker() {
        return new ThreadCapturedWorker(capturedThread);
    }

    @Override
    public void start() {
        while (!capturedThread.isInterrupted() || !requestToDispose.get()) {
            try {
                tasks.take().run();
            } catch (InterruptedException e) {
                if (!requestToDispose.get()) {
                    logger.error("Interrupting captured thread", e);
                }
                return;
            }
        }
    }

    @Override
    public void dispose() {
        requestToDispose.set(true);
        capturedThread.interrupt();
    }

    @Override
    public boolean isSchedulerThread(Thread thread) {
        return capturedThread.equals(thread);
    }

    public class ThreadCapturedWorker implements Worker {

        private final Thread capturedThread;

        public ThreadCapturedWorker(Thread capturedThread) {
            this.capturedThread = capturedThread;
        }

        @Override
        public Disposable schedule(Runnable task) {
            return ThreadCaptureScheduler.this.schedule(task);
        }

        @Override
        public void dispose() {
            // Ignore worker disposing. we have only one thread. it is very important for macos rendering thread.
        }
    }


}
