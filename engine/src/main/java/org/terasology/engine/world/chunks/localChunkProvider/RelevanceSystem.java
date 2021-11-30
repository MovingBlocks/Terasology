// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks.localChunkProvider;

import com.google.common.collect.Maps;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.monitoring.Activity;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.world.RelevanceRegionComponent;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkRegionListener;
import org.terasology.engine.world.chunks.event.BeforeChunkUnload;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import org.terasology.engine.world.chunks.internal.ChunkRelevanceRegion;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * RelevanceSystem loads, holds and unloads chunks around "players" (entity with {@link RelevanceRegionComponent} and
 * {@link LocationComponent}).
 * <p>
 * Uses in singleplayer or multiplayer on server-side.
 * <p>
 * Client side multiplayer downloads and displays the chunks sent by the server.
 * <p>
 * It is uses {@link RelevanceRegionComponent} for determinate "view distance".
 */
public class RelevanceSystem implements UpdateSubscriberSystem {

    private static final Vector3i UNLOAD_LEEWAY = new Vector3i(1, 1, 1);
    private final ReadWriteLock regionLock = new ReentrantReadWriteLock();
    private final Map<EntityRef, ChunkRelevanceRegion> regions = Maps.newHashMap();
    private final LocalChunkProvider chunkProvider;

    public RelevanceSystem(LocalChunkProvider chunkProvider) {
        this.chunkProvider = chunkProvider;
    }

    @ReceiveEvent(components = {RelevanceRegionComponent.class, LocationComponent.class})
    public void onNewRelevanceRegion(OnActivatedComponent event, EntityRef entity) {
        addRelevanceEntity(entity, entity.getComponent(RelevanceRegionComponent.class).distance, null);
    }

    public Collection<ChunkRelevanceRegion> getRegions() {
        return regions.values();
    }

    @ReceiveEvent(components = RelevanceRegionComponent.class)
    public void onRelevanceRegionChanged(OnChangedComponent event, EntityRef entity) {
        updateRelevanceEntityDistance(entity, entity.getComponent(RelevanceRegionComponent.class).distance);
    }

    @ReceiveEvent(components = {RelevanceRegionComponent.class, LocationComponent.class})
    public void onLostRelevanceRegion(BeforeDeactivateComponent event, EntityRef entity) {
        removeRelevanceEntity(entity);
    }

    @ReceiveEvent(components = WorldComponent.class)
    public void onNewChunk(OnChunkLoaded chunkAvailable, EntityRef worldEntity) {
        for (ChunkRelevanceRegion region : regions.values()) {
            region.checkIfChunkIsRelevant(chunkProvider.getChunk(chunkAvailable.getChunkPos()));
        }
    }

    @ReceiveEvent(components = WorldComponent.class)
    public void onRemoveChunk(BeforeChunkUnload chunkUnloadEvent, EntityRef worldEntity) {
        for (ChunkRelevanceRegion region : regions.values()) {
            region.chunkUnloaded(chunkUnloadEvent.getChunkPos());
        }
    }

    /**
     * Update distance of relative entity, if exists.
     *
     * @param entity entity for update distance.
     * @param distance new distance for setting to entity's region.
     */
    public void updateRelevanceEntityDistance(EntityRef entity, Vector3ic distance) {
        regionLock.readLock().lock();
        try {
            ChunkRelevanceRegion region = regions.get(entity);
            if (region != null) {
                region.setRelevanceDistance(distance);
            }
        } finally {
            regionLock.readLock().unlock();
        }
    }

    /**
     * Remove Entity from relevance system.
     *
     * @param entity entity for remove.
     */
    public void removeRelevanceEntity(EntityRef entity) {
        regionLock.writeLock().lock();
        try {
            regions.remove(entity);
        } finally {
            regionLock.writeLock().unlock();
        }
    }

    /**
     * @return All chunks in the relevant regions which haven't been generated yet.
     */
    public Stream<Vector3i> neededChunks() {
        return regions.values()
                .stream()
                .flatMap(x -> StreamSupport.stream(x.getNeededChunks().spliterator(), false));
    }

    /**
     * Synchronize region center to entity's position and create/load chunks in that region.
     */
    private void updateRelevance() {
        try (Activity activity = PerformanceMonitor.startActivity("Update relevance")) {
            for (ChunkRelevanceRegion chunkRelevanceRegion : regions.values()) {
                chunkRelevanceRegion.update();
                if (chunkRelevanceRegion.isDirty()) {
                    for (Vector3i pos : chunkRelevanceRegion.getNeededChunks()) {
                        Chunk chunk = chunkProvider.getChunk(pos);
                        if (chunk != null) {
                            chunkRelevanceRegion.checkIfChunkIsRelevant(chunk);
                        }
                    }
                    chunkProvider.notifyRelevanceChanged();
                    chunkRelevanceRegion.setUpToDate();
                }
            }
        }
    }

    /**
     * Add entity to relevance system. create region for it. Update distance if region exists already. Create/Load
     * chunks for region.
     *
     * @param entity the region will be centered around the LocationComponent of this entity
     * @param distance the dimensions of the region, in chunks
     * @param listener notified when relevant chunks become available
     *
     * @return the region of chunks deemed relevant
     */
    public BlockRegionc addRelevanceEntity(EntityRef entity, Vector3ic distance, ChunkRegionListener listener) {
        if (!entity.exists()) {
            return null;  // Futures.immediateFailedFuture(new IllegalArgumentException("Entity does not exist."));
        }
        regionLock.readLock().lock();
        try {
            ChunkRelevanceRegion region = regions.get(entity);
            if (region != null) {
                region.setRelevanceDistance(distance);
                return new BlockRegion(region.getCurrentRegion());  // Future of “when region.currentRegion is no longer dirty”?
            }
        } finally {
            regionLock.readLock().unlock();
        }
        ChunkRelevanceRegion region = new ChunkRelevanceRegion(entity, distance);
        if (listener != null) {
            region.setListener(listener);
        }
        regionLock.writeLock().lock();
        try {
            regions.put(entity, region);
        } finally {
            regionLock.writeLock().unlock();
        }

        StreamSupport.stream(region.getCurrentRegion().spliterator(), false)
                .forEach(
                        pos -> {
                            Chunk chunk = chunkProvider.getChunk(pos);
                            if (chunk != null) {
                                region.checkIfChunkIsRelevant(chunk);
                            }
                        }
                );
        return new BlockRegion(region.getCurrentRegion());  // whenAllComplete
    }

    /**
     * Check that chunk contains in any regions.
     *
     * @param pos chunk's position
     * @return {@code true} if chunk in regions, otherwise {@code false}
     */
    public boolean isChunkInRegions(Vector3ic pos) {
        for (ChunkRelevanceRegion region : regions.values()) {
            if (new BlockRegion(region.getCurrentRegion()).expand(UNLOAD_LEEWAY).contains(pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create comparator for chunk positions, which compare by distance from region centers
     *
     * @return Comparator.
     */
    public Comparator<Vector3ic> createChunkPosComparator() {
        return new PositionRelevanceComparator();
    }

    /**
     * @param delta The time (in seconds) since the last engine update.
     */
    @Override
    public void update(float delta) {
        updateRelevance();
    }

    @Override
    public void initialise() {
        // ignore
    }

    @Override
    public void preBegin() {
        // ignore
    }

    @Override
    public void postBegin() {
        // ignore
    }

    @Override
    public void preSave() {
        // ignore
    }

    @Override
    public void postSave() {
        // ignore
    }

    @Override
    public void shutdown() {
        // ignore
    }

    private int regionsDistanceScore(Vector3ic chunk) {
        int score = Integer.MAX_VALUE;

        regionLock.readLock().lock();
        try {

            for (ChunkRelevanceRegion region : regions.values()) {
                int dist = (int) chunk.gridDistance(region.getCenter());
                if (dist < score) {
                    score = dist;
                }
                if (score == 0) {
                    break;
                }
            }
            return score;
        } finally {
            regionLock.readLock().unlock();
        }
    }

    /**
     * Compare ChunkTasks by distance from region's centers.
     */
    private class PositionRelevanceComparator implements Comparator<Vector3ic> {

        @Override
        public int compare(Vector3ic o1, Vector3ic o2) {
            return score(o1) - score(o2);
        }

        private int score(Vector3ic position) {
            return RelevanceSystem.this.regionsDistanceScore(position);
        }
    }
}
