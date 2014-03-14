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
import org.terasology.registry.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.ChunkMonitor;
import org.terasology.world.chunks.pipeline.InternalLightingChunkTask;
import org.terasology.world.internal.ChunkViewCore;
import org.terasology.world.internal.ChunkViewCoreImpl;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.ChunkRegionListener;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.world.chunks.pipeline.ChunkGenerationPipeline;
import org.terasology.world.chunks.pipeline.ChunkTask;
import org.terasology.world.generator.internal.RemoteWorldGenerator;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.propagation.BatchPropagator;
import org.terasology.world.propagation.PropagationRules;
import org.terasology.world.propagation.PropagatorWorldView;
import org.terasology.world.propagation.StandardBatchPropagator;
import org.terasology.world.propagation.SunlightRegenBatchPropagator;
import org.terasology.world.propagation.light.LightPropagationRules;
import org.terasology.world.propagation.light.LightWorldView;
import org.terasology.world.propagation.light.SunlightPropagationRules;
import org.terasology.world.propagation.light.SunlightRegenPropagationRules;
import org.terasology.world.propagation.light.SunlightRegenWorldView;
import org.terasology.world.propagation.light.SunlightWorldView;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * @author Immortius
 */
public class RemoteChunkProvider implements ChunkProvider, GeneratingChunkProvider {

    private static final Logger logger = LoggerFactory.getLogger(RemoteChunkProvider.class);
    private Map<Vector3i, ChunkImpl> chunkCache = Maps.newHashMap();
    private final BlockingQueue<Vector3i> readyChunks = Queues.newLinkedBlockingQueue();
    private ChunkReadyListener listener;

    private ChunkGenerationPipeline pipeline;
    private List<BatchPropagator> loadEdgePropagators = Lists.newArrayList();

    private RemoteWorldGenerator remoteWorldGenerator;

    public RemoteChunkProvider() {
        pipeline = new ChunkGenerationPipeline(new ChunkTaskRelevanceComparator());
        loadEdgePropagators.add(new StandardBatchPropagator(new LightPropagationRules(), new LightWorldView(this)));
        PropagatorWorldView regenWorldView = new SunlightRegenWorldView(this);
        PropagationRules sunlightRules = new SunlightPropagationRules(regenWorldView);
        PropagatorWorldView sunlightWorldView = new SunlightWorldView(this);
        BatchPropagator sunlightPropagator = new StandardBatchPropagator(sunlightRules, sunlightWorldView);
        loadEdgePropagators.add(new SunlightRegenBatchPropagator(new SunlightRegenPropagationRules(), regenWorldView, sunlightPropagator, sunlightWorldView));
        loadEdgePropagators.add(sunlightPropagator);
        ChunkMonitor.fireChunkProviderInitialized(this);

        remoteWorldGenerator = new RemoteWorldGenerator();
    }

    public void subscribe(ChunkReadyListener chunkReadyListener) {
        this.listener = chunkReadyListener;
    }

    public void receiveChunk(ChunkImpl chunk) {
        chunkCache.put(chunk.getPos(), chunk);
        pipeline.doTask(new InternalLightingChunkTask(pipeline, chunk.getPos(), this));
    }

    public void invalidateChunks(Vector3i pos) {
        chunkCache.remove(pos);
    }

    @Override
    public void update() {
        if (listener != null) {
            Vector3i pos = readyChunks.poll();
            if (pos != null) {
                ChunkImpl chunk = chunkCache.get(pos);
                chunk.markReady();
                for (Side side : Side.horizontalSides()) {
                    Vector3i adjChunkPos = side.getAdjacentPos(pos);
                    ChunkImpl adjChunk = getChunk(adjChunkPos);
                    if (adjChunk != null) {
                        for (BatchPropagator propagator : loadEdgePropagators) {
                            propagator.propagateBetween(chunk, adjChunk, side, true);
                        }
                    }
                }
                for (BatchPropagator propagator : loadEdgePropagators) {
                    propagator.process();
                }
                listener.onChunkReady(pos);
            }
        }
    }

    @Override
    public ChunkImpl getChunk(int x, int y, int z) {
        return getChunk(new Vector3i(x, y, z));
    }

    @Override
    public ChunkImpl getChunk(Vector3i chunkPos) {
        if (isChunkReady(chunkPos)) {
            return chunkCache.get(chunkPos);
        }
        return null;
    }

    @Override
    public boolean isChunkReady(Vector3i pos) {
        ChunkImpl chunk = chunkCache.get(pos);
        return chunk != null && chunk.getChunkState() == ChunkImpl.State.COMPLETE;
    }

    @Override
    public void dispose() {
        ChunkMonitor.fireChunkProviderDisposed(this);
    }

    @Override
    public void purgeChunks() {
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
        Region3i region = TeraMath.getChunkRegionAroundWorldPos(blockPos, extent);
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
        ChunkImpl[] chunks = new ChunkImpl[region.size().x * region.size().y * region.size().z];
        for (Vector3i chunkPos : region) {
            ChunkImpl chunk = chunkCache.get(chunkPos);
            if (chunk == null || chunk.getChunkState() != ChunkImpl.State.COMPLETE) {
                return null;
            }
            chunkPos.sub(region.min());
            int index = TeraMath.calculate3DArrayIndex(chunkPos, region.size());
            chunks[index] = chunk;
        }
        return new ChunkViewCoreImpl(chunks, region, offset);
    }

    @Override
    public void setWorldEntity(EntityRef entity) {

    }

    @Override
    public void addRelevanceEntity(EntityRef entity, Vector3i distance) {
    }

    @Override
    public void addRelevanceEntity(EntityRef entity, Vector3i distance, ChunkRegionListener chunkRegionListener) {
    }

    @Override
    public void updateRelevanceEntity(EntityRef entity, Vector3i distance) {
    }

    @Override
    public void removeRelevanceEntity(EntityRef entity) {

    }

    @Override
    public ChunkImpl getChunkForProcessing(Vector3i pos) {
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

    @Override
    public WorldGenerator getWorldGenerator() {
        //TODO: send this information over the wire
        return remoteWorldGenerator;
    }

    private static class ChunkTaskRelevanceComparator implements Comparator<ChunkTask> {

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
