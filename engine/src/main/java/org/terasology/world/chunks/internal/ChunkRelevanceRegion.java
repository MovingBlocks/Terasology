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
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.ChunkMath;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionc;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkRegionListener;
import org.terasology.world.chunks.Chunks;

import java.util.Iterator;
import java.util.Set;

/**
 */
public class ChunkRelevanceRegion {
    private EntityRef entity;
    private Vector3i relevanceDistance = new Vector3i();
    private boolean dirty;
    private Vector3i center = new Vector3i();
    private BlockRegion currentRegion = new BlockRegion(BlockRegion.INVALID);
    private BlockRegion previousRegion = new BlockRegion(BlockRegion.INVALID);
    private ChunkRegionListener listener;

    private Set<Vector3i> relevantChunks = Sets.newLinkedHashSet();

    public ChunkRelevanceRegion(EntityRef entity, Vector3ic relevanceDistance) {
        this.entity = entity;
        this.relevanceDistance.set(relevanceDistance);

        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if(loc == null) {
            dirty = false;
            return;
        }
        Vector3f position = loc.getWorldPosition(new Vector3f());
        if (!position.isFinite()) {
            dirty = false;
            return;
        }
        center.set(Chunks.toChunkPos(position, new Vector3i()));
        updateCurrentRegion();
        dirty = true;
    }

    public Vector3i getCenter() {
        return center;
    }

    public void setRelevanceDistance(Vector3ic distance) {
        if (!distance.equals(this.relevanceDistance)) {
            reviewRelevantChunks(distance);
            this.relevanceDistance.set(distance);
            updateCurrentRegion();
            dirty = true;
        }
    }

    private void reviewRelevantChunks(Vector3ic distance) {
        BlockRegion retainRegion = new BlockRegion(center).expand(distance.x() / 2, distance.y() / 2, distance.z() / 2);
        Iterator<Vector3i> iter = relevantChunks.iterator();
        while (iter.hasNext()) {
            Vector3i pos = iter.next();
            if (!retainRegion.contains(pos)) {
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

    public BlockRegionc getCurrentRegion() {
        return currentRegion;
    }

    public BlockRegionc getPreviousRegion() {
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
                updateCurrentRegion();
                reviewRelevantChunks(relevanceDistance);
            }
        }
    }

    private void updateCurrentRegion() {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            Vector3fc position = loc.getWorldPosition(new Vector3f());
            if (position.isFinite()) {
                Vector3ic chunkPosition = Chunks.toChunkPos(position, new Vector3i());
                currentRegion.set(chunkPosition, chunkPosition).expand(relevanceDistance.x / 2, relevanceDistance.y / 2, relevanceDistance.z / 2);
            }
        }
        currentRegion.set(BlockRegion.INVALID);
    }

    private Vector3i calculateCenter() {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null && !Float.isNaN(loc.getWorldPosition().x)) {
            Vector3fc position = loc.getWorldPosition(new Vector3f());
            if (position.isFinite()) {
                Vector3i chunkPosition = Chunks.toChunkPos(position, new Vector3i());
                return chunkPosition;
            }
        }
        return new Vector3i();
    }

    public void setListener(ChunkRegionListener listener) {
        this.listener = listener;
    }

    private void sendChunkRelevant(Chunk chunk) {
        if (listener != null) {
            listener.onChunkRelevant(chunk.getPosition(new org.joml.Vector3i()), chunk);
        }
    }

    private void sendChunkIrrelevant(Vector3ic pos) {
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
        if (currentRegion.contains(chunk.getPosition(new Vector3i())) && !relevantChunks.contains(chunk.getPosition(new Vector3i()))) {
            relevantChunks.add(chunk.getPosition(new Vector3i()));
            sendChunkRelevant(chunk);
        }
    }

    public Iterable<Vector3ic> getNeededChunks() {
        return NeededChunksIterator::new;
    }

    public void chunkUnloaded(Vector3ic pos) {
        if (relevantChunks.contains(pos)) {
            relevantChunks.remove(pos);
            sendChunkIrrelevant(pos);
        }
    }

    private class NeededChunksIterator implements Iterator<Vector3ic> {
        Vector3i current = null;
        Vector3i next = new Vector3i();
        Iterator<Vector3ic> regionPositions = currentRegion.iterator();

        NeededChunksIterator() {
            next.set(findNext());
        }

        @Override
        public boolean hasNext() {
            if (current == null) {
                return true;
            }
            if (current.equals(next)) {
                Vector3ic nn = findNext();
                if (nn != null) {
                    next.set(nn);
                    return true;
                }
                return false;
            }
            return !relevantChunks.contains(next);
        }

        @Override
        public Vector3ic next() {
            if (current == null) {
                current = new Vector3i(next);
                return next;
            }
            if (current.equals(next)) {
                Vector3ic nn = findNext();
                if (nn != null) {
                    next.set(nn);
                    return next;
                }
                return null;
            }
            current.set(next);
            return next;
        }

        private Vector3ic findNext() {
            while (regionPositions.hasNext()) {
                Vector3ic candidate = regionPositions.next();
                if (!relevantChunks.contains(candidate)) {
                    return candidate;
                }
            }
            return null;
        }
    }
}
