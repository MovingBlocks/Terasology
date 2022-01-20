// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.world;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.world.chunks.Chunk;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ChunkMeshWorkerTest {
    static Vector3ic position0 = new Vector3i(123, 456, 789);

    ChunkMeshWorker worker;

    static Optional<Tuple2<Chunk, ChunkMesh>> alwaysCreateMesh(Chunk chunk) {
        return Optional.of(Tuples.of(chunk, mock(ChunkMesh.class)));
    }

    @BeforeEach
    void makeWorker() {
        worker = new ChunkMeshWorker(ChunkMeshWorkerTest::alwaysCreateMesh, null);
    }

    @Test
    void testChunkIsNotProcessedTwice() {
        var chunk1 = new DummyChunk(position0);
        var chunk2 = new DummyChunk(position0);
        worker.add(chunk1);
        worker.add(chunk2);
        // drain
        fail("TODO: assert number of results == 1");
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
