// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks;

import com.google.common.collect.Maps;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.terasology.engine.TerasologyTestingEnvironment;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.internal.BlockManagerImpl;
import org.terasology.engine.world.block.tiles.NullWorldAtlas;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.internal.ChunkImpl;
import org.terasology.engine.world.propagation.light.LightMerger;
import org.terasology.gestalt.assets.management.AssetManager;

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

/**
 * {@link LateLightMerger} is driven directly here rather than through an {@code IntegrationEnvironment}
 * - its constructor takes a plain {@code Map<Vector3ic, Chunk>}, so the bookkeeping can be tested
 * without a running engine. Real {@link ChunkImpl} chunks (rather than a bare stub) are used because
 * {@link LightMerger#merge} does genuine light propagation and needs real block/light storage - see
 * {@code BetweenChunkPropagationTest} for the same pattern.
 */
@Tag("TteTest")
class LateLightMergerTest extends TerasologyTestingEnvironment {

    private BlockManagerImpl blockManager;
    private ExtraBlockDataManager extraDataManager;

    @BeforeEach
    @Override
    public void setup() throws Exception {
        super.setup();
        blockManager = new BlockManagerImpl(new NullWorldAtlas(), CoreRegistry.get(AssetManager.class), true);
        extraDataManager = new ExtraBlockDataManager();
    }

    private Chunk createChunkAt(Vector3ic pos) {
        return new ChunkImpl(new Vector3i(pos), blockManager, extraDataManager);
    }

    /**
     * Covers a bug found and fixed in review, with no prior coverage: {@link LateLightMerger#mergeAt}
     * re-checks the neighbourhood at merge time, not just at queue time in {@link
     * LateLightMerger#chunkReady}. A position that loses a neighbour in between must go back into
     * {@code needsMerging} rather than being dropped - it is queued from neither bookkeeping set
     * otherwise, and only a chunk becoming ready ever re-queues anything, so it would stay unmerged
     * forever even once the neighbour comes back.
     */
    @Test
    void positionRequeuedWhenNeighbourGoesMissingBeforeMergeRuns() {
        Vector3ic center = new Vector3i(0, 0, 0);
        Vector3ic missingNeighbour = new Vector3i(1, 0, 0);

        Map<Vector3ic, Chunk> chunkCache = Maps.newHashMap();
        for (Vector3ic pos : LightMerger.requiredChunks(center)) {
            chunkCache.put(new Vector3i(pos), createChunkAt(pos));
        }

        // The merge only writes - and so only dirties - where light actually moves, so an entirely
        // uniform neighbourhood would merge to no observable effect at all. Light the face of the
        // +X neighbour that abuts the center chunk, giving the merge something to propagate inwards.
        Chunk litNeighbour = chunkCache.get(missingNeighbour);
        for (int y = 0; y < 4; y++) {
            for (int z = 0; z < 4; z++) {
                litNeighbour.setLight(0, y, z, (byte) 15);
            }
        }
        // ChunkImpl starts dirty (it still needs its first mesh); clear that so isDirty() below is a
        // clean signal for "the merge wrote here", not construction noise.
        chunkCache.values().forEach(chunk -> chunk.setDirty(false));

        LateLightMerger merger = new LateLightMerger(chunkCache);

        // Full neighbourhood already present, so this discovers it and queues center for merging.
        merger.chunkReady(center);

        // A neighbour unloads before the merge actually runs - checkForUnload() runs every tick in
        // both providers, ahead of processPending().
        Chunk removedNeighbour = chunkCache.remove(missingNeighbour);
        merger.chunkUnloaded(missingNeighbour);

        merger.processPending();

        // mergeAt() must have found the hole and backed off rather than merging with it.
        assertThat(chunkCache.get(center).isDirty()).isFalse();

        // The neighbour reloads. Nothing but a chunkReady() call ever re-discovers a completed
        // neighbourhood - if mergeAt() had dropped center instead of requeuing it, this would never
        // recover it and the assertions below would fail.
        chunkCache.put(new Vector3i(missingNeighbour), removedNeighbour);
        merger.chunkReady(missingNeighbour);
        merger.processPending();

        // The seeded light has now propagated into the center chunk, which is both the proof that
        // mergeAt() ran for it and the reason ChunkMeshWorker will re-mesh it.
        assertThat(chunkCache.get(center).isDirty()).isTrue();
    }
}
