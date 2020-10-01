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

package org.terasology.world.chunks.remoteChunkProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.monitoring.chunk.ChunkMonitor;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.ChunkRegionListener;
import org.terasology.world.chunks.event.BeforeChunkUnload;
import org.terasology.world.chunks.event.OnChunkLoaded;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.world.chunks.pipeline.AbstractChunkTask;
import org.terasology.world.chunks.pipeline.ChunkGenerationPipeline;
import org.terasology.world.chunks.pipeline.ChunkTask;
import org.terasology.world.internal.ChunkViewCore;
import org.terasology.world.internal.ChunkViewCoreImpl;
import org.terasology.world.propagation.light.InternalLightProcessor;
import org.terasology.world.propagation.light.LightMerger;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 *
 */
public class RemoteChunkProvider implements ChunkProvider, GeneratingChunkProvider {

    private static final Logger logger = LoggerFactory.getLogger(RemoteChunkProvider.class);
    private Map<Vector3i, Chunk> chunkCache = Maps.newHashMap();
    private final BlockingQueue<Chunk> readyChunks = Queues.newLinkedBlockingQueue();
    private final BlockingQueue<Vector3i> invalidateChunks = Queues.newLinkedBlockingQueue();
    private List<Chunk> sortedReadyChunks = Lists.newArrayList();
    private ChunkReadyListener listener;
    private EntityRef worldEntity = EntityRef.NULL;

    private BlockManager blockManager;

    private ChunkGenerationPipeline pipeline;

    private LightMerger<Chunk> lightMerger = new LightMerger<>(this);

    private LocalPlayer localPlayer;

    public RemoteChunkProvider(BlockManager blockManager, LocalPlayer localPlayer) {
        this.blockManager = blockManager;
        this.localPlayer = localPlayer;
        pipeline = new ChunkGenerationPipeline(new ChunkTaskRelevanceComparator());
        ChunkMonitor.fireChunkProviderInitialized(this);
    }

    public void subscribe(ChunkReadyListener chunkReadyListener) {
        this.listener = chunkReadyListener;
    }

    public void receiveChunk(final Chunk chunk) {
        pipeline.doTask(new AbstractChunkTask(chunk.getPosition()) {
            @Override
            public String getName() {
                return "Internal Light Generation";
            }

            @Override
            public void run() {
                InternalLightProcessor.generateInternalLighting(chunk);
                chunk.deflate();
                onChunkIsReady(chunk);
            }
        });
    }

    public void invalidateChunks(Vector3i pos) {
        invalidateChunks.offer(pos);
    }


    @Override
    public void completeUpdate() {
        lightMerger.completeMerge().forEach(chunk -> {
            if (chunkCache.containsKey(chunk.getPosition())) {
                chunk.markReady();
                listener.onChunkReady(chunk.getPosition());
                worldEntity.send(new OnChunkLoaded(chunk.getPosition()));
            }
        });
    }

    @Override
    public void beginUpdate() {
        if (listener != null) {
            checkForUnload();
            makeChunksAvailable();
        }
    }

    private void checkForUnload() {
        List<Vector3i> positions = Lists.newArrayListWithCapacity(invalidateChunks.size());
        invalidateChunks.drainTo(positions);
        for (Vector3i pos : positions) {
            Chunk removed = chunkCache.remove(pos);
            if (removed != null && !removed.isReady() && !sortedReadyChunks.remove(removed)) {
                worldEntity.send(new BeforeChunkUnload(pos));
                removed.dispose();
            }
        }
    }

    private void makeChunksAvailable() {
        List<Chunk> newReadyChunks = Lists.newArrayList();
        readyChunks.drainTo(newReadyChunks);
        if (!newReadyChunks.isEmpty()) {
            sortedReadyChunks.addAll(newReadyChunks);
            Collections.sort(sortedReadyChunks, new ReadyChunkRelevanceComparator());
            for (Chunk chunk : newReadyChunks) {
                Chunk oldChunk = chunkCache.put(chunk.getPosition(), chunk);
                if (oldChunk != null) {
                    oldChunk.dispose();
                }
            }
        }
        if (!sortedReadyChunks.isEmpty()) {
            for (int i = sortedReadyChunks.size() - 1; i >= 0; i--) {
                Chunk chunkInfo = sortedReadyChunks.get(i);
                PerformanceMonitor.startActivity("Make Chunk Available");
                if (makeChunkAvailable(chunkInfo)) {
                    sortedReadyChunks.remove(i);
                }
                PerformanceMonitor.endActivity();
            }
        }
    }

    private boolean makeChunkAvailable(final Chunk chunk) {
        for (Vector3i pos : Region3i.createFromCenterExtents(chunk.getPosition(), 1)) {
            if (chunkCache.get(pos) == null) {
                return false;
            }
        }

        lightMerger.beginMerge(chunk, chunk);
        return true;
    }


    @Override
    public Chunk getChunk(int x, int y, int z) {
        return getChunk(new Vector3i(x, y, z));
    }

    @Override
    public Chunk getChunk(Vector3i chunkPos) {
        Chunk chunk = chunkCache.get(chunkPos);
        if (chunk != null && chunk.isReady()) {
            return chunk;
        }
        return null;
    }

    @Override
    public boolean isChunkReady(Vector3i pos) {
        Chunk chunk = chunkCache.get(pos);
        return chunk != null && chunk.isReady();
    }

    @Override
    public void dispose() {
        ChunkMonitor.fireChunkProviderDisposed(this);
        pipeline.shutdown();
        lightMerger.shutdown();
    }

    @Override
    public boolean reloadChunk(Vector3i pos) {
        return false;
    }

    @Override
    public void purgeWorld() {
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Chunk> getAllChunks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void restart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChunkViewCore getLocalView(Vector3i centerChunkPos) {
        Region3i region = Region3i.createFromCenterExtents(centerChunkPos, ChunkConstants.LOCAL_REGION_EXTENTS);
        if (getChunk(centerChunkPos) != null) {
            return createWorldView(region, Vector3i.one());
        }
        return null;
    }

    @Override
    public ChunkViewCore getSubviewAroundBlock(Vector3i blockPos, int extent) {
        Region3i region = ChunkMath.getChunkRegionAroundWorldPos(blockPos, extent);
        return createWorldView(region, new Vector3i(-region.min().x, -region.min().y, -region.min().z));
    }

    @Override
    public ChunkViewCore getSubviewAroundChunk(Vector3i chunkPos) {
        Region3i region = Region3i.createFromCenterExtents(chunkPos, ChunkConstants.LOCAL_REGION_EXTENTS);
        if (getChunk(chunkPos) != null) {
            return createWorldView(region, new Vector3i(-region.min().x, -region.min().y, -region.min().z));
        }
        return null;
    }

    private ChunkViewCore createWorldView(Region3i region, Vector3i offset) {
        Chunk[] chunks = new Chunk[region.sizeX() * region.sizeY() * region.sizeZ()];
        for (Vector3i chunkPos : region) {
            Chunk chunk = chunkCache.get(chunkPos);
            if (chunk == null) {
                return null;
            }
            chunkPos.sub(region.minX(), region.minY(), region.minZ());
            int index = TeraMath.calculate3DArrayIndex(chunkPos, region.size());
            chunks[index] = chunk;
        }
        return new ChunkViewCoreImpl(chunks, region, offset, blockManager.getBlock(BlockManager.AIR_ID));
    }

    @Override
    public void setWorldEntity(EntityRef entity) {
        this.worldEntity = entity;
    }

    @Override
    public void onChunkIsReady(Chunk chunk) {
        try {
            readyChunks.put(chunk);
        } catch (InterruptedException e) {
            logger.warn("Failed to add chunk to ready queue", e);
        }
    }

    @Override
    public Chunk getChunkUnready(Vector3i pos) {
        return chunkCache.get(pos);
    }


    private class ChunkTaskRelevanceComparator implements Comparator<ChunkTask> {

        @Override
        public int compare(ChunkTask o1, ChunkTask o2) {
            return score(o1.getPosition()) - score(o2.getPosition());
        }

        private int score(Vector3i chunk) {
            Vector3i playerChunk = ChunkMath.calcChunkPos(new Vector3i(localPlayer.getPosition(), RoundingMode.HALF_UP));
            return playerChunk.distanceSquared(chunk);
        }
    }

    private class ReadyChunkRelevanceComparator implements Comparator<Chunk> {

        @Override
        public int compare(Chunk o1, Chunk o2) {
            return TeraMath.floorToInt(Math.signum(score(o2.getPosition())) - score(o1.getPosition()));
        }

        private float score(Vector3i chunkPos) {
            Vector3f vec = chunkPos.toVector3f();
            vec.sub(localPlayer.getPosition());
            return vec.lengthSquared();
        }
    }
}
