package org.terasology.monitoring;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.terasology.math.Vector3i;
import org.terasology.monitoring.impl.ChunkMonitorEntry;
import org.terasology.monitoring.impl.ChunkMonitorEvent;
import org.terasology.world.MiniatureChunk;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkState;
import org.terasology.world.chunks.provider.ChunkProvider;
import org.terasology.world.chunks.store.ChunkStore;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

public class ChunkMonitor {

    private static final EventBus eventbus = new EventBus("ChunkMonitor");
    private static final Map<Vector3i, ChunkMonitorEntry> chunks = Maps.newConcurrentMap();
    
    private static void post(Object event) {
        if (!Monitoring.isAdvancedMonitoringEnabled())
            return;
        eventbus.post(event);
    }
    
    private static synchronized void registerCachedChunks(ChunkStore farStore) {
        Preconditions.checkNotNull(farStore, "The parameter 'farStore' must not be null");
        if (!Monitoring.isAdvancedMonitoringEnabled())
            return;
        final LinkedList<Vector3i> cached = new LinkedList<Vector3i>();
        farStore.list(cached);
        for (final Vector3i pos : cached) {
            ChunkMonitorEntry entry = chunks.get(pos);
            if (entry == null) {
                entry = new ChunkMonitorEntry(pos);
                chunks.put(pos, entry);
            }
        }
    }
    
    private static synchronized ChunkMonitorEntry registerChunk(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        if (!Monitoring.isAdvancedMonitoringEnabled())
            return null;
        if (chunk instanceof MiniatureChunk)
            return null;
        final Vector3i pos = chunk.getPos();
        ChunkMonitorEntry entry = chunks.get(pos);
        if (entry == null) {
            entry = new ChunkMonitorEntry(pos);
            chunks.put(pos, entry);
        }
        entry.setChunk(chunk);
        return entry;
    }
    
    private ChunkMonitor() {}

    public static void registerForEvents(Object object) {
        Preconditions.checkNotNull(object, "The parameter 'object' must not be null");
        eventbus.register(object);
    }
    
    public static void fireChunkProviderInitialized(ChunkProvider provider, ChunkStore farStore) {
        registerCachedChunks(farStore);
        post(new ChunkMonitorEvent.ChunkProviderInitialized(provider, farStore));
    }
    
    public static void fireChunkProviderDisposed(ChunkProvider provider) {
        chunks.clear();
        post(new ChunkMonitorEvent.ChunkProviderDisposed(provider));
    }
    
    public static void fireChunkCreated(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        final ChunkMonitorEntry entry = registerChunk(chunk);
        if (entry != null)
            post(new ChunkMonitorEvent.Created(entry)); 
    }
    
    public static void fireChunkRevived(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        post(new ChunkMonitorEvent.Revived(chunk.getPos()));
    }
    
    public static void fireStateChanged(Chunk chunk, ChunkState oldState) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        post(new ChunkMonitorEvent.StateChanged(chunk.getPos(), oldState, chunk.getChunkState()));
    }
    
    public static void fireChunkDeflated(Chunk chunk, int oldSize, int newSize) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        post(new ChunkMonitorEvent.Deflated(chunk.getPos(), oldSize, newSize));
    }
    
    public static void fireChunkDisposed(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        post(new ChunkMonitorEvent.Disposed(chunk.getPos()));
    }

    public static synchronized void getChunks(List<ChunkMonitorEntry> output) {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        final Iterator<ChunkMonitorEntry> it = chunks.values().iterator();
        while (it.hasNext()) {
            final ChunkMonitorEntry e = it.next();
            output.add(e);
        }
    }
}
