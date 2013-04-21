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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;
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
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.monitoring.impl.SingleThreadMonitor;
import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.protobuf.ChunksProtobuf.CompressedChunks;
import org.terasology.protobuf.ChunksProtobuf.CompressedChunks.CompressedChunk;
import org.terasology.protobuf.ChunksProtobuf.CompressedChunks.CompressionMethod;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.Chunks;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

/**
 * ChunkStoreProtobuf implements a compressed storage for chunks.
 * <p/>
 * Chunks are serialized using protobuf and compressed using GZIP. Serialization and compression are delegated to background threads, 
 * whereas decompression and deserialization happen on retrieval in the calling thread.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public class ChunkStoreProtobuf implements ChunkStore, Serializable {
    static final long serialVersionUID = -8168985892342356264L;

    private static final int NUM_DISPOSAL_THREADS = 2;

    private static final Logger logger = LoggerFactory.getLogger(ChunkStoreProtobuf.class);

    private transient boolean initialized = false;
    private transient ConcurrentMap<Vector3i, Chunk> queuedChunks;
    private transient BlockingQueue<Chunk> compressionQueue;
    private transient ExecutorService compressionThreads;
    private transient final AtomicInteger finishedThreads = new AtomicInteger(0);

    private final ConcurrentMap<Vector3i, byte[]> serializedChunks = Maps.newConcurrentMap();
    private final AtomicInteger sizeInBytes = new AtomicInteger(0);
    private final AtomicBoolean running = new AtomicBoolean(true);

    protected Chunk decode(byte[] data) throws IOException {
        Preconditions.checkNotNull(data, "The parameter 'data' must not be null");
        final ByteArrayInputStream baIn = new ByteArrayInputStream(data);
        final GZIPInputStream gzIn = new GZIPInputStream(baIn);
        final CodedInputStream cIn = CodedInputStream.newInstance(gzIn);
        final ChunksProtobuf.Chunk message = ChunksProtobuf.Chunk.parseFrom(cIn);
        return Chunks.getInstance().decode(message); 
    }
    
    protected byte[] encode(Chunk chunk) throws IOException {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        final ChunksProtobuf.Chunk message = Chunks.getInstance().encode(chunk);
        final ByteArrayOutputStream baOut = new ByteArrayOutputStream();
        final GZIPOutputStream gzOut = new GZIPOutputStream(baOut);
        final CodedOutputStream cOut = CodedOutputStream.newInstance(gzOut);
        message.writeTo(cOut);
        cOut.flush();
        gzOut.close();
        return baOut.toByteArray();
    }
    
    protected void saveChunk(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        final Vector3i pos = chunk.getPos();
        try {
            try {
                final byte[] data = encode(chunk);
                sizeInBytes.addAndGet(data.length);
                serializedChunks.put(pos, data);
            } finally {
                queuedChunks.remove(pos, chunk);
            }
        } catch (Exception e) {
            logger.error("Failed saving chunk {}", e, pos);
            throw new RuntimeException("Failed saving chunk " + pos);
        }
    }

    protected void setupThreads() {
        if (compressionThreads == null) {
            running.set(true);
            compressionThreads = Executors.newFixedThreadPool(NUM_DISPOSAL_THREADS);
            for (int i = 0; i < NUM_DISPOSAL_THREADS; ++i) {
                compressionThreads.execute(new Runnable() {
                    @Override
                    public void run() {
                        final SingleThreadMonitor monitor = ThreadMonitor.create("Terasology.Chunks.Storage", "Stored Chunks");
                        try {
                            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                            while (running.get()) {
                                try {
                                    Chunk chunk = compressionQueue.poll(500, TimeUnit.MILLISECONDS);
                                    if (chunk != null) {
                                        saveChunk(chunk);
                                        monitor.increment(0);
                                    }
                                } catch (InterruptedException e) {
                                    monitor.addError(e);
                                    logger.error("Thread interrupted", e);
                                } catch (Exception e) {
                                    monitor.addError(e);
                                    logger.error("Error in thread", e);
                                }
                            }
                            boolean remaining = true;
                            do {
                                Chunk chunk = compressionQueue.poll();
                                if (chunk != null) {
                                    try {
                                        saveChunk(chunk);
                                        monitor.increment(0);
                                    } catch (Exception e) {
                                        monitor.addError(e);
                                        logger.error("Error in thread", e);
                                    }
                                } else
                                    remaining = false;
                            } while (remaining);
                            logger.debug("Thread shutdown safely");
                        } finally {
                            finishedThreads.incrementAndGet();
                            monitor.setActive(false);
                        }
                    }
                });
            }
        }
    }

    public ChunkStoreProtobuf(boolean setup) {
        if (setup) setup();
    }
    
    public ChunkStoreProtobuf() {
        setup();
    }
    
    /**
     * CompressedProtobufHandler implements support for encoding/decoding the entire chunk store into/from protobuf messages.
     * Chunks are compressed using GZIP.
     *
     * @author Manuel Brotz <manu.brotz@gmx.ch>
     */
    public static class ProtobufHandler implements org.terasology.io.ProtobufHandler<ChunkStoreProtobuf, CompressedChunks> {

        @SuppressWarnings("serial")
        public static class ChunkStoreNotReadyException extends RuntimeException {
            public ChunkStoreNotReadyException(String message) {
                super(message);
            }
        }
        
        @Override
        public CompressedChunks encode(ChunkStoreProtobuf chunks) {
            Preconditions.checkNotNull(chunks, "The parameter 'chunks' must not be null");
            if (chunks.finishedThreads.get() != NUM_DISPOSAL_THREADS) {
                logger.error("The chunk store is not ready to be encoded");
                throw new ChunkStoreNotReadyException("The chunk store is not ready to be encoded");
            }
            final CompressedChunks.Builder chunksBuilder = CompressedChunks.newBuilder();
            chunksBuilder.setMethod(CompressionMethod.GZIP);
            for (final Entry<Vector3i, byte[]> chunk : chunks.serializedChunks.entrySet()) {
                final Vector3i key = chunk.getKey();
                final CompressedChunk.Builder chunkBuilder = CompressedChunk.newBuilder();
                chunkBuilder.setX(key.x);
                chunkBuilder.setY(key.y);
                chunkBuilder.setZ(key.z);
                chunkBuilder.setData(ByteString.copyFrom(chunk.getValue()));
                chunksBuilder.addChunks(chunkBuilder.build());
            }
            return chunksBuilder.build();
        }

        @Override
        public ChunkStoreProtobuf decode(CompressedChunks message) {
            final ChunkStoreProtobuf store = new ChunkStoreProtobuf(false);
            decode(message, store);
            return store;
        }

        @Override
        public void decode(CompressedChunks message, ChunkStoreProtobuf store) {
            Preconditions.checkNotNull(message, "The parameter 'message' must not be null");
            Preconditions.checkNotNull(store, "The parameter 'store' must not be null");
            if (store.initialized) throw new IllegalStateException("The chunk store has already been initialized");
            if (!message.hasMethod())
                throw new IllegalArgumentException("Illformed protobuf message. Missing compression method");
            if (message.getMethod() != CompressionMethod.GZIP)
                throw new IllegalArgumentException("Unsupported compression method: " + message.getMethod());
            for (final CompressedChunk chunk : message.getChunksList()) {
                if (!chunk.hasX())
                    throw new IllegalArgumentException("Illformed protobuf message. Missing x-coordinate");
                if (!chunk.hasY())
                    throw new IllegalArgumentException("Illformed protobuf message. Missing y-coordinate");
                if (!chunk.hasZ())
                    throw new IllegalArgumentException("Illformed protobuf message. Missing z-coordinate");
                if (!chunk.hasData())
                    throw new IllegalArgumentException("Illformed protobuf message. Missing chunk data");
                final Vector3i pos = new Vector3i(chunk.getX(), chunk.getY(), chunk.getZ());
                final byte[] data = chunk.getData().toByteArray();
                store.serializedChunks.put(pos, data);
                store.sizeInBytes.addAndGet(data.length);
            }
        }
    }

    public void setup() {
        if (initialized) return;
        initialized = true;
        queuedChunks = Maps.newConcurrentMap();
        compressionQueue = Queues.newLinkedBlockingDeque();
        setupThreads();
    }

    public Chunk get(Vector3i id) {
        if (!initialized) throw new IllegalStateException("The chunk store is not initialized");
        final Chunk c = queuedChunks.get(id);
        if (c != null)
            return new Chunk(c);
        try {
            final byte[] data = serializedChunks.get(id);
            if (data == null) return null;
            return decode(data);
        } catch (Exception e) {
            logger.error("Error loading chunk {}", e, id);
            throw new RuntimeException("Error loading chunk " + id);
        }
    }

    public void put(Chunk c) {
        if (!initialized) throw new IllegalStateException("The chunk store is not initialized");
        final Vector3i pos = c.getPos();
        queuedChunks.put(pos, c);
        if (!compressionQueue.offer(c)) {
            queuedChunks.remove(pos, c);
            logger.error("Failed to add chunk to compression queue {}", pos);
        }
    }

    @Override
    public boolean contains(Vector3i position) {
        if (!initialized) throw new IllegalStateException("The chunk store is not initialized");
        return queuedChunks.containsKey(position) || serializedChunks.containsKey(position);
    }
    
    @Override
    public int list(List<Vector3i> output) {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        final int size = output.size();
        output.addAll(serializedChunks.keySet());
        return output.size() - size;
    }

    @Override
    public long sizeInBytes() {
        return sizeInBytes.get();
    }

    @Override
    public float size() {
        return (float) sizeInBytes.get() / (1 << 20);
    }

    public void dispose() {
        running.set(false);
        if (!initialized) return;
        compressionThreads.shutdown();
        try {
            compressionThreads.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Interrupted while awaiting thread disposal");
        }
    }

    @Override
    public void saveToFile(File file) {
        Preconditions.checkNotNull(file, "The parameter 'file' must not be null");
        try {
            final FileOutputStream fileOut = new FileOutputStream(file);
            final BufferedOutputStream bos = new BufferedOutputStream(fileOut);
            final CodedOutputStream cos = CodedOutputStream.newInstance(bos);
            final ProtobufHandler ph = new ProtobufHandler();
            ph.encode(this).writeTo(cos);
            cos.flush();
            bos.flush();
            bos.close();
            fileOut.close();
        } catch (IOException e) {
            logger.error("Error saving chunks to file {}", e, file);
        }
    }

    @Override
    public void loadFromFile(File file) {
        Preconditions.checkNotNull(file, "The parameter 'file' must not be null");
        try {
            final FileInputStream fileIn = new FileInputStream(file);
            final BufferedInputStream bis = new BufferedInputStream(fileIn);
            final CodedInputStream cin = CodedInputStream.newInstance(bis);
            final ProtobufHandler ph = new ProtobufHandler();
            final CompressedChunks message = CompressedChunks.parseFrom(cin);
            ph.decode(message, this);
        } catch (IOException e) {
            logger.error("Error loading chunks from file {}", e, file);
        }
    }
}
