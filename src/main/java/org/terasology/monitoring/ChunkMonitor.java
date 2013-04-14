package org.terasology.monitoring;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.terasology.monitoring.impl.ChunkMonitorEvent;
import org.terasology.monitoring.impl.WeakChunk;
import org.terasology.world.MiniatureChunk;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkState;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

public class ChunkMonitor {

    private static final EventBus eventbus = new EventBus("ChunkMonitor");
    private static final LinkedList<WeakChunk> chunks = new LinkedList<WeakChunk>();
    
    private static void post(Object event) {
        if (!Monitoring.isAdvancedMonitoringEnabled())
            return;
        eventbus.post(event);
    }
    
    private ChunkMonitor() {}

    public static void registerForEvents(Object object) {
        Preconditions.checkNotNull(object, "The parameter 'object' must not be null");
        eventbus.register(object);
    }
    
    public static synchronized void registerChunk(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        if (!Monitoring.isAdvancedMonitoringEnabled())
            return;
        if (chunk instanceof MiniatureChunk)
            return;
        final WeakChunk w = new WeakChunk(chunk);
        chunks.add(w);
        post(new ChunkMonitorEvent.Created(w));
    }
    
    public static synchronized void getChunks(List<Chunk> output) {
        final Iterator<WeakChunk> it = chunks.iterator();
        while (it.hasNext()) {
            final WeakChunk e = it.next();
            final Chunk c = e.getChunk();
            if (c != null) {
                output.add(c);
            }
        }
    }
    
    public static synchronized void getWeakChunks(List<WeakChunk> output) {
        final Iterator<WeakChunk> it = chunks.iterator();
        while (it.hasNext()) {
            final WeakChunk e = it.next();
            final Chunk c = e.getChunk();
            if (c != null) {
                output.add(e);
            }
        }
    }
    
    public static synchronized int purgeDeadChunks() {
        int result = 0;
        final Iterator<WeakChunk> it = chunks.iterator();
        while (it.hasNext()) {
            final WeakChunk e = it.next();
            final Chunk c = e.getChunk();
            if (c == null) {
                it.remove();
                ++result;
            }
        }
        return result;
    }
    
    public static void fireStateChanged(Chunk chunk, ChunkState oldState) {
        post(new ChunkMonitorEvent.StateChanged(chunk, oldState));
    }
    
    public static void fireChunkDeflated(Chunk chunk, int oldSize, int newSize) {
        post(new ChunkMonitorEvent.Deflated(chunk, oldSize, newSize));
    }
    
    public static void fireChunkDisposed(Chunk chunk) {
        post(new ChunkMonitorEvent.Disposed(chunk));
    }
}
