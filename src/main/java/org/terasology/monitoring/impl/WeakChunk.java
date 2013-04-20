package org.terasology.monitoring.impl;

import java.lang.ref.WeakReference;

import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;

import com.google.common.base.Preconditions;

public class WeakChunk {
    
    protected final Vector3i position;
    protected final WeakReference<Chunk> ref;
    
    public WeakChunk(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
        this.position = chunk.getPos();
        this.ref = new WeakReference<Chunk>(chunk);
    }
    
    public final Vector3i getPos() {
        return new Vector3i(position);
    }
    
    public final Chunk getChunk() {
        return ref.get();
    }
}