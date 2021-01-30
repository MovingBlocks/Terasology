// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks;

import com.google.common.collect.Queues;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
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
    private BlockRegion[] lodRegions = new BlockRegion[0];
    private Map<Vector3ic, Integer> requiredChunks; // The sizes of all of the LOD chunks that are meant to exist.
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
        requiredChunks = new ConcurrentHashMap<>();
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
            while (true) {
                Vector3ic pos = neededChunks.take();
                Integer scale = requiredChunks.get(pos); // Actually the log scale
                if (scale == null) {
                    // This chunk is being removed in the main thread.
                    continue;
                }
                Chunk chunk = new ChunkImpl(JomlUtil.from(pos).div(1 << scale), blockManager, extraDataManager);
                generator.createChunk(chunk, new EntityBufferImpl());
                InternalLightProcessor.generateInternalLighting(chunk);
                tintChunk(chunk);
                ChunkView view = new ChunkViewCoreImpl(new Chunk[]{chunk}, new BlockRegion(chunk.getPosition(new Vector3i())), new Vector3i(), unloaded);
                ChunkMesh mesh = tessellator.generateMesh(view, Chunks.SIZE_Y, 0, 1 << scale);
                readyChunks.add(new LodChunk(pos, mesh, scale));
            }
        } catch (InterruptedException ignored) { }
    }

    private void processReadyChunks() {
        while (!readyChunks.isEmpty()) {
            LodChunk chunk = readyChunks.remove();
            Vector3i pos = chunk.getPosition(new Vector3i());
            Integer requiredScale = requiredChunks.get(pos);
            if (requiredScale != null && requiredScale == chunk.scale) { // The relevant region may have been updated since this chunk was requested.
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
        BlockRegion newLoadedRegion = new BlockRegion(center).expand(viewDistance).expand(-1, -1, -1);
        BlockRegion[] newLodRegions = new BlockRegion[newChunkLods];
        boolean lodRegionChange = newLodRegions.length != lodRegions.length;
        for (int i = 0; i < newLodRegions.length; i++) {
            if (i == 0) {
                newLodRegions[i] = new BlockRegion(newLoadedRegion);
            } else {
                newLodRegions[i] = new BlockRegion(center.div(1 << i, new Vector3i())).expand(viewDistance);
            }
            Vector3i min = newLodRegions[i].getMin(new Vector3i());
            Vector3i max = newLodRegions[i].getMax(new Vector3i());
            newLodRegions[i].addToMin(-Math.abs(min.x % 2), -Math.abs(min.y % 2), -Math.abs(min.z % 2));
            newLodRegions[i].addToMax( Math.abs(max.x % 2),  Math.abs(max.y % 2),  Math.abs(max.z % 2));
            if (!lodRegionChange && !newLodRegions[i].equals(lodRegions[i])) {
                lodRegionChange = true;
            }
        }
        if (lodRegionChange || !newLoadedRegion.equals(loadedRegion)) {
            // Remove previously present chunks.
            Set<Vector3ic> previouslyRequiredChunks = new HashSet<>(requiredChunks.keySet());
            for (Vector3ic pos : previouslyRequiredChunks) {
                int scale = requiredChunks.get(pos);
                if (scale >= newLodRegions.length || !newLodRegions[scale].contains(pos.div(1 << scale, new Vector3i()))) {
                    removeChunk(pos);
                }
            }

            // Add new chunks.
            for (int scale = 0; scale < newLodRegions.length; scale++) {
                for (Vector3ic pos : newLodRegions[scale]) {
                    if (scale == 0 && newLoadedRegion.contains(pos)) {
                        continue;
                    }
                    Vector3i globalPos = pos.mul(1 << scale, new Vector3i());
                    if (requiredChunks.get(globalPos) == null && chunkProvider.getChunk(globalPos) == null) {
                        addChunk(globalPos, scale);
                    }
                }
            }
        }
        lodRegions = newLodRegions;
        loadedRegion = newLoadedRegion;
    }

    public void onRealChunkUnloaded(Vector3ic pos) {
        if (chunkLods > 0 && !loadedRegion.contains(pos)) {
            Vector3i scaledPos = new Vector3i(pos);
            for (int scale = 0; scale < chunkLods; scale++) {
                if (lodRegions[scale].contains(scaledPos)) {
                    addChunk(pos, scale);
                    return;
                }
                scaledPos.div(2);
            }
        }
    }

    private void addChunk(Vector3ic pos, int scale) {
        requiredChunks.put(pos, scale);
        neededChunks.add(pos);
    }

    public void onRealChunkLoaded(Vector3ic pos) {
        if (requiredChunks.get(pos) != null) {
            removeChunk(pos);
        }
    }

    private void removeChunk(Vector3ic pos) {
        neededChunks.remove(pos);
        requiredChunks.remove(pos);
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

    public int getChunkLods() {
        return chunkLods;
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
