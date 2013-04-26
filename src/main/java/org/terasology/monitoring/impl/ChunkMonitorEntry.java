package org.terasology.monitoring.impl;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;

import com.google.common.base.Preconditions;

public class ChunkMonitorEntry {

    protected static final Logger logger = LoggerFactory.getLogger(ChunkMonitorEntry.class);
    
    protected final Vector3i pos;
    protected LinkedList<WeakReference<Chunk>> chunks = new LinkedList<WeakReference<Chunk>>();
    
    protected final void purge() {
        if (chunks.size() == 0) return;
        final Iterator<WeakReference<Chunk>> it = chunks.iterator();
        while (it.hasNext()) {
            final WeakReference<Chunk> w = it.next();
            if (w.get() == null)
                it.remove();
        } 
    }
    
    public ChunkMonitorEntry(Vector3i pos) {
        this.pos = Preconditions.checkNotNull(pos, "The parameter 'pos' must not be null");
    }

    public Vector3i getPosition() {
        return new Vector3i(pos);
    }
    
    public Chunk getLatestChunk() {
        final WeakReference<Chunk> chunk = chunks.peekLast();
        if (chunk != null)
            return chunk.get();
        return null;
    }
    
    public void addChunk(Chunk value) {
        Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
        Preconditions.checkArgument(pos.equals(value.getPos()), "Expected chunk for position {} but got position {} instead", pos, value.getPos());
        purge();
        chunks.add(new WeakReference<Chunk>(value));
        if (chunks.size() > 1)
            logger.error("Multiple chunks for position {} are registered ({})", pos, chunks.size());
    }
}
