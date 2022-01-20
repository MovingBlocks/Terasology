// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.world;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.terasology.engine.core.GameScheduler;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.RenderableChunk;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ChunkMeshWorkerTest {
    static Vector3ic position0 = new Vector3i(123, 456, 789);

    final Vector3i currentPosition = new Vector3i(position0);

    ChunkMeshWorker worker;
    Comparator<RenderableChunk> comparator;

    static Mono<Tuple2<Chunk, ChunkMesh>> alwaysCreateMesh(Chunk chunk) {
        return Mono.just(Tuples.of(chunk, mock(ChunkMesh.class)));
    }

    @BeforeEach
    void makeWorker() {
        comparator = Comparator.comparingDouble(chunk ->
                chunk.getRenderPosition().distanceSquared(currentPosition.x, currentPosition.y, currentPosition.z)
        );

        worker = new ChunkMeshWorker(
                ChunkMeshWorkerTest::alwaysCreateMesh,
                comparator,
                GameScheduler.parallel(),
                GameScheduler.gameMain()
        );
    }

    @Test
    void testChunkIsNotProcessedTwice() {
        var chunk1 = new DummyChunk(position0);
        var chunk2 = new DummyChunk(position0);

        Sinks.Many<Chunk> sink = worker.getChunkMeshPublisher();

        StepVerifier verifier = StepVerifier
                .create(worker.getChunksAndNewMeshes())
                .expectNextCount(1) // Expect only _one_ result.
                .expectComplete()
                .verifyLater();

        sink.tryEmitNext(chunk1);
        sink.tryEmitNext(chunk2);
        sink.tryEmitComplete();
        verifier.verify();
    }

    @Test
    void testChunkIsRegeneratedIfDirty() {
        var chunk = new DummyChunk(position0);
        worker.add(chunk);
        // tick - work function has started
        chunk.setDirty(true);
        worker.add(chunk);
        // drain
        fail("TODO: assert number of results == 2");
    }

    @Test
    void testChunksDirtiedBetweenGenerationAndUploadAreUploadedAnyway() {
        // maybe redundant with testChunkIsRegeneratedIfDirty?
        // I guess the assertion is that the upload function did a thing
        // instead of skipping a thing.
    }

    @Test
    void testChunkCanBeRemovedBeforeMeshGeneration() {
        var chunk = new DummyChunk(position0);
        worker.add(chunk);
        worker.remove(chunk);
        // drain
        fail("TODO: assert no work happened on chunk");
    }

    @Test
    void testDoubleRemoveIsNoProblem() {
        var chunk = new DummyChunk(position0);
        worker.add(chunk);
        worker.remove(chunk);
        worker.remove(chunk);
        // drain
    }

    @Test
    void testChunkCanBeRemovedBeforeUpload() {
        var chunk = new DummyChunk(position0);
        worker.add(chunk);
        // tick so generation is finished, but not upload
        worker.remove(chunk);
        // drain
        fail("assert upload did not happen for chunk");
    }

    @Test
    void testChunkCanBeRemovedByPosition() {
        var chunk = new DummyChunk(position0);
        worker.add(chunk);
        worker.remove(position0);
        // drain
        fail("TODO: assert no work happened on chunk");
    }

    @Test
    void testWorkIsPrioritized() {
        var nearChunk = new DummyChunk(position0);
        var farChunk = new DummyChunk(new Vector3i(position0).add(100, 0, 0));

        worker.add(farChunk);
        worker.add(nearChunk);
        // …add a few more so the result isn't just a coin toss.

        // tick
        fail("TODO: assert first one through the gate was nearChunk");

        // change the state of the comparator
        // assert the next one through the gate is the one closest *now*
    }

    @Test
    void testWorkerStopsWhenShutDown() {
        fail("TODO: add shutdown method");
    }

    @Test
    void testSomethingAboutTheUpdateMethod() {
        fail("FIXME: What does this do, and what needs testing?");
    }

    // What else? More parallelization tests?
}
