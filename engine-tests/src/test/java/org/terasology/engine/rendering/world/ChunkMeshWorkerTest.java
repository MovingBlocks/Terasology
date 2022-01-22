// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.world;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.RenderableChunk;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ChunkMeshWorkerTest {
    static Vector3ic position0 = new Vector3i(123, 456, 789);

    final Vector3i currentPosition = new Vector3i(position0);

    ChunkMeshWorker worker;
    Comparator<RenderableChunk> comparator;
    StepVerifier.FirstStep<Chunk> verifier;

    static Mono<Tuple2<Chunk, ChunkMesh>> alwaysCreateMesh(Chunk chunk) {
        chunk.setDirty(false);
        return Mono.just(Tuples.of(chunk, mock(ChunkMesh.class)));
    }

    @BeforeEach
    void makeWorker() {
        comparator = Comparator.comparingDouble(chunk ->
                chunk.getRenderPosition().distanceSquared(currentPosition.x, currentPosition.y, currentPosition.z)
        );

        StepVerifier.setDefaultTimeout(Duration.ofSeconds(10));

        // Use virtual time so we don't have to wait around in real time
        // to see whether there are more events pending.
        // Requires that the schedulers be created _inside_ the withVirtualTime supplier.
        verifier = StepVerifier.withVirtualTime(() -> {
            worker = new ChunkMeshWorker(
                    ChunkMeshWorkerTest::alwaysCreateMesh,
                    comparator,
                    Schedulers.parallel(),
                    Schedulers.single()
            );
            return worker.getCompletedChunks();
        });
    }

    static Chunk newDirtyChunk(Vector3ic position) {
        var chunk = new DummyChunk(position);
        chunk.markReady();
        chunk.setDirty(true);
        return chunk;
    }

    @Test
    void testMultipleChunks() {
        var chunk1 = newDirtyChunk(position0);
        var chunk2 = newDirtyChunk(new Vector3i(position0).add(1, 0, 0));

        verifier.then(() -> {
                    worker.add(chunk1);
                    worker.add(chunk2);
                    worker.update();
                })
                .expectNext(chunk1, chunk2)
                .verifyTimeout(Duration.ofSeconds(5));
    }

    @Test
    void testChunkIsNotProcessedTwice() {
        var chunk1 = newDirtyChunk(position0);

        verifier.then(() -> {
                    worker.add(chunk1);
                    worker.add(chunk1);  // added twice
                    worker.update();
                })
                .expectNextCount(1).as("expect only one result")
                .then(() -> {
                    // adding it again and doing another update should still not change
                    worker.add(chunk1);
                    worker.update();
                })
                .verifyTimeout(Duration.ofSeconds(5));
    }

    @Test
    void testChunkIsRegeneratedIfDirty() {
        var chunk1 = newDirtyChunk(position0);

        verifier.then(() -> {
                    worker.add(chunk1);
                    worker.update();
                })
                .expectNext(chunk1).as("initial generation")
                .then(() -> {
                    chunk1.setDirty(true);
                    worker.update();
                })
                .expectNext(chunk1).as("regenerating after dirty")
                .verifyTimeout(Duration.ofSeconds(5));
    }

    @Test
    @Disabled("TODO: How to do scenarios with mid-pipeline actions?")
    void testChunksDirtiedBetweenGenerationAndUploadAreUploadedAnyway() {
        // maybe redundant with testChunkIsRegeneratedIfDirty?
        // I guess the assertion is that the upload function did a thing
        // instead of skipping a thing.
    }

    @Test
    void testChunkCanBeRemovedBeforeMeshGeneration() {
        var chunk = newDirtyChunk(position0);
        verifier.then(() -> {
                    worker.add(chunk);
                    worker.remove(chunk);
                    worker.update();
                })
                // chunk was removed, no events expected
                .verifyTimeout(Duration.ofSeconds(5));
    }

    @Test
    void testDoubleRemoveIsNoProblem() {
        var chunk = newDirtyChunk(position0);
        verifier.then(() -> {
                    worker.add(chunk);
                    worker.remove(chunk);
                    worker.update();
                })
                .then(() -> {
                    worker.remove(chunk);  // second time calling remove on the same chunk
                    worker.update();
                })
                // chunk was removed, no events expected
                .verifyTimeout(Duration.ofSeconds(5));
    }

    @Test
    @Disabled("TODO: How to do scenarios with mid-pipeline actions?")
    void testChunkCanBeRemovedBeforeUpload() {
        var chunk = newDirtyChunk(position0);
        worker.add(chunk);
        // tick so generation is finished, but not upload
        worker.remove(chunk);
        // drain
        fail("assert upload did not happen for chunk");
    }

    @Test
    void testChunkCanBeRemovedByPosition() {
        var chunk = newDirtyChunk(position0);
        verifier.then(() -> {
                    worker.add(chunk);
                    worker.remove(position0);
                    worker.update();
                })
                // chunk was removed, no events expected
                .verifyTimeout(Duration.ofSeconds(5));
    }

    @Test
    void testWorkIsPrioritized() {
        var nearChunk = newDirtyChunk(position0);
        var farChunk = newDirtyChunk(new Vector3i(position0).add(100, 0, 0));

        verifier.then(() -> {
                    worker.add(farChunk);
                    worker.add(nearChunk);
                    worker.update();
                })
                // TODO: this may be flaky due to parallelization.
                //   Given a scheduler with N threads, we should test it with more than N chunks.
                .expectNext(nearChunk)
                .expectNext(farChunk)
                .verifyTimeout(Duration.ofSeconds(5));

        // TODO: change the state of the comparator
        // assert the next one through the gate is the one closest *now*
    }

    @Test
    @Disabled("TODO")
    void testWorkerStopsWhenShutDown() {
        fail("TODO: add shutdown method");
    }

    // What else? More parallelization tests?
}
