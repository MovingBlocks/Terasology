// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring.chunk;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import org.joml.Vector3ic;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkProvider;

import java.util.List;
import java.util.Map;

public final class ChunkMonitor {

    private static final EventBus EVENT_BUS = new EventBus("ChunkMonitor");
    private static final Map<Vector3ic, ChunkMonitorEntry> CHUNKS = Maps.newConcurrentMap();

    private ChunkMonitor() {
    }

    private static void post(Object event) {
        EVENT_BUS.post(event);
    }

    private static synchronized ChunkMonitorEntry registerChunk(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        final Vector3ic pos = chunk.getPosition();
        ChunkMonitorEntry entry = CHUNKS.get(pos);
        if (entry == null) {
            entry = new ChunkMonitorEntry(pos);
            CHUNKS.put(pos, entry);
        }
        entry.addChunk(chunk);
        return entry;
    }

    public static void registerForEvents(Object object) {
        Preconditions.checkNotNull(object, "The parameter 'object' must not be null");
        EVENT_BUS.register(object);
    }

    public static void fireChunkProviderInitialized(ChunkProvider provider) {
        post(new ChunkMonitorEvent.ChunkProviderInitialized(provider));
    }

    public static void fireChunkProviderDisposed(ChunkProvider provider) {
        CHUNKS.clear();
        post(new ChunkMonitorEvent.ChunkProviderDisposed(provider));
    }

    public static void fireChunkCreated(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        final ChunkMonitorEntry entry = registerChunk(chunk);
        post(new ChunkMonitorEvent.Created(entry));
    }

    public static void fireChunkDisposed(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        post(new ChunkMonitorEvent.Disposed(chunk.getPosition()));
    }

    public static void fireChunkRevived(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        post(new ChunkMonitorEvent.Revived(chunk.getPosition()));
    }

    public static void fireChunkDeflated(Chunk chunk, int oldSize, int newSize) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        post(new ChunkMonitorEvent.Deflated(chunk.getPosition(), oldSize, newSize));
    }

    public static void fireChunkTessellated(Chunk chunk, ChunkMesh mesh) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunkPos' must not be null");
        post(new ChunkMonitorEvent.Tessellated(chunk.getPosition(), mesh));
    }

    public static synchronized void getChunks(List<ChunkMonitorEntry> output) {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        output.addAll(CHUNKS.values());
    }
}
