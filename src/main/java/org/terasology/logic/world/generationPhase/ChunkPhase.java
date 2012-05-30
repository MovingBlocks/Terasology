/*
 * Copyright 2012
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

package org.terasology.logic.world.generationPhase;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.terasology.math.Vector3i;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius
 */
public abstract class ChunkPhase {
    private Logger logger = Logger.getLogger(getClass().getName());

    private final AtomicBoolean running = new AtomicBoolean(true);
    private Set<Vector3i> processing = Sets.newHashSet();
    private BlockingQueue<Vector3i> queue;
    private ConcurrentLinkedQueue<Vector3i> completed = Queues.newConcurrentLinkedQueue();
    private ExecutorService threads;

    public ChunkPhase(int numThreads, Comparator<Vector3i> chunkRelevanceComparator) {
        queue = new PriorityBlockingQueue<Vector3i>(128, chunkRelevanceComparator);
        threads = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; ++i) {
            threads.execute(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    while (running.get()) {
                        try {
                            Vector3i pos = queue.poll(500, TimeUnit.MILLISECONDS);
                            if (pos != null) {
                                process(pos);
                                completed.add(pos);
                            }
                        } catch (InterruptedException e) {
                            logger.log(Level.SEVERE, "Thread interrupted", e);
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Error in thread", e);
                        }
                    }
                    logger.log(Level.INFO, "Thread shutdown safely");
                }
            });
        }
    }

    protected abstract void process(Vector3i pos);

    public void dispose() {
        running.set(false);
        threads.shutdown();
        try {
            threads.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interrupted while attempting clean shutdown", e);
        }
        processing.clear();
        completed.clear();
        queue.clear();
    }

    public boolean isResultAvailable() {
        return !completed.isEmpty();
    }

    public boolean processing(Vector3i pos) {
        return processing.contains(pos);
    }

    public void queue(Vector3i pos) {
        if (processing.add(pos)) {
            if (!queue.offer(pos)) {
                logger.severe("Failed to queue chunk: " + pos);
            }
        }
    }

    public Vector3i poll() {
        Vector3i result = completed.poll();
        if (result != null) {
            processing.remove(result);
        }
        return result;
    }


}
