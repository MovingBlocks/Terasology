// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.chunks.localChunkProvider;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.Activity;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.world.RelevanceRegionComponent;
import org.terasology.world.WorldComponent;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkRegionListener;
import org.terasology.world.chunks.event.BeforeChunkUnload;
import org.terasology.world.chunks.event.OnChunkLoaded;
import org.terasology.world.chunks.internal.ChunkRelevanceRegion;
import org.terasology.world.chunks.pipeline.PositionFuture;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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

    private static final Vector3i UNLOAD_LEEWAY = Vector3i.one();
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
            region.chunkUnloaded(JomlUtil.from(chunkUnloadEvent.getChunkPos()));
        }
    }

    /**
     * Update distance of relative entity, if exists.
     *
     * @param entity entity for update distance.
     * @param distance new distance for setting to entity's region.
     */
    public void updateRelevanceEntityDistance(EntityRef entity, Vector3i distance) {
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
                        } else {
                            chunkProvider.createOrLoadChunk(pos);
                        }
                    }
                    chunkRelevanceRegion.setUpToDate();
                }
            }
        }
    }

    /**
     * Add entity to relevance system. create region for it. Update distance if region exists already. Create/Load
     * chunks for region.
     *
     * @param entity entity to add.
     * @param distance region's distance.
     * @param listener chunk relevance listener.
     */
    public void addRelevanceEntity(EntityRef entity, Vector3i distance, ChunkRegionListener listener) {
        if (!entity.exists()) {
            return;
        }
        regionLock.readLock().lock();
        try {
            ChunkRelevanceRegion region = regions.get(entity);
            if (region != null) {
                region.setRelevanceDistance(distance);
                return;
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
                .sorted(new PositionRelevanceComparator()) //<-- this is n^2 cost. not sure why this needs to be sorted like this.
                .forEach(
                        pos -> {
                            Chunk chunk = chunkProvider.getChunk(pos);
                            if (chunk != null) {
                                region.checkIfChunkIsRelevant(chunk);
                            } else {
                                chunkProvider.createOrLoadChunk(pos);
                            }
                        }
                );
    }

    /**
     * Check that chunk contains in any regions.
     *
     * @param pos chunk's position
     * @return {@code true} if chunk in regions, otherwise {@code false}
     */
    public boolean isChunkInRegions(Vector3i pos) {
        for (ChunkRelevanceRegion region : regions.values()) {
            if (region.getCurrentRegion().expand(UNLOAD_LEEWAY).encompasses(pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create comporator for ChunkTasks, which compare by distance from region centers
     *
     * @return Comporator.
     */
    public Comparator<Future<Chunk>> createChunkTaskComporator() {
        return new ChunkTaskRelevanceComparator();
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

    private int regionsDistanceScore(Vector3i chunk) {
        int score = Integer.MAX_VALUE;

        regionLock.readLock().lock();
        try {

            for (ChunkRelevanceRegion region : regions.values()) {
                int dist = distFromRegion(chunk, region.getCenter());
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

    private int distFromRegion(Vector3i pos, Vector3i regionCenter) {
        return pos.gridDistance(regionCenter);
    }

    /**
     * Compare ChunkTasks by distance from region's centers.
     */
    private class ChunkTaskRelevanceComparator implements Comparator<Future<Chunk>> {

        @Override
        public int compare(Future<Chunk> o1, Future<Chunk> o2) {
            return score((PositionFuture<?>) o1) - score((PositionFuture<?>) o2);
        }

        private int score(PositionFuture<?> task) {
            return RelevanceSystem.this.regionsDistanceScore(JomlUtil.from(task.getPosition()));
        }
    }


    /**
     * Compare ChunkTasks by distance from region's centers.
     */
    private class PositionRelevanceComparator implements Comparator<Vector3i> {

        @Override
        public int compare(Vector3i o1, Vector3i o2) {
            return score(o1) - score(o2);
        }

        private int score(Vector3i position) {
            return RelevanceSystem.this.regionsDistanceScore(position);
        }
    }
}
