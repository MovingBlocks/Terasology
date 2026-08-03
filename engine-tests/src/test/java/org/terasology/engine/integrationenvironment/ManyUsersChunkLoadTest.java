// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.particles.components.ParticleEmitterComponent;
import org.terasology.engine.particles.components.affectors.VelocityAffectorComponent;
import org.terasology.engine.particles.components.generators.ColorRangeGeneratorComponent;
import org.terasology.engine.particles.functions.affectors.VelocityAffectorFunction;
import org.terasology.engine.particles.functions.generators.ColorRangeGeneratorFunction;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.chunks.ChunkProvider;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/**
 * Diagnostic load test for https://github.com/MovingBlocks/Terasology/issues/5150.
 * <p>
 * Two things are measured:
 * <ol>
 *     <li>Whether several players exploring different parts of the world at once makes the host's
 *     per-tick chunk processing (see {@code LocalChunkProvider}) slower per-chunk than a single
 *     player doing the same thing alone - i.e. does concurrent <em>generation</em> contend.</li>
 *     <li>How long it takes to reload those same chunks - now populated with persistent entities -
 *     from disk, all at once. This exercises {@code ChunkStoreInternal.restoreEntities()} (see its
 *     own timing/count log line) under concurrent load, which is the path suspected of causing the
 *     multi-second main-thread stalls seen after loading a real saved world.</li>
 * </ol>
 * Not a pass/fail regression test - it reports timings for manual comparison. Re-run with a different
 * {@link #NUM_USERS} or {@link #ENTITIES_PER_USER} to see how the numbers move.
 */
@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
public class ManyUsersChunkLoadTest {
    private static final Logger logger = LoggerFactory.getLogger(ManyUsersChunkLoadTest.class);

    private static final int NUM_USERS = 8;
    // Chunks are 32x64x32 blocks; space "users" 512 blocks apart so their regions (and whatever
    // padding RelevanceSystem adds around them) never overlap and load genuinely distinct chunks.
    private static final int USER_SPACING = 512;
    // Half-width, in blocks, of the area each simulated user forces relevant - a few chunks across.
    private static final int REGION_HALF_SIZE = 48;
    // Persistent entities to place in each user's area, to give restoreEntities() something real to do.
    private static final int ENTITIES_PER_USER = 50;

    // A prior run timed out waiting for concurrent reloads with entities to finish (default 30s game
    // time / 60s safety). Raised here to find out whether that's a severe-but-finite stall or a true
    // hang - see https://github.com/MovingBlocks/Terasology/issues/5150.
    private static final long RELOAD_TIMEOUT_MS = 300_000;

    @Test
    // Heavy: spins up 9 engines (1 host + 8 simulated clients, each its own TerasologyEngine) to
    // simulate 8 players exploring distinct regions concurrently, then bulk-reloads all their chunks
    // from disk at once. Reports timings for manual comparison, not a pass/fail regression check -
    // see class javadoc.
    @Tag("diagnostic")
    public void manySimulatedUsersLoadDifferentAreasConcurrently(ModuleTestingHelper helper, TestReporter reporter)
            throws Exception {
        helper.setSafetyTimeoutMs(RELOAD_TIMEOUT_MS + 60_000);

        // Baseline: how long does a single, uncontended region take to become relevant?
        long baselineStart = System.currentTimeMillis();
        helper.runUntil(helper.makeBlocksRelevant(regionFor(0)));
        long baselineMs = System.currentTimeMillis() - baselineStart;
        reporter.publishEntry("baseline_solo_region_ms", String.valueOf(baselineMs));
        logger.info("Baseline (solo, no other clients): region 0 relevant in {}ms", baselineMs);

        // Connect NUM_USERS clients - real network/entity overhead per "user", same as real players joining.
        long connectStart = System.currentTimeMillis();
        for (int i = 0; i < NUM_USERS; i++) {
            helper.createClient();
        }
        helper.awaitClients(NUM_USERS);
        long connectMs = System.currentTimeMillis() - connectStart;
        reporter.publishEntry("connect_" + NUM_USERS + "_clients_ms", String.valueOf(connectMs));
        logger.info("{} clients connected and registered on host in {}ms", NUM_USERS, connectMs);

        // Now force NUM_USERS distinct, spread-out regions relevant concurrently - simulating each
        // connected user exploring their own area of the world at the same time.
        long concurrentStart = System.currentTimeMillis();
        List<ListenableFuture<ChunkRegionFuture>> futures = new ArrayList<>();
        for (int i = 1; i <= NUM_USERS; i++) {
            futures.add(helper.makeBlocksRelevant(regionFor(i)));
        }
        List<ChunkRegionFuture> regions = helper.runUntil(Futures.allAsList(futures));
        long concurrentMs = System.currentTimeMillis() - concurrentStart;
        reporter.publishEntry("concurrent_" + NUM_USERS + "_regions_ms", String.valueOf(concurrentMs));
        logger.info("{} concurrent regions all relevant in {}ms ({}ms/region average, vs {}ms solo baseline)",
                NUM_USERS, concurrentMs, concurrentMs / NUM_USERS, baselineMs);
        assertThat(concurrentMs).isGreaterThan(0);

        // Populate each user's area with persistent entities (default EntityInfoComponent: persisted,
        // not always-relevant, no owner) so they get bucketed into that chunk's EntityStore on save -
        // see SaveTransaction.createChunkPosToUnsavedOwnerLessEntitiesMap().
        //
        // Bare LocationComponent-only entities turned out too cheap to reproduce the multi-second
        // real-world stalls (0-2ms/chunk even with 25 entities - see prior run). The real log showed
        // repeated "Field rawDataMask#...ParticleSystemFunction will not be serialised" warnings during
        // the stall, which only fire when ParticleEmitterComponent's generatorFunctionMap/
        // affectorFunctionMap (Map<Component, GeneratorFunction/AffectorFunction>) actually hold
        // entries - so give each entity one of each, to walk that same nested-reflection path.
        EntityManager entityManager = helper.getHostContext().get(EntityManager.class);
        for (int i = 1; i <= NUM_USERS; i++) {
            int centerX = i * USER_SPACING;
            for (int e = 0; e < ENTITIES_PER_USER; e++) {
                float offset = e - ENTITIES_PER_USER / 2f;
                ParticleEmitterComponent emitter = new ParticleEmitterComponent();
                emitter.generatorFunctionMap.put(new ColorRangeGeneratorComponent(), new ColorRangeGeneratorFunction());
                emitter.affectorFunctionMap.put(new VelocityAffectorComponent(), new VelocityAffectorFunction());
                entityManager.create(new LocationComponent(new Vector3f(centerX + offset, 40, 0)), emitter);
            }
        }
        logger.info("Created {} persistent particle-emitter entities across {} regions ({} each)",
                NUM_USERS * ENTITIES_PER_USER, NUM_USERS, ENTITIES_PER_USER);

        // Force every chunk in every region to unload (queuing its now-entity-populated state for
        // save, synchronously - see ReadWriteStorageManager.deactivateChunk()) and immediately reload
        // from that saved state - all concurrently. This is the same LocalChunkProvider path a real
        // "host game" on a previously-explored save goes through, and it's what ChunkStoreInternal's
        // restoreEntities() timing/count log line (added for this investigation) reports on.
        ChunkProvider chunkProvider = helper.getHostContext().get(ChunkProvider.class);
        List<Vector3ic> allChunkPositions = new ArrayList<>();
        for (ChunkRegionFuture region : regions) {
            for (Vector3ic chunkPos : region.getChunkRegion()) {
                allChunkPositions.add(new Vector3i(chunkPos));
            }
        }
        logger.info("Reloading {} chunks (unload + immediate reload from disk) across {} regions concurrently",
                allChunkPositions.size(), NUM_USERS);

        long reloadStart = System.currentTimeMillis();
        for (Vector3ic pos : allChunkPositions) {
            chunkProvider.reloadChunk(pos);
        }
        // Poll in short slices rather than one long wait, so we can tell steady-but-slow progress
        // apart from a true stall on a fixed set of chunks (and know which ones, if so).
        long pollIntervalMs = 5_000;
        long elapsedMs = 0;
        List<Vector3ic> stillPending;
        while (true) {
            helper.runUntil(pollIntervalMs, () -> allChunkPositions.stream().allMatch(chunkProvider::isChunkReady));
            elapsedMs = System.currentTimeMillis() - reloadStart;
            stillPending = allChunkPositions.stream().filter(pos -> !chunkProvider.isChunkReady(pos)).toList();
            if (stillPending.isEmpty()) {
                break;
            }
            logger.info("{}ms elapsed: {}/{} chunks still not ready: {}",
                    elapsedMs, stillPending.size(), allChunkPositions.size(), stillPending);
            if (elapsedMs >= RELOAD_TIMEOUT_MS) {
                throw new AssertionError(String.format(
                        "Timed out after %dms with %d/%d chunks still not ready: %s",
                        elapsedMs, stillPending.size(), allChunkPositions.size(), stillPending));
            }
        }
        long reloadMs = System.currentTimeMillis() - reloadStart;
        reporter.publishEntry("reload_" + allChunkPositions.size() + "_chunks_with_entities_ms", String.valueOf(reloadMs));
        logger.info("{} chunks (~{} entities each) reloaded from disk in {}ms ({}ms/chunk average) - "
                        + "see ChunkStoreInternal log lines above for per-chunk restoreEntities() timings",
                allChunkPositions.size(), ENTITIES_PER_USER, reloadMs, reloadMs / allChunkPositions.size());

        assertThat(reloadMs).isGreaterThan(0);
    }

    private static BlockRegion regionFor(int userIndex) {
        int centerX = userIndex * USER_SPACING;
        return new BlockRegion(
                centerX - REGION_HALF_SIZE, 0, -REGION_HALF_SIZE,
                centerX + REGION_HALF_SIZE, 63, REGION_HALF_SIZE);
    }
}
