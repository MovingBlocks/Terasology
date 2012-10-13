/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.world.chunks.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkStore;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

public class ChunkStoreGZip implements ChunkStore, Serializable {
    static final long serialVersionUID = -8168985892342356264L;

    private static final int NUM_DISPOSAL_THREADS = 2;

    private static final Logger logger = LoggerFactory.getLogger(ChunkStoreGZip.class);

    private transient ConcurrentMap<Vector3i, Chunk> modifiedChunks;
    private transient ExecutorService compressionThreads = null;
    private transient BlockingQueue<Chunk> compressionQueue;

    private ConcurrentMap<Vector3i, byte[]> compressedChunks = Maps.newConcurrentMap();
    private AtomicInteger sizeInByte = new AtomicInteger(0);
    private AtomicBoolean running = new AtomicBoolean(true);

    public static ChunkStoreGZip load(File file) throws IOException {
        FileInputStream fileIn = null;
        ObjectInputStream in = null;
        try {
            fileIn = new FileInputStream(file);
            in = new ObjectInputStream(fileIn);

            ChunkStoreGZip cache = (ChunkStoreGZip) in.readObject();
            cache.compressionQueue = Queues.newLinkedBlockingDeque();
            cache.modifiedChunks = Maps.newConcurrentMap();
            cache.setupThreads();
            return cache;

        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to load chunk cache", e);
        } finally {
            // JAVA7 : cleanup
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("Failed to close input stream", e);
                }
            }
            if (fileIn != null) {
                try {
                    fileIn.close();
                } catch (IOException e) {
                    logger.error("Failed to close input stream", e);
                }
            }
        }
    }

    public ChunkStoreGZip() {
        modifiedChunks = Maps.newConcurrentMap();
        compressionQueue = Queues.newLinkedBlockingDeque();
        setupThreads();
    }

    public void setupThreads() {
        if (compressionThreads == null) {
            running.set(true);
            compressionThreads = Executors.newFixedThreadPool(NUM_DISPOSAL_THREADS);
            for (int i = 0; i < NUM_DISPOSAL_THREADS; ++i) {
                compressionThreads.execute(new Runnable() {
                    @Override
                    public void run() {
                        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                        while (running.get()) {
                            try {
                                Chunk chunk = compressionQueue.poll(500, TimeUnit.MILLISECONDS);
                                if (chunk != null) {
                                    saveChunk(chunk);
                                }

                            } catch (InterruptedException e) {
                                logger.error("Thread interrupted", e);
                            } catch (Exception e) {
                                logger.error("Error in thread", e);
                            }
                        }
                        boolean remaining = true;
                        do {
                            Chunk chunk = compressionQueue.poll();
                            if (chunk != null) {
                                saveChunk(chunk);
                            } else {
                                remaining = false;
                            }
                        } while (remaining);
                        logger.debug("Thread shutdown safely");
                    }
                });
            }
        }
    }

    public Chunk get(Vector3i id) {
        Chunk c;
        c = modifiedChunks.get(id);
        if (c != null) {
            return new Chunk(c);
        }

        try {
            byte[] b = compressedChunks.get(id);
            if (b == null)
                return null;
            ByteArrayInputStream bais = new ByteArrayInputStream(b);
            GZIPInputStream gzipIn = new GZIPInputStream(bais);
            ObjectInputStream objectIn = new ObjectInputStream(gzipIn);
            c = (Chunk) objectIn.readObject();
            objectIn.close();
        } catch (IOException e) {
            logger.error("Error loading chunk", e);
        } catch (ClassNotFoundException e) {
            logger.error("Error loading chunk", e);
        }
        return c;
    }

    public void put(Chunk c) {
        modifiedChunks.put(c.getPos(), c);
        if (!compressionQueue.offer(c)) {
            logger.error("Failed to add chunk to compression queue");
        }
    }

    @Override
    public boolean contains(Vector3i position) {
        return modifiedChunks.containsKey(position) || compressedChunks.containsKey(position);
    }

    public float size() {
        return (float) sizeInByte.get() / (1 << 20);
    }

    public void dispose() {
        running.set(false);
        compressionThreads.shutdown();
        try {
            compressionThreads.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Interrupted while awaiting thread disposal");
        }
    }

    private void saveChunk(Chunk c) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
            ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
            objectOut.writeObject(c);
            objectOut.close();
            byte[] b = baos.toByteArray();
            sizeInByte.addAndGet(b.length);
            compressedChunks.put(c.getPos(), b);
            modifiedChunks.remove(c.getPos(), c);
        } catch (IOException e) {
            logger.error("Error saving chunk", e);
        }
    }
}
