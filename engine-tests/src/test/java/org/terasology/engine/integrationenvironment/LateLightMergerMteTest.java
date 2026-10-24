// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EntityScope;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.localChunkProvider.RelevanceSystem;
import org.terasology.unittest.worlds.DummyWorldGenerator;

import java.util.stream.StreamSupport;

/**
 * Pins down the central behaviour change of {@code LateLightMerger}: a chunk becomes ready as soon
 * as its own generation finishes, not only once its whole 3x3x3 neighbourhood does too.
 * <p>
 * Under the pipeline stage this replaced, a chunk whose neighbours were never requested could only
 * become ready via {@code ChunkProcessingPipeline}'s idle-skip timeout - two consecutive 5s-idle
 * polls, so ~10s at best (see {@code POLL_INTERVAL_MS} / {@code IDLE_POLLS_BEFORE_SKIP} there). Both
 * tests below bound completion well under that, on relevance requests deliberately built so the
 * requested chunk(s)' own neighbours are never themselves requested. That makes them fail loudly
 * (timeout) rather than merely slowly against the old, pipeline-stage merge - see the task/PR notes
 * for the actual before/after timings observed when checking that.
 * <p>
 * Deliberately not asserted here: that a requested chunk's neighbours stay unloaded. They usually do,
 * but {@code RelevanceSystem.addRelevanceEntity}'s own {@code .sorted()} pass over a
 * {@code BlockRegion}'s iterator can request one extra, wrong position - a pre-existing aliasing bug
 * (the iterator hands out a reused, mutable {@code Vector3i} that a later {@code hasNext()} call can
 * mutate out from under a caller that buffers rather than immediately consumes it), unrelated to
 * light merging. {@code RelevanceSystem.updateRelevance()}'s follow-up pass - which uses the
 * defensive-copying {@code ChunkRelevanceRegion.getNeededChunks()} instead - still requests the
 * correct position(s) a tick later, so it doesn't affect these tests' timing, but it does mean a
 * "neighbours were never loaded" assertion is not reliable and was left out rather than pinned to
 * today's incidental behaviour of an unrelated bug.
 *
 * @see org.terasology.engine.world.chunks.LateLightMerger
 */
@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
class LateLightMergerMteTest {

    /**
     * Comfortably above what one dummy-world chunk takes to generate, comfortably below the ~10s
     * ChunkProcessingPipeline idle-skip the old pipeline-stage merge needed whenever a chunk's
     * neighbours were never requested.
     */
    private static final long READY_TIMEOUT_MS = 8000;

    @Test
    void chunkBecomesReadyWithoutNeighbourhoodLoaded(EntityManager entityManager, RelevanceSystem relevanceSystem,
            MainLoop mainLoop, ChunkProvider chunkProvider) {
        // Far from spawn (a fixed (0,0,0) for DummyWorldGenerator) and from the other test below, so
        // nothing else ever requests this position or its neighbours.
        Vector3fc center = new Vector3f(200_000, DummyWorldGenerator.SURFACE_HEIGHT, 200_000);
        Vector3i chunkPos = Chunks.toChunkPos(center, new Vector3i());

        EntityRef entity = entityManager.create(new LocationComponent(center));
        entity.setScope(EntityScope.GLOBAL);
        // distance (1,1,1) requests relevance for exactly this one chunk - unlike
        // ChunkRegionFuture, no margin, so its neighbours are never deliberately requested.
        relevanceSystem.addRelevanceEntity(entity, new Vector3i(1, 1, 1), null);

        mainLoop.awaitUntil(READY_TIMEOUT_MS, "an isolated chunk (no neighbours requested) to become ready",
                () -> chunkProvider.isChunkReady(chunkPos));
    }

    @Test
    void relevanceRegionBecomesFullyReadyWithoutMargin(EntityManager entityManager, RelevanceSystem relevanceSystem,
            MainLoop mainLoop, ChunkProvider chunkProvider) {
        // ChunkRegionFuture.REQUIRED_CHUNK_MARGIN pads every relevance request by one extra shell of
        // chunks, specifically so the requested region's own outer shell has its neighbourhood
        // requested too (see its FIXME comment). Going straight to RelevanceSystem instead of through
        // ChunkRegionFuture, with no padding at all, tests whether that padding is still needed now
        // that readiness no longer waits on the neighbourhood.
        Vector3fc center = new Vector3f(300_000, DummyWorldGenerator.SURFACE_HEIGHT, 300_000);

        EntityRef entity = entityManager.create(new LocationComponent(center));
        entity.setScope(EntityScope.GLOBAL);
        BlockRegionc region = relevanceSystem.addRelevanceEntity(entity, new Vector3i(3, 3, 3), null);

        mainLoop.awaitUntil(READY_TIMEOUT_MS, "every chunk in an unpadded 3x3x3 relevance region to become ready",
                () -> StreamSupport.stream(region.spliterator(), false).allMatch(chunkProvider::isChunkReady));
    }
}
