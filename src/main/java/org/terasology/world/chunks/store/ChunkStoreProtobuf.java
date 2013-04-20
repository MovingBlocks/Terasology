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
import java.io.Serializable;
import java.util.List;
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
import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.Chunks;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

/**
 * Implements a chunk store using protobuf internally. This is just copied and adapted from {@code ChunkStoreGZip}.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 * @see org.terasology.world.chunks.store.ChunkStoreGZip
 *
 */
public class ChunkStoreProtobuf implements ChunkStore, Serializable {
    static final long serialVersionUID = -8168985892342356264L;

    private static final int NUM_DISPOSAL_THREADS = 2;

    private static final Logger logger = LoggerFactory.getLogger(ChunkStoreProtobuf.class);

    private transient ConcurrentMap<Vector3i, Chunk> modifiedChunks;
    private transient ExecutorService compressionThreads = null;
    private transient BlockingQueue<Chunk> compressionQueue;

    private ConcurrentMap<Vector3i, byte[]> serializedChunks = Maps.newConcurrentMap();
    private AtomicInteger sizeInByte = new AtomicInteger(0);
    private AtomicBoolean running = new AtomicBoolean(true);

    protected void setupThreads() {
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

    public ChunkStoreProtobuf() {
        setup();
    }
    
    public void setup() {
        modifiedChunks = Maps.newConcurrentMap();
        compressionQueue = Queues.newLinkedBlockingDeque();
        setupThreads();
    }

    public Chunk get(Vector3i id) {
        Chunk c = modifiedChunks.get(id);
        if (c != null) {
            return new Chunk(c);
        }

        try {
            final byte[] b = serializedChunks.get(id);
            if (b == null) return null;
            final ByteArrayInputStream baIn = new ByteArrayInputStream(b);
            final GZIPInputStream gzIn = new GZIPInputStream(baIn);
            final CodedInputStream cIn = CodedInputStream.newInstance(gzIn);
            final ChunksProtobuf.Chunk message = ChunksProtobuf.Chunk.parseFrom(cIn);
            c = Chunks.getInstance().decode(message); 
        } catch (Exception e) {
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
        return modifiedChunks.containsKey(position) || serializedChunks.containsKey(position);
    }
    
    @Override
    public int list(List<Vector3i> output) {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        final int size = output.size();
        output.addAll(serializedChunks.keySet());
        return output.size() - size;
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
            final ChunksProtobuf.Chunk message = Chunks.getInstance().encode(c);
            final ByteArrayOutputStream baOut = new ByteArrayOutputStream();
            final GZIPOutputStream gzOut = new GZIPOutputStream(baOut);
            final CodedOutputStream cOut = CodedOutputStream.newInstance(gzOut);
            message.writeTo(cOut);
            cOut.flush();
            gzOut.close();
            final byte[] serialized = baOut.toByteArray();
            sizeInByte.addAndGet(serialized.length);
            serializedChunks.put(c.getPos(), serialized);
            modifiedChunks.remove(c.getPos(), c); // TODO Does that actually work???
        } catch (Exception e) {
            logger.error("Failed saving chunk", e);
        }
    }
}
