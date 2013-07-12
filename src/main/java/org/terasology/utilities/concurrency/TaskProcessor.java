/*
 * Copyright 2013 Moving Blocks
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

import java.util.concurrent.BlockingQueue;

/**
 * @author Immortius
 */
final class TaskProcessor<T extends Task> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TaskProcessor.class);

    private BlockingQueue<T> queue;

    public TaskProcessor(BlockingQueue<T> taskQueue) {
        this.queue = taskQueue;
    }

    @Override
    public final void run() {
        boolean running = true;
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        while (running) {
            try {
                T task = queue.take();
                task.enact();
                if (task.isTerminateSignal()) {
                    running = false;
                }
            } catch (InterruptedException e) {
                logger.error("Thread interrupted", e);
            } catch (Exception e) {
                logger.error("Error in thread", e);
            }
        }
        logger.debug("Thread shutdown safely");
    }
}
