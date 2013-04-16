package org.terasology.monitoring.impl;

import org.terasology.math.Vector3i;
import org.terasology.world.chunks.ChunkState;

import com.google.common.base.Preconditions;

public abstract class ChunkMonitorEvent {

    public abstract Vector3i getPosition();
    
    protected static class BasicChunkEvent extends ChunkMonitorEvent {
        
        protected final Vector3i position;
        
        public BasicChunkEvent(Vector3i position) {
            Preconditions.checkNotNull(position, "The parameter 'chunk' must not be null");
            this.position = position;
        }
        
        public final Vector3i getPosition() {
            return position;
        }
    }
    
    public static class Created extends BasicChunkEvent {
        
        protected final ChunkMonitorEntry entry;
        
        public Created(ChunkMonitorEntry chunk) {
            super(Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null").getPosition());
            this.entry = chunk;
        }
        
        public final ChunkMonitorEntry getEntry() {
            return entry;
        }
    }
    
    public static class Revived extends BasicChunkEvent {
        public Revived(Vector3i position) {
            super(position);
        }
    }
    
    public static class Disposed extends BasicChunkEvent {
        public Disposed(Vector3i position) {
            super(position);
        }
    }
    
    public static class StateChanged extends BasicChunkEvent {
        
        public final ChunkState oldState, newState;
        
        public StateChanged(Vector3i position, ChunkState oldState, ChunkState newState) {
            super(position);
            this.oldState = oldState;
            this.newState = newState;
        }
    }
    
    public static class Deflated extends BasicChunkEvent {
        
        public final int oldSize, newSize;
        
        public Deflated(Vector3i position, int oldSize, int newSize) {
            super(position);
            this.oldSize = oldSize;
            this.newSize = newSize;
        }
    }
}
