// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.schedulers;

import com.google.common.collect.Queues;
import reactor.core.Disposable;
import reactor.core.Disposables;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Single thread scheduler with external `tick` source.
 * Creates especially for TeraEd (awt's runLater loop)
 */
public class ExternalTickScheduler implements ThreadAwareScheduler {

    private final Worker worker = new ExternalTickWorker();
    private final Thread workerThread;
    private Queue<Runnable> tasks = Queues.newConcurrentLinkedQueue();

    public ExternalTickScheduler(Thread workerThread) {
        this.workerThread = workerThread;
    }

    @Override
    public Disposable schedule(Runnable task) {
        Disposable dis = Disposables.single();
        tasks.offer(task);
        return dis;
    }

    public void processTasks() {
        List<Runnable> thisTickTasks = new ArrayList<>(tasks);
        for (Runnable runnable : thisTickTasks) {
            runnable.run();
        }
    }

    @Override
    public Worker createWorker() {
        return worker;
    }

    @Override
    public boolean isSchedulerThread(Thread thread) {
        return workerThread.equals(thread);
    }

    private class ExternalTickWorker implements Worker {

        @Override
        public Disposable schedule(Runnable task) {
            return ExternalTickScheduler.this.schedule(task);
        }

        @Override
        public void dispose() {
            // Nothing to dispose
        }
    }
}
