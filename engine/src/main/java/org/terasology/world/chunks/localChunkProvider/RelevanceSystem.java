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
package org.terasology.world.chunks.localChunkProvider;

import com.google.common.collect.Maps;
import org.terasology.engine.modes.loadProcesses.AwaitedLocalCharacterSpawnEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.world.RelevanceRegionComponent;
import org.terasology.world.WorldComponent;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkRegionListener;
import org.terasology.world.chunks.event.BeforeChunkUnload;
import org.terasology.world.chunks.event.OnChunkLoaded;
import org.terasology.world.chunks.internal.ChunkRelevanceRegion;
import org.terasology.world.chunks.internal.ReadyChunkInfo;
import org.terasology.world.chunks.pipeline.ChunkTask;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 */
public class RelevanceSystem implements UpdateSubscriberSystem {

    private static final Vector3i UNLOAD_LEEWAY = Vector3i.one();
    private final ReadWriteLock regionLock = new ReentrantReadWriteLock();
    private final Map<EntityRef, ChunkRelevanceRegion> regions = Maps.newHashMap();
    private LocalChunkProvider chunkProvider;

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
        updateRelevanceEntity(entity, entity.getComponent(RelevanceRegionComponent.class).distance);
    }

    public void updateRelevanceEntity(EntityRef entity, Vector3i distance) {
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

    @ReceiveEvent(components = {RelevanceRegionComponent.class, LocationComponent.class})
    public void onLostRelevanceRegion(BeforeDeactivateComponent event, EntityRef entity) {
        removeRelevanceEntity(entity);
    }

    public void removeRelevanceEntity(EntityRef entity) {
        regionLock.writeLock().lock();
        try {
            regions.remove(entity);
        } finally {
            regionLock.writeLock().unlock();
        }
    }

    @ReceiveEvent(components = WorldComponent.class)
    public void onRemoveChunk(BeforeChunkUnload chunkUnloadEvent, EntityRef worldEntity) {
        for (ChunkRelevanceRegion region : regions.values()) {
            region.chunkUnloaded(chunkUnloadEvent.getChunkPos());
        }
    }

    @ReceiveEvent(components = {WorldComponent.class})
    public void onNewChunk(OnChunkLoaded chunkAvailable, EntityRef worldEntity) {
        for (ChunkRelevanceRegion region : regions.values()) {
            region.checkIfChunkIsRelevant(chunkProvider.getChunk(chunkAvailable.getChunkPos()));
        }
    }



    private void updateRelevance() {
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
        for (Vector3i pos : region.getCurrentRegion()) {
            Chunk chunk = chunkProvider.getChunk(pos);
            if (chunk != null) {
                region.checkIfChunkIsRelevant(chunk);
            } else {
                chunkProvider.createOrLoadChunk(pos);
            }
        }
    }

    public boolean isKeepChunk(Vector3i pos) {
        for (ChunkRelevanceRegion region : regions.values()) {
            if (region.getCurrentRegion().expand(UNLOAD_LEEWAY).encompasses(pos)) {
                return true;
            }
        }
        return false;
    }

    public Comparator<ChunkTask> createChunkTaskComporator() {
        return new ChunkTaskRelevanceComparator();
    }

    public Comparator<ReadyChunkInfo> createReadyChunkInfoComporator() {
        return new ReadyChunkRelevanceComparator();
    }

    @Override
    public void update(float delta) {
        updateRelevance();
    }

    @Override
    public void initialise() {

    }

    @Override
    public void preBegin() {

    }

    @Override
    public void postBegin() {

    }

    @Override
    public void preSave() {

    }

    @Override
    public void postSave() {

    }

    @Override
    public void shutdown() {

    }

    private class ChunkTaskRelevanceComparator implements Comparator<ChunkTask> {

        @Override
        public int compare(ChunkTask o1, ChunkTask o2) {
            return score(o1) - score(o2);
        }

        private int score(ChunkTask task) {
            if (task.isTerminateSignal()) {
                return -1;
            }
            return score(task.getPosition());
        }

        private int score(Vector3i chunk) {
            int score = Integer.MAX_VALUE;

            regionLock.readLock().lock();
            try {
                for (ChunkRelevanceRegion region : regions.values()) {
                    int dist = distFromRegion(chunk, region.getCenter());
                    if (dist < score) {
                        score = dist;
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
    }

    private class ReadyChunkRelevanceComparator implements Comparator<ReadyChunkInfo> {

        @Override
        public int compare(ReadyChunkInfo o1, ReadyChunkInfo o2) {
            return score(o2.getPos()) - score(o1.getPos());
        }

        private int score(Vector3i chunk) {
            int score = Integer.MAX_VALUE;

            regionLock.readLock().lock();
            try {
                for (ChunkRelevanceRegion region : regions.values()) {
                    int dist = distFromRegion(chunk, region.getCenter());
                    if (dist < score) {
                        score = dist;
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
    }
}
