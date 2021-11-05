// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.internal;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkRegionListener;
import org.terasology.engine.world.chunks.Chunks;

import java.util.Iterator;
import java.util.Set;

public class ChunkRelevanceRegion {
    private EntityRef entity;
    private Vector3i relevanceDistance = new Vector3i();
    private boolean dirty;
    private Vector3i center = new Vector3i();
    private BlockRegion currentRegion = new BlockRegion(BlockRegion.INVALID);
    private BlockRegion previousRegion = new BlockRegion(BlockRegion.INVALID);
    private ChunkRegionListener listener;

    private Set<Vector3ic> relevantChunks = Sets.newLinkedHashSet();

    public ChunkRelevanceRegion(EntityRef entity, Vector3ic relevanceDistance) {
        this.entity = entity;
        this.relevanceDistance.set(relevanceDistance);

        LocationComponent loc = entity.getComponent(LocationComponent.class);

        if (loc == null || Float.isNaN(loc.getWorldPosition(new Vector3f()).x)) {
            dirty = false;
        } else {
            center.set(Chunks.toChunkPos(loc.getWorldPosition(new Vector3f()), new Vector3i()));
            currentRegion = calculateRegion();
            dirty = true;
        }
    }

    public Vector3i getCenter() {
        return center;
    }

    public void setRelevanceDistance(Vector3ic distance) {
        if (!distance.equals(this.relevanceDistance)) {
            reviewRelevantChunks(distance);
            this.relevanceDistance.set(distance);
            this.currentRegion = calculateRegion();
            dirty = true;
        }
    }

    private void reviewRelevantChunks(Vector3ic distance) {
        Vector3i extents = new Vector3i(distance.x() / 2, distance.y() / 2, distance.z() / 2);
        BlockRegion retainRegion = new BlockRegion(center).expand(extents);
        Iterator<Vector3ic> iter = relevantChunks.iterator();
        while (iter.hasNext()) {
            Vector3ic pos = iter.next();
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

    public BlockRegion getCurrentRegion() {
        return currentRegion;
    }

    public BlockRegion getPreviousRegion() {
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

    private BlockRegion calculateRegion() {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null && !Float.isNaN(loc.getWorldPosition(new Vector3f()).x)) {
            Vector3i extents = new Vector3i(relevanceDistance.x / 2, relevanceDistance.y / 2, relevanceDistance.z / 2);
            return new BlockRegion(Chunks.toChunkPos(loc.getWorldPosition(new Vector3f()), new Vector3i())).expand(extents);
        }
        return new BlockRegion(BlockRegion.INVALID);
    }

    private Vector3i calculateCenter() {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null && !Float.isNaN(loc.getWorldPosition(new Vector3f()).x)) {
            return Chunks.toChunkPos(loc.getWorldPosition(new Vector3f()), new Vector3i());
        }
        return new Vector3i();
    }

    public void setListener(ChunkRegionListener listener) {
        this.listener = listener;
    }

    private void sendChunkRelevant(Chunk chunk) {
        if (listener != null) {
            listener.onChunkRelevant(chunk.getPosition(new Vector3i()), chunk);
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

    public Iterable<Vector3i> getNeededChunks() {
        return NeededChunksIterator::new;
    }

    public void chunkUnloaded(Vector3ic pos) {
        if (relevantChunks.contains(pos)) {
            relevantChunks.remove(pos);
            sendChunkIrrelevant(pos);
        }
    }

    private class NeededChunksIterator implements Iterator<Vector3i> {
        Vector3i nextChunkPos;
        Iterator<Vector3ic> allPositions;

        NeededChunksIterator() {
            allPositions = currentRegion.iterator();
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
            while (allPositions.hasNext()) {
                Vector3ic regionPosition = allPositions.next();
                if (!relevantChunks.contains(regionPosition)) {
                    nextChunkPos = new Vector3i(regionPosition);
                    return;
                }
            }
        }
    }
}
