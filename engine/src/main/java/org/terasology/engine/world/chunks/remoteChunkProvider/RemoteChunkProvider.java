// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.remoteChunkProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.config.Config;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.monitoring.chunk.ChunkMonitor;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.event.BeforeChunkUnload;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import org.terasology.engine.world.chunks.pipeline.ChunkProcessingPipeline;
import org.terasology.engine.world.chunks.pipeline.PositionFuture;
import org.terasology.engine.world.chunks.pipeline.stages.ChunkTaskProvider;
import org.terasology.engine.world.internal.ChunkViewCore;
import org.terasology.engine.world.internal.ChunkViewCoreImpl;
import org.terasology.engine.world.propagation.light.InternalLightProcessor;
import org.terasology.engine.world.propagation.light.LightMerger;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Provides chunks received from remote source.
 * <p>
 * Loading/Unload chunks dependent on {@link org.terasology.engine.network.Server}
 * <p>
 * Produce events:
 * <p>
 * {@link OnChunkLoaded} when chunk received from server and processed.
 * <p>
 * {@link BeforeChunkUnload} when {@link org.terasology.engine.network.Server} send invalidate chunk and chunk removing
 */
public class RemoteChunkProvider implements ChunkProvider {

    private final BlockingQueue<Chunk> readyChunks = Queues.newLinkedBlockingQueue();
    private final BlockingQueue<Vector3ic> invalidateChunks = Queues.newLinkedBlockingQueue();
    private final Map<Vector3ic, Chunk> chunkCache = Maps.newHashMap();
    private final BlockManager blockManager;
    private final ChunkProcessingPipeline loadingPipeline;
    private EntityRef worldEntity = EntityRef.NULL;
    private ChunkReadyListener listener;

    public RemoteChunkProvider(BlockManager blockManager, LocalPlayer localPlayer, Config config) {
        this.blockManager = blockManager;
        loadingPipeline = new ChunkProcessingPipeline(config.getRendering().getChunkThreads(), this::getChunk,
                new LocalPlayerRelativeChunkComparator(localPlayer));

        loadingPipeline.addStage(
            ChunkTaskProvider.create("Chunk generate internal lightning",
                (Consumer<Chunk>) InternalLightProcessor::generateInternalLighting))
            .addStage(ChunkTaskProvider.create("Chunk deflate", Chunk::deflate))
            .addStage(ChunkTaskProvider.createMulti("Light merging",
                chunks -> {
                    Chunk[] localchunks = chunks.toArray(new Chunk[0]);
                    return LightMerger.merge(localchunks);
                }, LightMerger::requiredChunks
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

    public void invalidateChunks(Vector3ic pos) {
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
            worldEntity.send(new OnChunkLoaded(chunk.getPosition()));
        }
    }

    private void checkForUnload() {
        List<Vector3ic> positions = Lists.newArrayListWithCapacity(invalidateChunks.size());
        invalidateChunks.drainTo(positions);
        for (Vector3ic pos : positions) {
            Chunk removed = chunkCache.remove(pos);
            if (removed != null && !removed.isReady()) {
                worldEntity.send(new BeforeChunkUnload(pos));
                removed.dispose();
            }
        }
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        return getChunk(new Vector3i(x, y, z));
    }

    @Override
    public Chunk getChunk(Vector3ic chunkPos) {
        Chunk chunk = chunkCache.get(chunkPos);
        if (chunk != null && chunk.isReady()) {
            return chunk;
        }
        return null;
    }


    @Override
    public boolean isChunkReady(Vector3ic pos) {
        Chunk chunk = chunkCache.get(pos);
        return chunk != null && chunk.isReady();
    }

    @Override
    public void dispose() {
        ChunkMonitor.fireChunkProviderDisposed(this);
        loadingPipeline.shutdown();
    }

    @Override
    public boolean reloadChunk(Vector3ic pos) {
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
    public ChunkViewCore getSubview(BlockRegionc region, Vector3ic offset) {
        Chunk[] chunks = new Chunk[region.getSizeX() * region.getSizeY() * region.getSizeZ()];
        for (Vector3ic chunkPos : region) {
            Chunk chunk = chunkCache.get(chunkPos);
            int index = (chunkPos.x() - region.minX()) + region.getSizeX()
                    * ((chunkPos.z() - region.minZ()) + region.getSizeZ()
                    * (chunkPos.y() - region.minY()));
            chunks[index] = chunk;
        }
        return new ChunkViewCoreImpl(chunks, region, offset, blockManager.getBlock(BlockManager.AIR_ID));
    }

    @Override
    public void setWorldEntity(EntityRef entity) {
        this.worldEntity = entity;
    }

    private static final class LocalPlayerRelativeChunkComparator implements Comparator<Future<Chunk>> {
        private final LocalPlayer localPlayer;

        private LocalPlayerRelativeChunkComparator(LocalPlayer localPlayer) {
            this.localPlayer = localPlayer;
        }

        @Override
        public int compare(Future<Chunk> o1, Future<Chunk> o2) {
            return score((PositionFuture<?>) o1) - score((PositionFuture<?>) o2);
        }

        private int score(PositionFuture<?> task) {
            if (!localPlayer.isValid()) {
                return 1;
            }

            return (int) Chunks.toChunkPos(localPlayer.getPosition(new Vector3f()), new Vector3i()).distance(task.getPosition());
        }
    }
}
