package org.terasology.monitoring.impl;

import java.util.Date;

import org.terasology.math.Vector3i;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.world.chunks.ChunkState;
import org.terasology.world.chunks.provider.ChunkProvider;
import org.terasology.world.chunks.store.ChunkStore;

import com.google.common.base.Preconditions;

public abstract class ChunkMonitorEvent {
    
    public final Date date = new Date();
    
    public static class ChunkProviderInitialized extends ChunkMonitorEvent {
        
        public final ChunkProvider provider;
        public final ChunkStore farStore;
        
        public ChunkProviderInitialized(ChunkProvider provider, ChunkStore farStore) {
            Preconditions.checkNotNull(provider, "The parameter 'provider' must not be null");
            Preconditions.checkNotNull(farStore, "The parameter 'farStore' must not be null");
            this.provider = provider;
            this.farStore = farStore;
        }
    }
    
    public static class ChunkProviderDisposed extends ChunkMonitorEvent {
        
        public final ChunkProvider provider;
        
        public ChunkProviderDisposed(ChunkProvider provider) {
            Preconditions.checkNotNull(provider, "The parameter 'provider' must not be null");
            this.provider = provider;
        }
    } 

    public static class BasicChunkEvent extends ChunkMonitorEvent {
        
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
    
    public static class Tessellated extends BasicChunkEvent {

        public final ChunkMeshInfo meshInfo;
        
        public Tessellated(Vector3i position, ChunkMesh[] mesh) {
            super(position);
            this.meshInfo = new ChunkMeshInfo(mesh);
        }
    }
}
