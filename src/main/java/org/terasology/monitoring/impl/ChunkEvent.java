package org.terasology.monitoring.impl;

import org.terasology.monitoring.WeakChunk;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkState;

import com.google.common.base.Preconditions;

public abstract class ChunkEvent {

    public abstract Chunk getChunk();
    
    protected static class BasicChunkEvent extends ChunkEvent {
        
        protected final Chunk chunk;
        
        public BasicChunkEvent(Chunk chunk) {
            Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
            this.chunk = chunk;
        }
        
        public final Chunk getChunk() {
            return chunk;
        }
    }
    
    public static class Created extends ChunkEvent {
        
        protected final WeakChunk weakChunk;
        
        public Created(WeakChunk chunk) {
            this.weakChunk = chunk;
        }
        
        public final WeakChunk getWeakChunk() {
            return weakChunk;
        }
        
        public final Chunk getChunk() {
            return weakChunk.getChunk();
        }
    }
    
    public static class Disposed extends BasicChunkEvent {
        public Disposed(Chunk chunk) {
            super(chunk);
        }
    }
    
    public static class StateChanged extends BasicChunkEvent {
        
        public final ChunkState oldState, newState;
        
        public StateChanged(Chunk chunk, ChunkState oldState) {
            super(chunk);
            this.oldState = oldState;
            this.newState = chunk.getChunkState();
        }
    }
    
    public static class Deflated extends BasicChunkEvent {
        
        public final int oldSize, newSize;
        
        public Deflated(Chunk chunk, int oldSize, int newSize) {
            super(chunk);
            this.oldSize = oldSize;
            this.newSize = newSize;
        }
    }
}
