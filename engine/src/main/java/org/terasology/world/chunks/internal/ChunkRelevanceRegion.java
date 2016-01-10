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

package org.terasology.world.chunks.internal;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkRegionListener;

import java.util.Iterator;
import java.util.Set;

/**
 */
public class ChunkRelevanceRegion {
    private EntityRef entity;
    private Vector3i relevanceDistance = new Vector3i();
    private boolean dirty;
    private Vector3i center = new Vector3i();
    private Region3i currentRegion = Region3i.EMPTY;
    private Region3i previousRegion = Region3i.EMPTY;
    private ChunkRegionListener listener;

    private Set<Vector3i> relevantChunks = Sets.newLinkedHashSet();

    public ChunkRelevanceRegion(EntityRef entity, Vector3i relevanceDistance) {
        this.entity = entity;
        this.relevanceDistance.set(relevanceDistance);

        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc == null) {
            dirty = false;
        } else {
            center.set(ChunkMath.calcChunkPos(loc.getWorldPosition()));
            currentRegion = calculateRegion();
            dirty = true;
        }
    }

    public Vector3i getCenter() {
        return new Vector3i(center);
    }

    public void setRelevanceDistance(Vector3i distance) {
        if (!distance.equals(this.relevanceDistance)) {
            reviewRelevantChunks(distance);
            this.relevanceDistance.set(distance);
            this.currentRegion = calculateRegion();
            dirty = true;
        }
    }

    private void reviewRelevantChunks(Vector3i distance) {
        Vector3i extents = new Vector3i(distance.x / 2, distance.y / 2, distance.z / 2);
        Region3i retainRegion = Region3i.createFromCenterExtents(center, extents);
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
        previousRegion = currentRegion;
    }

    public Region3i getCurrentRegion() {
        return currentRegion;
    }

    public Region3i getPreviousRegion() {
        return previousRegion;
    }

    public void update() {
        if (!isValid()) {
            dirty = false;
        } else {
            Vector3i newCenter = calculateCenter();
            if (!newCenter.equals(center)) {
                dirty = true;
                center.set(newCenter);
                currentRegion = calculateRegion();
                reviewRelevantChunks(relevanceDistance);
            }
        }
    }

    private Region3i calculateRegion() {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            Vector3i extents = new Vector3i(relevanceDistance.x / 2, relevanceDistance.y / 2, relevanceDistance.z / 2);
            return Region3i.createFromCenterExtents(ChunkMath.calcChunkPos(loc.getWorldPosition()), extents);
        }
        return Region3i.EMPTY;
    }

    private Vector3i calculateCenter() {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            return ChunkMath.calcChunkPos(loc.getWorldPosition());
        }
        return new Vector3i();
    }

    public void setListener(ChunkRegionListener listener) {
        this.listener = listener;
    }

    private void sendChunkRelevant(Chunk chunk) {
        if (listener != null) {
            listener.onChunkRelevant(chunk.getPosition(), chunk);
        }
    }

    private void sendChunkIrrelevant(Vector3i pos) {
        if (listener != null) {
            listener.onChunkIrrelevant(pos);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
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

    /**
     * Checks if the chunk belongs to this relevance region and adds it to it if it is relevant.
     *
     * This method does explictly not care for the readyness of the chunk (light calcualted) or not: The light
     * calculation gets only performed once the adjacent chunks got loaded. So if wait for the light calculation
     * before we mark a chunk as relevant for a client then we would transfer less chunks to the client then the
     * relevance region is large. the client would then again perform the light calculation too based on that
     * reduced chunk count and would reduce the view distance again. That is why it makes sense to detect
     * chunks as relevant even when no light calculation has been performed yet.
     */
    public void checkIfChunkIsRelevant(Chunk chunk) {
        if (currentRegion.encompasses(chunk.getPosition()) && !relevantChunks.contains(chunk.getPosition())) {
            relevantChunks.add(chunk.getPosition());
            sendChunkRelevant(chunk);
        }
    }

    public Iterable<Vector3i> getNeededChunks() {
        return NeededChunksIterator::new;
    }

    public void chunkUnloaded(Vector3i pos) {
        if (relevantChunks.contains(pos)) {
            relevantChunks.remove(pos);
            sendChunkIrrelevant(pos);
        }
    }

    private class NeededChunksIterator implements Iterator<Vector3i> {
        Vector3i nextChunkPos;
        Iterator<Vector3i> regionPositions = currentRegion.iterator();

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
