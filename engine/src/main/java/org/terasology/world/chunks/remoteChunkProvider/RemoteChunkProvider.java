// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.remoteChunkProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.ChunkMath;
import org.terasology.math.JomlUtil;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.chunk.ChunkMonitor;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.event.BeforeChunkUnload;
import org.terasology.world.chunks.event.OnChunkLoaded;
import org.terasology.world.chunks.pipeline.ChunkProcessingPipeline;
import org.terasology.world.chunks.pipeline.PositionFuture;
import org.terasology.world.chunks.pipeline.stages.ChunkTaskProvider;
import org.terasology.world.internal.ChunkViewCore;
import org.terasology.world.internal.ChunkViewCoreImpl;
import org.terasology.world.propagation.light.InternalLightProcessor;
import org.terasology.world.propagation.light.LightMerger;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Provides chunks received from remote source.
 * <p>
 * Loading/Unload chunks dependent on {@link org.terasology.network.Server}
 * <p/>
 * Produce events:
 * <p>
 * {@link OnChunkLoaded} when chunk received from server and processed.
 * <p>
 * {@link BeforeChunkUnload} when {@link org.terasology.network.Server} send invalidate chunk and chunk removing
 */
public class RemoteChunkProvider implements ChunkProvider {

    private static final Logger logger = LoggerFactory.getLogger(RemoteChunkProvider.class);
    private final BlockingQueue<Chunk> readyChunks = Queues.newLinkedBlockingQueue();
    private final BlockingQueue<Vector3i> invalidateChunks = Queues.newLinkedBlockingQueue();
    private final Map<Vector3i, Chunk> chunkCache = Maps.newHashMap();
    private final BlockManager blockManager;
    private final ChunkProcessingPipeline loadingPipeline;
    private EntityRef worldEntity = EntityRef.NULL;
    private ChunkReadyListener listener;

    public RemoteChunkProvider(BlockManager blockManager, LocalPlayer localPlayer) {
        this.blockManager = blockManager;
        loadingPipeline = new ChunkProcessingPipeline(this::getChunk,
                new LocalPlayerRelativeChunkComparator(localPlayer));

        loadingPipeline.addStage(
                ChunkTaskProvider.create("Chunk generate internal lightning",
                        InternalLightProcessor::generateInternalLighting))
                .addStage(ChunkTaskProvider.create("Chunk deflate", Chunk::deflate))
                .addStage(ChunkTaskProvider.createMulti("Light merging",
                        chunks -> {
                            Chunk[] localchunks = chunks.toArray(new Chunk[0]);
                            return new LightMerger().merge(localchunks);
                        },
                        pos -> StreamSupport.stream(new BlockRegion(pos).expand(1, 1, 1).spliterator(), false)
                                .map(org.joml.Vector3i::new)
                                .collect(Collectors.toSet())
                ))
                .addStage(ChunkTaskProvider.create("", readyChunks::add));

        ChunkMonitor.fireChunkProviderInitialized(this);
    }

    public void subscribe(ChunkReadyListener chunkReadyListener) {
        this.listener = chunkReadyListener;
    }


    public void receiveChunk(final Chunk chunk) {
        loadingPipeline.invokePipeline(chunk);
    }

    public void invalidateChunks(Vector3i pos) {
        invalidateChunks.offer(pos);
    }

    @Override
    public void update() {
        if (listener != null) {
            checkForUnload();
        }
        Chunk chunk;
        while ((chunk = readyChunks.poll()) != null) {
            Chunk oldChunk = chunkCache.put(chunk.getPosition(), chunk);
            if (oldChunk != null) {
                oldChunk.dispose();
            }
            chunk.markReady();
            if (listener != null) {
                listener.onChunkReady(chunk.getPosition());
            }
            worldEntity.send(new OnChunkLoaded(chunk.getPosition(new org.joml.Vector3i())));
        }
    }

    private void checkForUnload() {
        List<Vector3i> positions = Lists.newArrayListWithCapacity(invalidateChunks.size());
        invalidateChunks.drainTo(positions);
        for (Vector3i pos : positions) {
            Chunk removed = chunkCache.remove(pos);
            if (removed != null && !removed.isReady()) {
                worldEntity.send(new BeforeChunkUnload(JomlUtil.from(pos)));
                removed.dispose();
            }
        }
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
    public boolean isChunkReady(Vector3ic pos) {
        Chunk chunk = chunkCache.get(JomlUtil.from(pos));
        return chunk != null && chunk.isReady();
    }

    @Override
    public void dispose() {
        ChunkMonitor.fireChunkProviderDisposed(this);
        loadingPipeline.shutdown();
    }


    public Chunk getChunk(org.joml.Vector3ic pos) {
        return getChunk(JomlUtil.from(pos));
    }

    @Override
    public boolean reloadChunk(Vector3i pos) {
        return false;
    }

    @Override
    public void purgeWorld() {
        // RemoteChunkProvider is slave of server. It cannot purge world "projection"
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
        return new ChunkViewCoreImpl(chunks, JomlUtil.from(region), JomlUtil.from(offset), blockManager.getBlock(BlockManager.AIR_ID));
    }

    @Override
    public void setWorldEntity(EntityRef entity) {
        this.worldEntity = entity;
    }

    private class LocalPlayerRelativeChunkComparator implements Comparator<Future<Chunk>> {
        private final LocalPlayer localPlayer;

        private LocalPlayerRelativeChunkComparator(LocalPlayer localPlayer) {
            this.localPlayer = localPlayer;
        }

        @Override
        public int compare(Future<Chunk> o1, Future<Chunk> o2) {
            return score((PositionFuture<?>) o1) - score((PositionFuture<?>) o2);
        }

        private int score(PositionFuture<?> task) {
            return (int) ChunkMath.calcChunkPos(JomlUtil.from(localPlayer.getPosition()), new org.joml.Vector3i()).distance(task.getPosition());
        }
    }
}
