// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks;

import com.google.common.collect.Queues;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.rendering.primitives.ChunkTessellator;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.world.ChunkView;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.internal.PreLodChunk;
import org.terasology.engine.world.generator.ScalableWorldGenerator;
import org.terasology.engine.world.internal.ChunkViewCoreImpl;
import org.terasology.engine.world.propagation.light.InternalLightProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class LodChunkProvider {
    private final ChunkProvider chunkProvider;
    private final BlockManager blockManager;
    private final ExtraBlockDataManager extraDataManager;
    private final ChunkTessellator tessellator;
    private final ScalableWorldGenerator generator;

    private Vector3i center = new Vector3i();
    private ViewDistance viewDistanceSetting = ViewDistance.MODERATE;
    private int chunkLods = 0;
    // The chunks that may be actually loaded.
    private BlockRegion possiblyLoadedRegion = new BlockRegion(BlockRegion.INVALID);
    // The chunks that should be visible, and therefore shouldn't have LOD chunks even if the chunk there hasn't
    // loaded yet.
    private BlockRegion probablyLoadedRegion = new BlockRegion(BlockRegion.INVALID);
    private BlockRegion[] lodRegions = new BlockRegion[0];
    // The sizes of all of the LOD chunks that are meant to exist. All the chunks at the same positions with larger
    // sizes also may exist, but don't always.
    private final Map<Vector3ic, Integer> requiredChunks;
    private final ArrayList<Map<Vector3i, LodChunk>> chunks = new ArrayList<>();
    private final ClosenessComparator nearby;

    // Communication with the generation threads.
    private final PriorityBlockingQueue<Vector3ic> neededChunks;
    private final BlockingQueue<LodChunk> readyChunks = Queues.newLinkedBlockingQueue();
    private final List<Thread> generationThreads = new ArrayList<>();

    public LodChunkProvider(ChunkProvider chunkProvider, BlockManager blockManager, ExtraBlockDataManager extraDataManager,
                            ScalableWorldGenerator generator, ChunkTessellator tessellator) {
        this.chunkProvider = chunkProvider;
        this.blockManager = blockManager;
        this.extraDataManager = extraDataManager;
        this.generator = generator;
        this.tessellator = tessellator;
        this.requiredChunks = new ConcurrentHashMap<>();
        this.nearby = new ClosenessComparator(center);
        this.neededChunks = new PriorityBlockingQueue<>(11, nearby);
        for (int i = 0; i < 4; i++) {
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
                Chunk chunk = new PreLodChunk(scaleDown(pos, scale), blockManager, extraDataManager);
                generator.createChunk(chunk, (1 << scale) * (2f / (Chunks.SIZE_X - 2) + 1));
                InternalLightProcessor.generateInternalLighting(chunk, 1 << scale);
                //tintChunk(chunk);
                ChunkView view = new ChunkViewCoreImpl(new Chunk[]{chunk},
                        new BlockRegion(chunk.getPosition()), new Vector3i(), unloaded);
                ChunkMesh mesh = tessellator.generateMesh(view, 1 << scale, 1);
                readyChunks.add(new LodChunk(pos, mesh, scale));
            }
        } catch (InterruptedException ignored) {
        }
    }

    private void processReadyChunks() {
        while (!readyChunks.isEmpty()) {
            LodChunk chunk = readyChunks.remove();
            Vector3i pos = chunk.getPosition(new Vector3i());
            Integer requiredScale = requiredChunks.get(pos);
            int scale = chunk.scale;
            if (requiredScale != null && requiredScale <= scale) { // The relevant region may have been updated since
                // this chunk was requested.
                chunk.getMesh().updateMesh();
                chunk.getMesh().discardData();
                Vector3i subPos = new Vector3i();
                if (scale > 0) {
                    int subScale = 1 << (scale - 1);
                    for (int dx = 0; dx <= subScale; dx += subScale) {
                        for (int dy = 0; dy <= subScale; dy += subScale) {
                            for (int dz = 0; dz <= subScale; dz += subScale) {
                                pos.add(dx, dy, dz, subPos);
                                if (chunks.get(scale - 1).containsKey(subPos) || scale == 1 && chunkProvider.isChunkReady(subPos)) {
                                    chunk.hiddenness++;
                                }
                            }
                        }
                    }
                } else {
                    chunk.realVersion = chunkProvider.getChunk(pos);
                }
                chunks.get(scale).put(new Vector3i(pos), chunk);
                if (scale < chunkLods) {
                    int mask = ~(1 << scale);
                    LodChunk largerChunk = chunks.get(scale + 1).get(new Vector3i(pos.x & mask, pos.y & mask,
                            pos.z & mask));
                    if (largerChunk != null) {
                        largerChunk.hiddenness++;
                    }
                }
            }
        }
    }

    public void update(Vector3i newCenter) {
        updateRenderableRegion(viewDistanceSetting, chunkLods, newCenter);
        processReadyChunks();
    }

    public void updateRenderableRegion(ViewDistance newViewDistance, int newChunkLods, Vector3i newCenter) {
        this.viewDistanceSetting = newViewDistance;
        this.center = new Vector3i(delay(center.x, newCenter.x), delay(center.y, newCenter.y), delay(center.z, newCenter.z));
        this.chunkLods = newChunkLods;
        this.nearby.pos = center;
        Vector3i viewDistance = new Vector3i(newViewDistance.getChunkDistance()).div(2);
        Vector3i altViewDistance = viewDistance.add(1 - Math.abs(viewDistance.x % 2),
                1 - Math.abs(viewDistance.y % 2), 1 - Math.abs(viewDistance.z % 2), new Vector3i());
        BlockRegion newPossiblyLoadedRegion = new BlockRegion(newCenter).expand(viewDistance);
        BlockRegion newProbablyLoadedRegion = new BlockRegion(newPossiblyLoadedRegion).expand(-1, -1, -1);
        BlockRegion[] newLodRegions = new BlockRegion[newChunkLods == 0 ? 0 : 1 + newChunkLods];
        while (chunks.size() < newLodRegions.length) {
            chunks.add(new ConcurrentHashMap<>());
        }
        while (chunks.size() > newLodRegions.length) {
            for (LodChunk chunk : chunks.remove(chunks.size() - 1).values()) {
                chunk.disposeMesh();
            }
        }
        boolean lodRegionChange = newLodRegions.length != lodRegions.length;
        for (int i = 0; i < newLodRegions.length; i++) {
            if (i == 0) {
                newLodRegions[i] = new BlockRegion(newPossiblyLoadedRegion);
            } else {
                // By making viewDistance odd, we ensure that every time a chunk boundary is crossed, at most a single
                // lodRegion changes (except possibly for lodRegions[0], which is more closely tied to the renderable
                // region).
                newLodRegions[i] = new BlockRegion(scaleDown(center, i)).expand(altViewDistance);
            }
            Vector3i min = newLodRegions[i].getMin(new Vector3i());
            Vector3i max = newLodRegions[i].getMax(new Vector3i());
            newLodRegions[i].addToMin(-Math.abs(min.x % 2), -Math.abs(min.y % 2), -Math.abs(min.z % 2));
            newLodRegions[i].addToMax(1 - Math.abs(max.x % 2), 1 - Math.abs(max.y % 2), 1 - Math.abs(max.z % 2));
            if (!lodRegionChange && !newLodRegions[i].equals(lodRegions[i])) {
                lodRegionChange = true;
            }
        }
        if (lodRegionChange || !newProbablyLoadedRegion.equals(probablyLoadedRegion) || !newPossiblyLoadedRegion.equals(possiblyLoadedRegion)) {
            // Remove previously present chunks.
            Set<Vector3ic> previouslyRequiredChunks = new HashSet<>(requiredChunks.keySet());
            for (Vector3ic pos : previouslyRequiredChunks) {
                int scale = requiredChunks.get(pos);
                // Whether this entry in requiredChunks should be removed entirely (i.e. the chunk at the actually
                // required scale is not at this position).
                boolean gone = false;
                boolean increased = false;
                while (scale < newLodRegions.length && !gone && !newLodRegions[scale].contains(scaleDown(pos, scale))) {
                    LodChunk chunk = chunks.get(scale).get(new Vector3i(pos));
                    if (chunk != null) {
                        chunk.disposeMesh();
                        chunks.get(scale).remove(new Vector3i(pos));
                    }
                    gone = ((pos.x() | pos.y() | pos.z()) & (1 << scale)) != 0;
                    scale++;
                    increased = true;
                }
                if (gone || scale >= newLodRegions.length) {
                    neededChunks.remove(pos);
                    requiredChunks.remove(pos);
                } else if (increased) {
                    LodChunk chunk = chunks.get(scale).get(new Vector3i(pos));
                    if (chunk != null) {
                        requiredChunks.put(new Vector3i(pos), scale);
                        chunk.hiddenness = 0;
                    } else {
                        requiredChunks.remove(pos);
                    }
                }
            }

            // Add new chunks.
            for (int scale = 0; scale < newLodRegions.length; scale++) {
                for (Vector3ic pos : newLodRegions[scale]) {
                    if (
                            scale == 0 && newProbablyLoadedRegion.contains(pos)
                                    || scale == 0 && newPossiblyLoadedRegion.contains(pos) && chunkProvider.isChunkReady(pos)
                                    || scale > 0 && newLodRegions[scale - 1].contains(pos.mul(2, new Vector3i()))
                    ) {
                        continue;
                    }
                    Vector3i globalPos = pos.mul(1 << scale, new Vector3i());
                    Integer previousScale = requiredChunks.get(globalPos);
                    if (previousScale == null || previousScale > scale) {
                        addChunk(globalPos, scale);
                    }
                }
            }
        }
        lodRegions = newLodRegions;
        probablyLoadedRegion = newProbablyLoadedRegion;
        possiblyLoadedRegion = newPossiblyLoadedRegion;
    }

    public void onRealChunkUnloaded(Vector3ic pos) {
        if (chunkLods > 0 && !probablyLoadedRegion.contains(pos) && lodRegions[0].contains(pos) && !requiredChunks.containsKey(pos)) {
            addChunk(pos, 0);
        } else if (chunkLods > 0) {
            LodChunk unscaledChunk = chunks.get(0).get(new Vector3i(pos));
            LodChunk scaledChunk = chunks.get(1).get(new Vector3i(pos.x() & -2, pos.y() & -2, pos.z() & -2));
            if (unscaledChunk != null) {
                unscaledChunk.realVersion = null;
            } else if (scaledChunk != null) {
                scaledChunk.hiddenness--;
            }
        }
    }

    private void addChunk(Vector3ic pos, int scale) {
        requiredChunks.put(new Vector3i(pos), scale);
        neededChunks.add(pos);
    }

    public void onRealChunkLoaded(Vector3ic pos) {
        if (possiblyLoadedRegion.contains(pos) && chunkProvider.isChunkReady(pos) && chunkLods > 0) {
            LodChunk unscaledChunk = chunks.get(0).get(new Vector3i(pos));
            LodChunk scaledChunk = chunks.get(1).get(new Vector3i(pos.x() & -2, pos.y() & -2, pos.z() & -2));
            if (unscaledChunk != null) {
                unscaledChunk.realVersion = chunkProvider.getChunk(pos);
            } else if (scaledChunk != null) {
                scaledChunk.hiddenness++;
            }
        }
    }

    public void addAllChunks(Collection<RenderableChunk> collection) {
        for (Map<Vector3i, LodChunk> chunkMap : chunks) {
            collection.addAll(chunkMap.values());
        }
    }

    public void shutdown() {
        for (Thread thread : generationThreads) {
            thread.interrupt();
        }
        for (Map<Vector3i, LodChunk> chunkMap : chunks) {
            for (LodChunk chunk : chunkMap.values()) {
                chunk.disposeMesh();
            }
        }
    }

    public int getChunkLods() {
        return chunkLods;
    }

    private Vector3i scaleDown(Vector3ic v, int scale) {
        return new Vector3i(v.x() >> scale, v.y() >> scale, v.z() >> scale);
    }

    private int delay(int previous, int target) {
        if (previous < target) {
            return target - 1;
        } else if (previous == target) {
            return target;
        } else {
            return target + 1;
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
