// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core;

import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.scheduler.Scheduler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadCaptureScheduler implements Scheduler {

    private static Logger logger = LoggerFactory.getLogger(ThreadCaptureScheduler.class);

    private Thread capturedThread;
    private AtomicBoolean requestToDispose = new AtomicBoolean(false);
    private BlockingQueue<Runnable> tasks = Queues.newArrayBlockingQueue(200);

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


    public Thread getCapturedThread() {
        return capturedThread;
    }

    public ThreadCaptureScheduler setCapturedThread(Thread capturedThread) {
        this.capturedThread = capturedThread;
        return this;
    }

    @Override
    public void dispose() {
        requestToDispose.set(true);
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
