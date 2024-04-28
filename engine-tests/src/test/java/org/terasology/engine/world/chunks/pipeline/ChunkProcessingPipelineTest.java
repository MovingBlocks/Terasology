// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.terasology.engine.TerasologyTestingEnvironment;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.internal.BlockManagerImpl;
import org.terasology.engine.world.block.tiles.NullWorldAtlas;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.internal.ChunkImpl;
import org.terasology.engine.world.chunks.pipeline.stages.ChunkTaskProvider;
import org.terasology.gestalt.assets.management.AssetManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Tag("TteTest")
class ChunkProcessingPipelineTest extends TerasologyTestingEnvironment {

    private final BlockManager blockManager = new BlockManagerImpl(new NullWorldAtlas(),
            CoreRegistry.get(AssetManager.class));
    private final ExtraBlockDataManager extraDataManager = new ExtraBlockDataManager();
    private ChunkProcessingPipeline pipeline;

    @Test
    void simpleProcessingSuccess() throws ExecutionException, InterruptedException, TimeoutException {
        pipeline = new ChunkProcessingPipeline(0, (p) -> null, (o1, o2) -> 0);

        Vector3i chunkPos = new Vector3i(0, 0, 0);
        Chunk chunk = createChunkAt(chunkPos);

        pipeline.addStage(ChunkTaskProvider.create("dummy task", (c) -> c));

        Future<Chunk> chunkFuture = pipeline.invokeGeneratorTask(new Vector3i(0, 0, 0), () -> chunk);
        Chunk chunkAfterProcessing = chunkFuture.get(1, TimeUnit.SECONDS);

        Assertions.assertEquals(chunkAfterProcessing.getPosition(), chunk.getPosition(),
                "Chunk after processing must have equals position, probably pipeline lost you chunk");
    }

    @Test
    void simpleStopProcessingSuccess() {
        pipeline = new ChunkProcessingPipeline(0, (p) -> null, (o1, o2) -> 0);

        Vector3i position = new Vector3i(0, 0, 0);
        Chunk chunk = createChunkAt(position);


        pipeline.addStage(ChunkTaskProvider.create("dummy long executing task", (c) -> {
            try {
                Thread.sleep(1_000);
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

    /**
     * Imagine that we have task, which requires neighbors with same Z level. neighbors chunk already in chunk cache.
     */
    @Test
    void multiRequirementsChunksExistsSuccess() throws ExecutionException, InterruptedException, TimeoutException {
        Vector3i positionToGenerate = new Vector3i(0, 0, 0);
        Map<Vector3ic, Chunk> chunkCache =
                getNearChunkPositions(positionToGenerate)
                        .stream()
                        .filter((p) -> !p.equals(positionToGenerate)) //remove central chunk.
                        .map(this::createChunkAt)
                        .collect(Collectors.toMap(
                                ChunkImpl::getPosition,
                                Function.identity()
                        ));

        pipeline = new ChunkProcessingPipeline(0, chunkCache::get, (o1, o2) -> 0);
        pipeline.addStage(ChunkTaskProvider.createMulti(
                "flat merging task",
                (chunks) -> chunks.stream()
                        .filter((c) -> c.getPosition().equals(positionToGenerate))
                        .findFirst() // return central chunk.
                        .get(),
                this::getNearChunkPositions));

        Chunk chunk = createChunkAt(positionToGenerate);
        Future<Chunk> chunkFuture = pipeline.invokeGeneratorTask(new Vector3i(0, 0, 0), () -> chunk);
        Chunk chunkAfterProcessing = chunkFuture.get(1, TimeUnit.SECONDS);

        Assertions.assertEquals(chunkAfterProcessing.getPosition(), chunk.getPosition(),
                "Chunk after processing must have equals position, probably pipeline lost you chunk");
    }

    /**
     * Imagine that we have task, which requires neighbors with same Z level. neighbor will generated.
     */
    @Test
    void multiRequirementsChunksWillGeneratedSuccess() throws ExecutionException, InterruptedException,
            TimeoutException {
        Vector3i positionToGenerate = new Vector3i(0, 0, 0);
        Map<Vector3ic, Chunk> chunkToGenerate =
                getNearChunkPositions(positionToGenerate)
                        .stream()
                        .filter((p) -> !p.equals(positionToGenerate)) //remove central chunk.
                        .map(this::createChunkAt)
                        .collect(Collectors.toMap(
                                ChunkImpl::getPosition,
                                Function.identity()
                        ));

        pipeline = new ChunkProcessingPipeline(0, (p) -> null, (o1, o2) -> 0);
        pipeline.addStage(ChunkTaskProvider.createMulti(
                "flat merging task",
                (chunks) -> chunks.stream()
                        .filter((c) -> c.getPosition().equals(positionToGenerate)).findFirst() // return central chunk.
                        .get(),
                this::getNearChunkPositions));

        Chunk chunk = createChunkAt(positionToGenerate);
        Future<Chunk> chunkFuture = pipeline.invokeGeneratorTask(new Vector3i(0, 0, 0), () -> chunk);

        Thread.sleep(1_000); // sleep 1 second. and check future.
        Assertions.assertFalse(chunkFuture.isDone(), "Chunk must be not generated, because ChunkTask have not exists " +
                "neighbors in requirements");

        chunkToGenerate.forEach((position, neighborChunk) -> pipeline.invokeGeneratorTask(position,
                () -> neighborChunk));

        Chunk chunkAfterProcessing = chunkFuture.get(1, TimeUnit.SECONDS);

        Assertions.assertEquals(chunkAfterProcessing.getPosition(), chunk.getPosition(),
                "Chunk after processing must have equals position, probably pipeline lost you chunk");
    }

    @Test
    void emulateEntityMoving() throws InterruptedException {
        final AtomicReference<Vector3ic> position = new AtomicReference<>();
        Map<Vector3ic, Future<Chunk>> futures = Maps.newHashMap();
        Map<Vector3ic, Chunk> chunkCache = Maps.newConcurrentMap();
        pipeline = new ChunkProcessingPipeline(0, chunkCache::get, (o1, o2) -> {
            if (position.get() != null) {
                Vector3ic entityPos = position.get();
                return (int) (entityPos.distance(((PositionFuture<?>) o1).getPosition())
                            - entityPos.distance(((PositionFuture<?>) o2).getPosition()));
            }
            return 0;
        });
        pipeline.addStage(ChunkTaskProvider.createMulti(
                "flat merging task",
                (chunks) -> chunks.stream()
                        .sorted((o1, o2) -> {
                            Function<Chunk, Vector3ic> pos = Chunk::getPosition;
                            return Comparator.comparing(pos.andThen(Vector3ic::x))
                                    .thenComparing(pos.andThen(Vector3ic::y))
                                    .thenComparing(pos.andThen(Vector3ic::z))
                                    .compare(o1, o2);
                        }).toArray(Chunk[]::new)[5],
                this::getNearChunkPositions));
        pipeline.addStage(ChunkTaskProvider.create("finish chunk", (c) -> {
            c.markReady();
            chunkCache.put(c.getPosition(), c);
        }));


        Set<Vector3ic> relativeRegion = Collections.emptySet();
        for (int i = 0; i < 10; i++) {
            position.set(new Vector3i(i, 0, 0));
            Set<Vector3ic> newRegion = Sets.newHashSet(getNearChunkPositions(position.get(), 10));
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

    private List<Vector3ic> getNearChunkPositions(Vector3ic p) {
        return getNearChunkPositions(p, 1);
    }

    private List<Vector3ic> getNearChunkPositions(Vector3ic p, int distance) {
        List<Vector3ic> requirements = Lists.newArrayListWithCapacity((distance + distance + 1) * (distance + distance + 1));
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
