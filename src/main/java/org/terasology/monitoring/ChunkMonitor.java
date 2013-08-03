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
package org.terasology.monitoring;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.impl.ChunkMonitorEntry;
import org.terasology.monitoring.impl.ChunkMonitorEvent;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;

import java.util.List;
import java.util.Map;

public class ChunkMonitor {

    private static final EventBus eventbus = new EventBus("ChunkMonitor");
    private static final Map<Vector3i, ChunkMonitorEntry> chunks = Maps.newConcurrentMap();

    private static void post(Object event) {
        eventbus.post(event);
    }

    private static synchronized ChunkMonitorEntry registerChunk(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        final Vector3i pos = chunk.getPos();
        ChunkMonitorEntry entry = chunks.get(pos);
        if (entry == null) {
            entry = new ChunkMonitorEntry(pos);
            chunks.put(pos, entry);
        }
        entry.addChunk(chunk);
        return entry;
    }

    private ChunkMonitor() {
    }

    public static void registerForEvents(Object object) {
        Preconditions.checkNotNull(object, "The parameter 'object' must not be null");
        eventbus.register(object);
    }

    public static void fireChunkProviderInitialized(ChunkProvider provider) {
        post(new ChunkMonitorEvent.ChunkProviderInitialized(provider));
    }

    public static void fireChunkProviderDisposed(ChunkProvider provider) {
        chunks.clear();
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
        post(new ChunkMonitorEvent.Disposed(chunk.getPos()));
    }

    public static void fireChunkRevived(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        post(new ChunkMonitorEvent.Revived(chunk.getPos()));
    }

    public static void fireStateChanged(Chunk chunk, Chunk.State oldState) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        post(new ChunkMonitorEvent.StateChanged(chunk.getPos(), oldState, chunk.getChunkState()));
    }

    public static void fireChunkDeflated(Chunk chunk, int oldSize, int newSize) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        post(new ChunkMonitorEvent.Deflated(chunk.getPos(), oldSize, newSize));
    }

    public static void fireChunkTessellated(Vector3i chunkPos, ChunkMesh[] mesh) {
        Preconditions.checkNotNull(chunkPos, "The parameter 'chunkPos' must not be null");
        post(new ChunkMonitorEvent.Tessellated(chunkPos, mesh));
    }

    public static synchronized void getChunks(List<ChunkMonitorEntry> output) {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        output.addAll(chunks.values());
    }
}
