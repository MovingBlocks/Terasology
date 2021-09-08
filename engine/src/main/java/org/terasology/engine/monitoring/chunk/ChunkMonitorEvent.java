// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring.chunk;

import com.google.common.base.Preconditions;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.world.chunks.ChunkProvider;

public abstract class ChunkMonitorEvent {

    public static class ChunkProviderInitialized extends ChunkMonitorEvent {

        public final ChunkProvider provider;

        public ChunkProviderInitialized(ChunkProvider provider) {
            Preconditions.checkNotNull(provider, "The parameter 'provider' must not be null");
            this.provider = provider;
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

        protected final Vector3ic position;

        public BasicChunkEvent(Vector3ic position) {
            Preconditions.checkNotNull(position, "The parameter 'chunk' must not be null");
            this.position = position;
        }

        public final Vector3ic getPosition() {
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
        public Revived(Vector3ic position) {
            super(position);
        }
    }

    public static class Disposed extends BasicChunkEvent {
        public Disposed(Vector3ic position) {
            super(position);
        }
    }

    public static class Deflated extends BasicChunkEvent {

        public final int oldSize;
        public final int newSize;

        public Deflated(Vector3ic position, int oldSize, int newSize) {
            super(position);
            this.oldSize = oldSize;
            this.newSize = newSize;
        }
    }

    public static class Tessellated extends BasicChunkEvent {

        public final ChunkMeshInfo meshInfo;

        public Tessellated(Vector3ic position, ChunkMesh mesh) {
            super(position);
            this.meshInfo = new ChunkMeshInfo(mesh);
        }
    }
}
