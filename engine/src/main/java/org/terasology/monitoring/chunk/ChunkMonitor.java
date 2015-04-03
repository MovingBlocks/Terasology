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
package org.terasology.monitoring.chunk;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;

import java.util.List;
import java.util.Map;

public final class ChunkMonitor {

    private static final EventBus EVENT_BUS = new EventBus("ChunkMonitor");
    private static final Map<Vector3i, ChunkMonitorEntry> CHUNKS = Maps.newConcurrentMap();

    private ChunkMonitor() {
    }

    private static void post(Object event) {
        EVENT_BUS.post(event);
    }

    private static synchronized ChunkMonitorEntry registerChunk(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        final Vector3i pos = chunk.getPosition();
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
        if (entry != null) {
            post(new ChunkMonitorEvent.Created(entry));
        }
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

    public static void fireChunkTessellated(Vector3i chunkPos, ChunkMesh mesh) {
        Preconditions.checkNotNull(chunkPos, "The parameter 'chunkPos' must not be null");
        post(new ChunkMonitorEvent.Tessellated(chunkPos, mesh));
    }

    public static synchronized void getChunks(List<ChunkMonitorEntry> output) {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        output.addAll(CHUNKS.values());
    }
}
