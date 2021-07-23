// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

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
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Tag("TteTest")
class ChunkProcessingPipelineTest extends TerasologyTestingEnvironment {

    private final BlockManager blockManager = new BlockManagerImpl(new NullWorldAtlas(),
            CoreRegistry.get(AssetManager.class));
    private final ExtraBlockDataManager extraDataManager = new ExtraBlockDataManager();
    private ChunkProcessingPipeline pipeline;
    private FluxSink<Chunk> chunkSink;

    void initPipeline(Function<Vector3ic, Chunk> getCached) {
        // Use a custom scheduler which runs everything immediately, on the same thread
        pipeline = new ChunkProcessingPipeline(getCached, Flux.push(sink -> chunkSink = sink), new Scheduler() {
            @Override
            public Disposable schedule(Runnable task) {
                task.run();
                return Disposables.single();
            }

            @Override
            public Worker createWorker() {
                return new Worker() {
                    @Override
                    public Disposable schedule(Runnable task) {
                        task.run();
                        return Disposables.single();
                    }

                    @Override
                    public void dispose() {

                    }
                };
            }
        });
    }

    @Test
    void simpleProcessingSuccess() throws InterruptedException {
        initPipeline(p -> null);

        Vector3i chunkPos = new Vector3i(0, 0, 0);
        Chunk chunk = createChunkAt(chunkPos);
        List<Chunk> result = new ArrayList<>();

        pipeline.addStage(ChunkTaskProvider.create("dummy task", (c) -> c));
        pipeline.addStage(ChunkTaskProvider.create("Chunk ready", (Consumer<Chunk>) result::add));

        chunkSink.next(chunk);
        chunkSink.complete();
        pipeline.notifyUpdate();

        Chunk chunkAfterProcessing = result.get(0);

        Assertions.assertEquals(chunkAfterProcessing.getPosition(), chunk.getPosition(),
                "Chunk after processing must have the same position, the pipeline probably lost your chunk");
    }

    @Test
    void simpleStopProcessingSuccess() {
        initPipeline(p -> null);

        Vector3i position = new Vector3i(0, 0, 0);
        Chunk chunk = createChunkAt(position);

        pipeline.addStage(ChunkTaskProvider.create("dummy long executing task", (c) -> {
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
            }
            return c;
        }));

        chunkSink.next(chunk);
        chunkSink.complete();
        pipeline.notifyUpdate();

        pipeline.stopProcessingAt(position);
        Assertions.assertFalse(pipeline.isPositionProcessing(position));
    }

    /**
     * Imagine that we have task, which requires neighbors with same Z level. neighbors chunk already in chunk cache.
     */
    @Test
    void multiRequirementsChunksExistsSuccess() throws InterruptedException {
        Vector3i positionToGenerate = new Vector3i(0, 0, 0);
        Map<Vector3ic, Chunk> chunkCache =
                getNearChunkPositions(positionToGenerate)
                        .stream()
                        .filter((p) -> !p.equals(positionToGenerate)) //remove central chunk.
                        .map(this::createChunkAt)
                        .collect(Collectors.toMap(
                                (chunk) -> chunk.getPosition(new Vector3i()),
                                Function.identity()
                        ));

        initPipeline(chunkCache::get);
        pipeline.addStage(ChunkTaskProvider.createMulti(
                "flat merging task",
                (chunks) -> chunks.stream()
                        .filter((c) -> c.getPosition().equals(positionToGenerate))
                        .findFirst() // return central chunk.
                        .get(),
                this::getNearChunkPositions));
        List<Chunk> result = new ArrayList<>();
        pipeline.addStage(ChunkTaskProvider.create("Chunk ready", (Consumer<Chunk>) result::add));

        Chunk chunk = createChunkAt(positionToGenerate);

        chunkSink.next(chunk);
        chunkSink.complete();
        pipeline.notifyUpdate();

        Chunk chunkAfterProcessing = result.get(0);

        Assertions.assertEquals(chunkAfterProcessing.getPosition(), chunk.getPosition(),
                "Chunk after processing must have the same position, the pipeline probably lost your chunk");
    }

    /**
     * Imagine that we have task, which requires neighbors with same Z level. neighbor will generated.
     */
    @Test
    void multiRequirementsChunksWillGeneratedSuccess() throws InterruptedException {
        Vector3i positionToGenerate = new Vector3i(0, 0, 0);
        List<Chunk> chunkToGenerate =
                getNearChunkPositions(positionToGenerate)
                        .stream()
                        .filter((p) -> !p.equals(positionToGenerate)) //remove central chunk.
                        .map(this::createChunkAt)
                        .collect(Collectors.toList());

        initPipeline(p -> null);
        pipeline.addStage(ChunkTaskProvider.createMulti(
                "flat merging task",
                (chunks) -> chunks.stream()
                        .filter((c) -> c.getPosition().equals(positionToGenerate)).findFirst() // return central chunk.
                        .get(),
                this::getNearChunkPositions));
        List<Chunk> result = new ArrayList<>();
        pipeline.addStage(ChunkTaskProvider.create("Chunk ready", (Consumer<Chunk>) result::add));

        Chunk chunk = createChunkAt(positionToGenerate);

        chunkSink.next(chunk);


        Thread.sleep(1_000); // sleep 1 second. and check future.
        Assertions.assertTrue(result.isEmpty(), "Chunk must be not generated, because the ChunkTask have doesn't have " +
                "its neighbors in requirements");

        chunkToGenerate.forEach(chunkSink::next);
        chunkSink.complete();
        pipeline.notifyUpdate();

        Chunk chunkAfterProcessing = result.get(0);

        Assertions.assertEquals(chunkAfterProcessing.getPosition(), chunk.getPosition(),
                "Chunk after processing must have the same position, the pipeline probably lost your chunk");
    }

    @Test
    void emulateEntityMoving() throws InterruptedException {
        final AtomicReference<Vector3ic> position = new AtomicReference<>();
        Map<Vector3ic, Chunk> chunkCache = Maps.newConcurrentMap();
        initPipeline(chunkCache::get);
        pipeline.addStage(ChunkTaskProvider.createMulti(
                "flat merging task",
                (chunks) -> chunks.stream()
                        .sorted((o1, o2) -> {
                            Function<Chunk, Vector3i> pos = (c) -> c.getPosition(new Vector3i());
                            return Comparator.comparing(pos.andThen(Vector3i::x))
                                    .thenComparing(pos.andThen(Vector3i::y))
                                    .thenComparing(pos.andThen(Vector3i::z))
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
            Set<Vector3ic> newRegion = getNearChunkPositions(position.get(), 10);
            // load new chunks.
            Sets.difference(newRegion, relativeRegion).forEach((pos) -> chunkSink.next(createChunkAt(pos)));

            Sets.difference(relativeRegion, newRegion).forEach(// remove old chunks
                    (pos) -> {
                        chunkCache.remove(pos);
                        if (pipeline.isPositionProcessing(pos)) {
                            pipeline.stopProcessingAt(new Vector3i(pos));
                        }
                    }
            );
            relativeRegion = newRegion;

            pipeline.notifyUpdate();

            Assertions.assertTrue(Sets.difference(chunkCache.keySet(), relativeRegion).isEmpty(), "We must haven't " +
                    "chunks not related to relativeRegion");
            Assertions.assertTrue(Sets.difference(pipeline.getProcessingPositions(), relativeRegion).isEmpty(),
                    "We must haven't chunks in processing not related to relativeRegion");

            Assertions.assertTrue(relativeRegion.containsAll(pipeline.getProcessingPositions()),
                    "No non-relative chunks should be processing");

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
