// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.GameThread;
import org.terasology.engine.monitoring.ThreadActivity;
import org.terasology.engine.monitoring.ThreadMonitor;

import java.util.concurrent.BlockingQueue;

final class TaskProcessor<T extends Task> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TaskProcessor.class);

    private String name;
    private BlockingQueue<T> queue;

     TaskProcessor(String name, BlockingQueue<T> taskQueue) {
        this.queue = taskQueue;
        this.name = name;
    }

    @Override
    public void run() {
        boolean running = true;
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        Thread.currentThread().setName(name);
        while (running) {
            try {
                T task = queue.take();
                try (ThreadActivity ignored = ThreadMonitor.startThreadActivity(task.getName())) {
                    task.run();
                }
                if (task.isTerminateSignal()) {
                    running = false;
                }
            } catch (InterruptedException e) {
                ThreadMonitor.addError(e);
                logger.error("Thread interrupted", e);
            } catch (RuntimeException e) {
                ThreadMonitor.addError(e);
                logger.error("Error in thread {}", Thread.currentThread().getName(), e); //NOPMD
            } catch (Error e) {
                GameThread.asynch(() -> {
                    throw e;  // re-throw on game thread to terminate the entire application
                });
            }
        }
        logger.debug("Thread shutdown safely");
    }
}
