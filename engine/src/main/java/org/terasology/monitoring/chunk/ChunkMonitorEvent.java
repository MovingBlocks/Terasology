/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.monitoring.chunk;

import com.google.common.base.Preconditions;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.world.chunks.ChunkProvider;

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

    public static class Deflated extends BasicChunkEvent {

        public final int oldSize;
        public final int newSize;

        public Deflated(Vector3i position, int oldSize, int newSize) {
            super(position);
            this.oldSize = oldSize;
            this.newSize = newSize;
        }
    }

    public static class Tessellated extends BasicChunkEvent {

        public final ChunkMeshInfo meshInfo;

        public Tessellated(Vector3i position, ChunkMesh mesh) {
            super(position);
            this.meshInfo = new ChunkMeshInfo(mesh);
        }
    }
}
