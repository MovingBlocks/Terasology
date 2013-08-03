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

package org.terasology.world.chunks.remoteChunkProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.ChunkMonitor;
import org.terasology.world.ChunkView;
import org.terasology.world.RegionalChunkView;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.ChunkRegionListener;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.world.chunks.pipeline.ChunkGenerationPipeline;
import org.terasology.world.chunks.pipeline.ChunkTask;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * @author Immortius
 */
public class RemoteChunkProvider implements ChunkProvider, GeneratingChunkProvider {

    private static final Logger logger = LoggerFactory.getLogger(RemoteChunkProvider.class);
    private Map<Vector3i, Chunk> chunkCache = Maps.newHashMap();
    private final BlockingQueue<Vector3i> readyChunks = Queues.newLinkedBlockingQueue();
    private ChunkReadyListener listener;

    private ChunkGenerationPipeline pipeline;

    public RemoteChunkProvider() {
        pipeline = new ChunkGenerationPipeline(this, null, new ChunkTaskRelevanceComparator());
        ChunkMonitor.fireChunkProviderInitialized(this);
    }

    public void subscribe(ChunkReadyListener listener) {
        this.listener = listener;
    }

    public void receiveChunk(Chunk chunk) {
        chunkCache.put(chunk.getPos(), chunk);
        pipeline.requestReview(Region3i.createFromCenterExtents(chunk.getPos(), ChunkConstants.LOCAL_REGION_EXTENTS));
    }

    public void invalidateChunks(Vector3i pos) {
        chunkCache.remove(pos);
    }

    @Override
    public void update() {
        if (!readyChunks.isEmpty() && listener != null) {
            List<Vector3i> ready = Lists.newArrayListWithExpectedSize(readyChunks.size());
            readyChunks.drainTo(ready);
            for (Vector3i pos : ready) {
                listener.onChunkReady(pos);
            }
        }
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        return getChunk(new Vector3i(x, y, z));
    }

    @Override
    public Chunk getChunk(Vector3i chunkPos) {
        if (isChunkReady(chunkPos)) {
            return chunkCache.get(chunkPos);
        }
        return null;
    }

    @Override
    public boolean isChunkReady(Vector3i pos) {
        Chunk chunk = chunkCache.get(pos);
        return chunk != null && chunk.getChunkState() == Chunk.State.COMPLETE;
    }

    @Override
    public void dispose() {
        ChunkMonitor.fireChunkProviderDisposed(this);
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
            Chunk chunk = chunkCache.get(chunkPos);
            if (chunk == null || chunk.getChunkState().compareTo(Chunk.State.FULL_LIGHT_CONNECTIVITY_PENDING) == -1) {
                return null;
            }
            int index = (chunkPos.x - region.min().x) + region.size().x * (chunkPos.z - region.min().z);
            chunks[index] = chunk;
        }
        return new RegionalChunkView(chunks, region, offset);
    }

    @Override
    public void setWorldEntity(EntityRef entity) {

    }

    @Override
    public void addRelevanceEntity(EntityRef entity, int distance) {
    }

    @Override
    public void addRelevanceEntity(EntityRef entity, int distance, ChunkRegionListener listener) {
    }

    @Override
    public void updateRelevanceEntity(EntityRef entity, int distance) {
    }

    @Override
    public void removeRelevanceEntity(EntityRef entity) {

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
        return chunkCache.get(pos);
    }

    @Override
    public void createOrLoadChunk(Vector3i position) {
    }

    @Override
    public void onChunkIsReady(Vector3i position) {
        try {
            readyChunks.put(position);
        } catch (InterruptedException e) {
            logger.warn("Failed to add chunk to ready queue", e);
        }
    }

    private class ChunkTaskRelevanceComparator implements Comparator<ChunkTask> {

        private LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);

        @Override
        public int compare(ChunkTask o1, ChunkTask o2) {
            return score(o1.getPosition()) - score(o2.getPosition());
        }

        private int score(Vector3i chunk) {
            Vector3i playerChunk = TeraMath.calcChunkPos(new Vector3i(localPlayer.getPosition(), 0.5f));
            return playerChunk.distanceSquared(chunk);
        }
    }
}
