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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.GameThread;
import org.terasology.monitoring.ThreadActivity;
import org.terasology.monitoring.ThreadMonitor;

import java.util.concurrent.BlockingQueue;

/**
 */
final class TaskProcessor<T extends Task> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TaskProcessor.class);

    private String name;
    private BlockingQueue<T> queue;

    public TaskProcessor(String name, BlockingQueue<T> taskQueue) {
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
                logger.error("Error in thread {}", Thread.currentThread().getName(), e);
            } catch (Error e) {
                GameThread.asynch(() -> {
                    throw e;  // re-throw on game thread to terminate the entire application
                });
            }
        }
        logger.debug("Thread shutdown safely");
    }
}
