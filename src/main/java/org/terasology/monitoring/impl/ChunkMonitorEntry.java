package org.terasology.monitoring.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;

import com.google.common.base.Preconditions;

public class ChunkMonitorEntry {

    protected static final Logger logger = LoggerFactory.getLogger(ChunkMonitorEntry.class);
    
    protected final Vector3i pos;
    protected WeakChunk chunk = null;
    
    public ChunkMonitorEntry(Vector3i pos) {
        this.pos = Preconditions.checkNotNull(pos, "The parameter 'pos' must not be null");
    }

    public Vector3i getPosition() {
        return new Vector3i(pos);
    }
    
    public Chunk getChunk() {
        if (chunk != null)
            return chunk.getChunk();
        return null;
    }
    
    public void setChunk(Chunk value) {
        Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
        if (value == null)
            this.chunk = null;
        else if (chunk == null || chunk.getChunk() == null)
            chunk = new WeakChunk(value);
        else
            logger.error("A chunk is already registered for position {}", pos);
    }
}
