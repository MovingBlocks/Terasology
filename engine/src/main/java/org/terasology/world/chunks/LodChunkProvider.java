// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks;

import com.google.common.collect.Queues;
import gnu.trove.list.TFloatList;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.math.JomlUtil;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.rendering.world.viewDistance.ViewDistance;
import org.terasology.world.ChunkView;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.generation.impl.EntityBufferImpl;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.internal.ChunkViewCoreImpl;
import org.terasology.world.propagation.light.InternalLightProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class LodChunkProvider {
    private static final Logger logger = LoggerFactory.getLogger(LodChunkProvider.class);

    private ChunkProvider chunkProvider;
    private BlockManager blockManager;
    private ExtraBlockDataManager extraDataManager;
    private ChunkTessellator tessellator;
    private WorldGenerator generator;

    private Vector3i center;
    private ViewDistance viewDistanceSetting;
    private int chunkLods;
    private BlockRegion loadedRegion = new BlockRegion(BlockRegion.INVALID); // The chunks that may be actually loaded, which therefore don't need LOD chunks.
    private BlockRegion lodRegion = new BlockRegion(BlockRegion.INVALID);
    private Set<Vector3ic> requiredChunks; // All of the LOD chunks that are meant to exist.
    private Map<Vector3i, LodChunk> chunks;
    private ClosenessComparator nearby;

    // Communication with the generation threads.
    private PriorityBlockingQueue<Vector3ic> neededChunks;
    private BlockingQueue<LodChunk> readyChunks = Queues.newLinkedBlockingQueue();
    private List<Thread> generationThreads = new ArrayList<>();

    public LodChunkProvider(Context context, ChunkTessellator tessellator, ViewDistance viewDistance, int chunkLods, Vector3i center) {
        chunkProvider = context.get(ChunkProvider.class);
        blockManager = context.get(BlockManager.class);
        extraDataManager = context.get(ExtraBlockDataManager.class);
        generator = context.get(WorldGenerator.class);
        this.tessellator = tessellator;
        viewDistanceSetting = viewDistance;
        this.chunkLods = chunkLods;
        this.center = center;
        requiredChunks = new HashSet<>();
        chunks = new HashMap<>();
        nearby = new ClosenessComparator(center);
        neededChunks = new PriorityBlockingQueue<>(11, nearby);
        for (int i = 0; i < 2; i++) {
            Thread thread = new Thread(this::createChunks, "LOD Chunk Generation " + i);
            thread.start();
            generationThreads.add(thread);
        }
    }

    private void createChunks() {
        Block unloaded = blockManager.getBlock(BlockManager.UNLOADED_ID);
        try {
            while (true) { // TODO: add exit condition.
                Vector3ic pos = neededChunks.take();
                Chunk chunk = new ChunkImpl(JomlUtil.from(pos), blockManager, extraDataManager);
                generator.createChunk(chunk, new EntityBufferImpl());
                InternalLightProcessor.generateInternalLighting(chunk);
                tintChunk(chunk);
                ChunkView view = new ChunkViewCoreImpl(new Chunk[]{chunk}, new BlockRegion(chunk.getPosition(new Vector3i())), new Vector3i(), unloaded);
                ChunkMesh mesh = tessellator.generateMesh(view, Chunks.SIZE_Y, 0);
                readyChunks.add(new LodChunk(pos, mesh));
            }
        } catch (InterruptedException ignored) { }
    }

    private void processReadyChunks() {
        while (!readyChunks.isEmpty()) {
            LodChunk chunk = readyChunks.remove();
            Vector3i pos = chunk.getPosition(new Vector3i());
            if (requiredChunks.contains(pos)) { // The relevant region may have been updated since this chunk was requested.
                chunk.getMesh().generateVBOs();
                chunks.put(pos, chunk);
            }
        }
    }

    public void update(Vector3i newCenter) {
        updateRenderableRegion(viewDistanceSetting, chunkLods, newCenter);
        processReadyChunks();
    }

    public void updateRenderableRegion(ViewDistance newViewDistance, int newChunkLods, Vector3i newCenter) {
        viewDistanceSetting = newViewDistance;
        center = newCenter;
        chunkLods = newChunkLods;
        nearby.pos = center;
        Vector3i viewDistance = new Vector3i(newViewDistance.getChunkDistance()).div(2);
        Vector3i lodViewDistance = new Vector3i(viewDistance).mul(chunkLods == 0 ? 0 : 1 << chunkLods);
        BlockRegion newLoadedRegion = new BlockRegion(center).expand(viewDistance.sub(1, 1, 1));
        BlockRegion newLodRegion = new BlockRegion(center).expand(lodViewDistance);
        if (!newLoadedRegion.equals(loadedRegion) || !newLodRegion.equals(lodRegion)) {
            // Remove previously present chunks.
            Iterator<Vector3ic> requiredChunkIt = requiredChunks.iterator();
            while (requiredChunkIt.hasNext()) {
                Vector3ic pos = requiredChunkIt.next();
                if (!newLodRegion.contains(pos)) {
                    // Although removeChunk also includes requiredChunks.remove(pos), it is necessary to actually remove it via the iterator first, to avoid a concurrent modification exception.
                    requiredChunkIt.remove();
                    removeChunk(pos);
                }
            }

            // Add new chunks.
            for (Vector3ic pos : newLodRegion) {
                if (!newLoadedRegion.contains(pos) && !requiredChunks.contains(pos) && chunkProvider.getChunk(pos) == null) {
                    addChunk(new Vector3i(pos));
                }
            }
        }
        lodRegion = newLodRegion;
        loadedRegion = newLoadedRegion;
    }

    public void onRealChunkUnloaded(Vector3ic pos) {
        if (lodRegion.contains(pos) && !loadedRegion.contains(pos)) {
            addChunk(pos);
        }
    }

    private void addChunk(Vector3ic pos) {
        requiredChunks.add(pos);
        neededChunks.add(pos);
    }

    public void onRealChunkLoaded(Vector3ic pos) {
        if (requiredChunks.contains(pos)) {
            removeChunk(pos);
        }
    }

    private void removeChunk(Vector3ic pos) {
        requiredChunks.remove(pos);
        neededChunks.remove(pos);
        LodChunk chunk = chunks.remove(pos);
        if (chunk != null) {
            chunk.disposeMesh();
        }
    }

    public Collection<LodChunk> getChunks() {
        return chunks.values();
    }

    public void shutdown() {
        for (Thread thread : generationThreads) {
            thread.interrupt();
        }
    }

    /**
     * Make the chunk a bit darker, so that it can be visually distinguished from an ordinary chunk.
     */
    private void tintChunk(Chunk chunk) {
        for (Vector3ic pos : Chunks.CHUNK_REGION) {
            chunk.setSunlight(JomlUtil.from(pos), (byte) (0.75f * chunk.getSunlight(JomlUtil.from(pos))));
        }
    }

    private static class ClosenessComparator implements Comparator<Vector3ic> {
        Vector3i pos;

        ClosenessComparator(Vector3i pos) {
            this.pos = pos;
        }

        @Override
        public int compare(Vector3ic x0, Vector3ic x1) {
            return Long.compare(x0.distanceSquared(pos), x1.distanceSquared(pos));
        }
    }
}
