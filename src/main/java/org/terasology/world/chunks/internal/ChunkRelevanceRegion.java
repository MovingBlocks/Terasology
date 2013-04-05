/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.world.chunks.internal;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkRegionListener;

import javax.vecmath.Vector3f;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Immortius
 */
public class ChunkRelevanceRegion {
    private int unloadLeeway = 2;

    private EntityRef entity;
    private int distance;
    private boolean dirty;
    private Vector3i center = new Vector3i();
    private Region3i region = Region3i.EMPTY;
    private ChunkRegionListener listener;

    private Set<Vector3i> relevantChunks = Sets.newLinkedHashSet();

    public ChunkRelevanceRegion(EntityRef entity, int distance) {
        this.entity = entity;
        this.distance = distance;

        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc == null) {
            dirty = false;
        } else {
            center.set(worldToChunkPos(loc.getWorldPosition()));
            region = calculateRegion();
            dirty = true;
        }
    }

    public Vector3i getCenter() {
        return new Vector3i(center);
    }

    public void setDistance(int distance) {
        if (distance < this.distance) {
            reviewRelevantChunks(distance);
        }
        this.distance = distance;
        this.region = calculateRegion();
        dirty = true;
    }

    private void reviewRelevantChunks(int distance) {
        Region3i retainRegion = Region3i.createFromCenterExtents(center, new Vector3i(TeraMath.ceilToInt(distance / 2.0f) + unloadLeeway, 0, TeraMath.ceilToInt(distance / 2.0f) + unloadLeeway));
        Iterator<Vector3i> iter = relevantChunks.iterator();
        while (iter.hasNext()) {
            Vector3i pos = iter.next();
            if (!retainRegion.encompasses(pos)) {
                sendChunkIrrelevant(pos);
                iter.remove();
            }
        }
    }

    public boolean isValid() {
        return entity.hasComponent(LocationComponent.class);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setUpToDate() {
        dirty = false;
    }

    public Region3i getRegion() {
        return region;
    }

    public void update() {
        if (!isValid()) {
            dirty = false;
        } else {
            Vector3i newCenter = calculateCenter();
            if (!newCenter.equals(center)) {
                dirty = true;
                center.set(newCenter);
                region = calculateRegion();
                reviewRelevantChunks(distance);
            }
        }
    }

    private Region3i calculateRegion() {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            return Region3i.createFromCenterExtents(worldToChunkPos(loc.getWorldPosition()), new Vector3i(TeraMath.ceilToInt(distance / 2.0f), 0, TeraMath.ceilToInt(distance / 2.0f)));
        }
        return Region3i.EMPTY;
    }

    private Vector3i calculateCenter() {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            return worldToChunkPos(loc.getWorldPosition());
        }
        return new Vector3i();
    }

    private Vector3i worldToChunkPos(Vector3f worldPos) {
        worldPos.x /= Chunk.SIZE_X;
        worldPos.y = 0;
        worldPos.z /= Chunk.SIZE_Z;
        return new Vector3i(worldPos);
    }

    public void setListener(ChunkRegionListener listener) {
        this.listener = listener;
    }

    private void sendChunkRelevant(Chunk chunk) {
        if (listener != null) {
            listener.onChunkRelevant(chunk.getPos(), chunk);
        }
    }

    private void sendChunkIrrelevant(Vector3i pos) {
        if (listener != null) {
            listener.onChunkIrrelevant(pos);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof ChunkRelevanceRegion) {
            ChunkRelevanceRegion other = (ChunkRelevanceRegion) o;
            return Objects.equal(other.entity, entity);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(entity);
    }

    public void chunkReady(Chunk chunk) {
        if (region.encompasses(chunk.getPos()) && !relevantChunks.contains(chunk.getPos())) {
            relevantChunks.add(chunk.getPos());
            sendChunkRelevant(chunk);
        }
    }

    public Iterable<Vector3i> getNeededChunks() {
        return new Iterable<Vector3i>() {
            @Override
            public Iterator<Vector3i> iterator() {
                return new NeededChunksIterator();
            }
        };
    }

    public void chunkUnloaded(Vector3i pos) {
        if (relevantChunks.contains(pos)) {
            relevantChunks.remove(pos);
            sendChunkIrrelevant(pos);
        }
    }

    private class NeededChunksIterator implements Iterator<Vector3i> {
        Vector3i nextChunkPos;
        Iterator<Vector3i> regionPositions = region.iterator();

        public NeededChunksIterator() {
            calculateNext();
        }

        @Override
        public boolean hasNext() {
            return nextChunkPos != null;
        }

        @Override
        public Vector3i next() {
            Vector3i result = nextChunkPos;
            calculateNext();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void calculateNext() {
            nextChunkPos = null;
            while (regionPositions.hasNext() && nextChunkPos == null) {
                Vector3i candidate = regionPositions.next();
                if (!relevantChunks.contains(candidate)) {
                    nextChunkPos = candidate;
                }
            }
        }
    }
}
