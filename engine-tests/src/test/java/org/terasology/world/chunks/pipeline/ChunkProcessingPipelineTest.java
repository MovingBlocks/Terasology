// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.assets.management.AssetManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.tiles.NullWorldAtlas;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.chunks.pipeline.stages.ChunkTaskProvider;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ChunkProcessingPipelineTest extends TerasologyTestingEnvironment {

    private final BlockManager blockManager = new BlockManagerImpl(new NullWorldAtlas(),
            CoreRegistry.get(AssetManager.class));
    private final ExtraBlockDataManager extraDataManager = new ExtraBlockDataManager();
    private ChunkProcessingPipeline pipeline;

    @Test
    void simpleProcessingSuccess() throws ExecutionException, InterruptedException, TimeoutException {
        pipeline = new ChunkProcessingPipeline();

        Vector3i chunkPos = new Vector3i(0, 0, 0);
        Chunk chunk = createChunkAt(chunkPos);

        pipeline.addStage(ChunkTaskProvider.create("dummy task", (c) -> c));

        Future<Chunk> chunkFuture = pipeline.invokeGeneratorTask(new Vector3i(0, 0, 0), () -> chunk);
        Chunk chunkAfterProcessing = chunkFuture.get(1, TimeUnit.SECONDS);

        Assertions.assertEquals(chunkAfterProcessing.getPosition(new Vector3i()), chunk.getPosition(new Vector3i()),
                "Chunk after processing must have equals position, probably pipeline lost you chunk");
    }

    @Test
    void simpleStopProcessingSuccess() {
        pipeline = new ChunkProcessingPipeline();

        Vector3i position = new Vector3i(0, 0, 0);
        Chunk chunk = createChunkAt(position);


        pipeline.addStage(ChunkTaskProvider.create("dummy long executing task", (c) -> {
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
            }
            return c;
        }));

        Future<Chunk> chunkFuture = pipeline.invokeGeneratorTask(position, () -> chunk);
        pipeline.stopProcessingAt(position);
        Assertions.assertThrows(
                CancellationException.class,
                () -> chunkFuture.get(1, TimeUnit.SECONDS),
                "chunkFuture must be cancelled, when processing stopped"
        );
    }

    @Test
    void emulateEntityMoving() throws InterruptedException {
        final AtomicReference<Vector3ic> position = new AtomicReference<>();
        Map<Vector3ic, Future<Chunk>> futures = Maps.newHashMap();
        Map<Vector3ic, Chunk> chunkCache = Maps.newConcurrentMap();
        pipeline = new ChunkProcessingPipeline();
        pipeline.addStage(ChunkTaskProvider.create("finish chunk", (c) -> {
            c.markReady();
            chunkCache.put(c.getPosition(new Vector3i()), c);
        }));


        Set<Vector3ic> relativeRegion = Collections.emptySet();
        for (int i = 0; i < 10; i++) {
            position.set(new Vector3i(i, 0, 0));
            Set<Vector3ic> newRegion = getNearChunkPositions(position.get(), 10);
            Sets.difference(newRegion, relativeRegion).forEach(// load new chunks.
                    (pos) -> {
                        Future<Chunk> future = pipeline.invokeGeneratorTask(new Vector3i(pos),
                                () -> createChunkAt(pos));
                        futures.put(pos, future);
                    }
            );

            Sets.difference(relativeRegion, newRegion).forEach(// remove old chunks
                    (pos) -> {
                        chunkCache.remove(pos);
                        if (pipeline.isPositionProcessing(pos)) {
                            pipeline.stopProcessingAt(new Vector3i(pos));
                        }
                    }
            );
            relativeRegion = newRegion;

            Assertions.assertTrue(Sets.difference(chunkCache.keySet(), relativeRegion).isEmpty(), "We must haven't " +
                    "chunks not related to relativeRegion");
            Assertions.assertTrue(Sets.difference(Sets.newHashSet(pipeline.getProcessingPosition()), relativeRegion).isEmpty(),
                    "We must haven't chunks in processing not related to relativeRegion");

            Stream<Future<Chunk>> relativeFutures = relativeRegion.stream().map(futures::get);
            Assertions.assertTrue(
                    relativeFutures.noneMatch(Future::isCancelled),
                    "Relative futures must be not cancelled");

            Stream<Future<Chunk>> nonRelativeFutures =
                    Sets.difference(futures.keySet(), relativeRegion).stream().map(futures::get);
            Assertions.assertTrue(
                    nonRelativeFutures.allMatch((f) -> f.isCancelled() || f.isDone()),
                    "Non relative futures must be cancelled or done");

            Thread.sleep(new Random().nextInt(500)); //think time
        }
    }

    @BeforeEach
    void cleanup() {
        if (pipeline != null) {
            pipeline.shutdown();
        }
    }

    private Set<Vector3ic> getNearChunkPositions(Vector3ic p) {
        return getNearChunkPositions(p, 1);
    }

    private Set<Vector3ic> getNearChunkPositions(Vector3ic p, int distance) {
        Set<Vector3ic> requirements = new HashSet<>();
        for (int x = -distance; x <= distance; x++) {
            for (int y = -distance; y <= distance; y++) {
                requirements.add(new Vector3i(p.x() + x, p.y() + y, p.z()));
            }
        }
        return requirements;
    }

    private ChunkImpl createChunkAt(Vector3ic chunkPos) {
        return new ChunkImpl(chunkPos, blockManager, extraDataManager);
    }

}
