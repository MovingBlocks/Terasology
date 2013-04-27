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

package org.terasology.world.chunks.localChunkProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.EntityRef;
import org.terasology.engine.CoreRegistry;
import org.terasology.logic.manager.PathManager;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.world.ChunkView;
import org.terasology.world.RegionalChunkView;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.ChunkReadyEvent;
import org.terasology.world.chunks.ChunkRegionListener;
import org.terasology.world.chunks.internal.ChunkRelevanceRegion;
import org.terasology.world.chunks.ChunkStore;
import org.terasology.world.chunks.ChunkUnloadedEvent;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.world.chunks.pipeline.AbstractChunkTask;
import org.terasology.world.chunks.pipeline.ChunkGenerationPipeline;
import org.terasology.world.chunks.pipeline.ChunkTask;
import org.terasology.world.generator.core.ChunkGeneratorManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Immortius
 */
public class LocalChunkProvider implements ChunkProvider, GeneratingChunkProvider {
    // TODO: Dynamically calculate this
    private static final int CACHE_SIZE = (int) (2 * Runtime.getRuntime().maxMemory() / 1048576);

    private static final Logger logger = LoggerFactory.getLogger(LocalChunkProvider.class);

    private ChunkStore farStore;

    private ChunkGenerationPipeline pipeline;
    private ChunkGeneratorManager generator;

    private Map<EntityRef, ChunkRelevanceRegion> regions = Maps.newHashMap();

    private ConcurrentMap<Vector3i, Chunk> nearCache = Maps.newConcurrentMap();

    private final Set<Vector3i> preparingChunks = Sets.newSetFromMap(Maps.<Vector3i, Boolean>newConcurrentMap());
    private final BlockingQueue<Vector3i> readyChunks = Queues.newLinkedBlockingQueue();

    private EntityRef worldEntity = EntityRef.NULL;

    private ReadWriteLock regionLock = new ReentrantReadWriteLock();

    public LocalChunkProvider(ChunkStore farStore, ChunkGeneratorManager generator) {
        this.farStore = farStore;
        this.generator = generator;
        this.pipeline = new ChunkGenerationPipeline(this, generator, new ChunkTaskRelevanceComparator());

        logger.info("CACHE_SIZE = {} for nearby chunks", CACHE_SIZE);
    }

    @Override
    public ChunkView getLocalView(Vector3i centerChunkPos) {
        Region3i region = Region3i.createFromCenterExtents(centerChunkPos, ChunkConstants.LOCAL_REGION_EXTENTS);
        if (getChunk(centerChunkPos) != null) {
            return createWorldView(region, Vector3i.one());
        }
        return null;
    }

    @Override
    public ChunkView getSubviewAroundBlock(Vector3i blockPos, int extent) {
        Region3i region = TeraMath.getChunkRegionAroundBlockPos(blockPos, extent);
        return createWorldView(region, new Vector3i(-region.min().x, 0, -region.min().z));
    }

    @Override
    public ChunkView getSubviewAroundChunk(Vector3i chunkPos) {
        Region3i region = Region3i.createFromCenterExtents(chunkPos, ChunkConstants.LOCAL_REGION_EXTENTS);
        if (getChunk(chunkPos) != null) {
            return createWorldView(region, new Vector3i(-region.min().x, 0, -region.min().z));
        }
        return null;
    }

    private ChunkView createWorldView(Region3i region, Vector3i offset) {
        Chunk[] chunks = new Chunk[region.size().x * region.size().y * region.size().z];
        for (Vector3i chunkPos : region) {
            Chunk chunk = nearCache.get(chunkPos);
            if (chunk == null || chunk.getChunkState().compareTo(Chunk.State.FULL_LIGHT_CONNECTIVITY_PENDING) == -1) {
                return null;
            }
            int index = (chunkPos.x - region.min().x) + region.size().x * (chunkPos.z - region.min().z);
            chunks[index] = chunk;
        }
        return new RegionalChunkView(chunks, region, offset);
    }

    public void setWorldEntity(EntityRef worldEntity) {
        this.worldEntity = worldEntity;
    }

    @Override
    public void addRelevanceEntity(EntityRef entity, int distance) {
        addRelevanceEntity(entity, distance, null);
    }

    @Override
    public void addRelevanceEntity(EntityRef entity, int distance, ChunkRegionListener listener) {
        if (!entity.exists()) {
            return;
        }
        regionLock.readLock().lock();
        try {
            ChunkRelevanceRegion region = regions.get(entity);
            if (region != null) {
                region.setDistance(distance + ChunkConstants.FULL_GENERATION_DISTANCE);
                return;
            }
        } finally {
            regionLock.readLock().unlock();
        }

        ChunkRelevanceRegion region = new ChunkRelevanceRegion(entity, distance + ChunkConstants.FULL_GENERATION_DISTANCE);
        if (listener != null) {
            region.setListener(listener);
        }
        regionLock.writeLock().lock();
        try {
            regions.put(entity, region);
        } finally {
            regionLock.writeLock().unlock();
        }
        for (Vector3i pos : region.getRegion()) {
            Chunk chunk = getChunk(pos);
            if (chunk != null) {
                region.chunkReady(chunk);
            }
        }
        pipeline.requestProduction(region.getRegion().expand(new Vector3i(2, 0, 2)));
    }

    @Override
    public void updateRelevanceEntity(EntityRef entity, int distance) {
        regionLock.readLock().lock();
        try {
            ChunkRelevanceRegion region = regions.get(entity);
            if (region != null) {
                region.setDistance(distance + ChunkConstants.FULL_GENERATION_DISTANCE);
            }
        } finally {
            regionLock.readLock().unlock();
        }
    }

    @Override
    public void removeRelevanceEntity(EntityRef entity) {
        regionLock.writeLock().lock();
        try {
            regions.remove(new ChunkRelevanceRegion(entity, 0));
        } finally {
            regionLock.writeLock().unlock();
        }
    }

    @Override
    public void update() {
        regionLock.readLock().lock();
        try {
            for (ChunkRelevanceRegion chunkRelevanceRegion : regions.values()) {
                chunkRelevanceRegion.update();
                if (chunkRelevanceRegion.isDirty()) {
                    boolean produceChunks = false;
                    for (Vector3i pos : chunkRelevanceRegion.getNeededChunks()) {
                        Chunk chunk = nearCache.get(pos);
                        if (chunk != null && chunk.getChunkState() == Chunk.State.COMPLETE) {
                            chunkRelevanceRegion.chunkReady(chunk);
                        } else {
                            produceChunks = true;
                        }
                    }
                    if (produceChunks) {
                        pipeline.requestProduction(chunkRelevanceRegion.getRegion().expand(new Vector3i(2, 0, 2)));
                    }
                    chunkRelevanceRegion.setUpToDate();
                }
            }

            if (!readyChunks.isEmpty()) {
                List<Vector3i> readyChunkPositions = Lists.newArrayListWithExpectedSize(readyChunks.size());
                readyChunks.drainTo(readyChunkPositions);
                for (Vector3i readyChunkPos : readyChunkPositions) {
                    worldEntity.send(new ChunkReadyEvent(readyChunkPos));
                    Chunk chunk = getChunk(readyChunkPos);
                    for (ChunkRelevanceRegion region : regions.values()) {
                        region.chunkReady(chunk);
                    }
                }
            }


            PerformanceMonitor.startActivity("Review cache size");
            if (nearCache.size() > CACHE_SIZE) {
                logger.debug("Compacting cache");
                Iterator<Vector3i> iterator = nearCache.keySet().iterator();
                while (iterator.hasNext()) {
                    Vector3i pos = iterator.next();
                    boolean keep = false;
                    for (ChunkRelevanceRegion region : regions.values()) {
                        if (region.getRegion().expand(new Vector3i(4, 0, 4)).encompasses(pos)) {
                            keep = true;
                            break;
                        }
                    }
                    if (!keep) {
                        // TODO: need some way to not dispose chunks being edited or processed (or do so safely)
                        Chunk chunk = nearCache.get(pos);
                        if (chunk.isLocked()) {
                            continue;
                        }
                        chunk.lock();
                        try {
                            farStore.put(chunk);
                            iterator.remove();
                            chunk.dispose();
                        } finally {
                            chunk.unlock();
                        }
                        for (ChunkRelevanceRegion region : regions.values()) {
                            region.chunkUnloaded(pos);
                        }
                        worldEntity.send(new ChunkUnloadedEvent(pos));
                    }

                }
            }
            PerformanceMonitor.endActivity();
        } finally {
            regionLock.readLock().unlock();
        }
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        return getChunk(new Vector3i(x, y, z));
    }

    @Override
    public Chunk getChunk(Vector3i pos) {
        Chunk chunk = nearCache.get(pos);
        if (isChunkReady(chunk)) {
            return chunk;
        }
        return null;
    }

    @Override
    public void dispose() {
        pipeline.shutdown();

        for (Chunk chunk : nearCache.values()) {
            farStore.put(chunk);
            chunk.dispose();
        }
        nearCache.clear();

        farStore.dispose();
        String title = CoreRegistry.get(WorldProvider.class).getTitle();
        File chunkFile = new File(PathManager.getInstance().getWorldSavePath(title), title + ".dat");
        try {
            FileOutputStream fileOut = new FileOutputStream(chunkFile);
            BufferedOutputStream bos = new BufferedOutputStream(fileOut);
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(farStore);
            out.close();
            bos.flush();
            bos.close();
            fileOut.close();
        } catch (IOException e) {
            logger.error("Error saving chunks", e);
        }
    }

    @Override
    public float size() {
        return farStore.size();
    }

    @Override
    public ChunkView getViewAround(Vector3i pos) {
        Region3i region = Region3i.createFromCenterExtents(pos, new Vector3i(1, 0, 1));
        Chunk[] chunks = new Chunk[region.size().x * region.size().z];
        for (Vector3i chunkPos : region) {
            Chunk chunk = getChunkForProcessing(chunkPos);
            if (chunk == null) {
                return null;
            }
            int index = (chunkPos.x - region.min().x) + region.size().x * (chunkPos.z - region.min().z);
            chunks[index] = chunk;
        }
        return new RegionalChunkView(chunks, region, Vector3i.one());
    }

    @Override
    public Chunk getChunkForProcessing(Vector3i pos) {
        return nearCache.get(pos);
    }

    @Override
    public void createOrLoadChunk(Vector3i chunkPos) {
        Chunk chunk = nearCache.get(chunkPos);
        if (chunk == null) {
            PerformanceMonitor.startActivity("Check chunk in cache");
            if (preparingChunks.add(chunkPos)) {
                if (farStore.contains(chunkPos)) {
                    pipeline.doTask(new AbstractChunkTask(pipeline, chunkPos, this) {
                        @Override
                        public void enact() {
                            Chunk chunk = farStore.get(getPosition());
                            if (nearCache.putIfAbsent(getPosition(), chunk) != null) {
                                logger.warn("Chunk {} is already in the near cache", getPosition());
                            }
                            preparingChunks.remove(getPosition());
                            if (chunk.getChunkState() == Chunk.State.COMPLETE) {
                                for (Vector3i adjPos : Region3i.createFromCenterExtents(getPosition(), ChunkConstants.LOCAL_REGION_EXTENTS)) {
                                    if (isChunkReady(adjPos)) {
                                        readyChunks.offer(adjPos);
                                    }
                                }
                            }
                            pipeline.requestReview(Region3i.createFromCenterExtents(getPosition(), ChunkConstants.LOCAL_REGION_EXTENTS));
                        }
                    });
                } else {
                    pipeline.doTask(new AbstractChunkTask(pipeline, chunkPos, this) {

                        @Override
                        public void enact() {
                            Chunk chunk = generator.generateChunk(getPosition());
                            if (nearCache.putIfAbsent(getPosition(), chunk) != null) {
                                logger.warn("Chunk {} is already in the near cache", getPosition());
                            }
                            preparingChunks.remove(getPosition());
                            pipeline.requestReview(Region3i.createFromCenterExtents(getPosition(), ChunkConstants.LOCAL_REGION_EXTENTS));
                        }
                    });
                }
            }
            PerformanceMonitor.endActivity();
        }
    }

    @Override
    public void onChunkIsReady(Vector3i position) {
        readyChunks.offer(position);
    }

    @Override
    public boolean isChunkReady(Vector3i pos) {
        return isChunkReady(nearCache.get(pos));
    }

    private boolean isChunkReady(Chunk chunk) {
        if (chunk == null || chunk.getChunkState() != Chunk.State.COMPLETE) {
            return false;
        }
        for (Vector3i adjPos : Region3i.createFromCenterExtents(chunk.getPos(), ChunkConstants.LOCAL_REGION_EXTENTS)) {
            Chunk adjChunk = nearCache.get(adjPos);
            if (adjChunk == null || adjChunk.getChunkState().compareTo(Chunk.State.FULL_LIGHT_CONNECTIVITY_PENDING) == -1) {
                return false;
            }
        }
        return true;
    }

    private class ChunkTaskRelevanceComparator implements Comparator<ChunkTask> {

        @Override
        public int compare(ChunkTask o1, ChunkTask o2) {
            return score(o1.getPosition()) - score(o2.getPosition());
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
