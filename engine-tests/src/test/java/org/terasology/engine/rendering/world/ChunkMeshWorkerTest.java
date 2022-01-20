// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.terasology.engine.world.chunks.Chunk;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ChunkMeshWorkerTest {
    ChunkMeshWorker worker;

    @BeforeEach
    void makeWorker() {
        worker = new ChunkMeshWorker(null, null, null);
    }

    @Test
    void testChunkIsNotProcessedTwice() {
        var chunk1 = mock(Chunk.class);
        var chunk2 = mock(Chunk.class);
        // FIXME: give both chunks the same .position
        worker.add(chunk1);
        worker.add(chunk2);
        // drain
        // assert number of results == 1
    }

    @Test
    void testChunkIsRegeneratedIfDirty() {
        var chunk = mock(Chunk.class);
        worker.add(chunk);
        // tick - work function has started
        chunk.setDirty(true);
        worker.add(chunk);
        // drain
        // assert number of results == 2
    }

    @Test
    void testChunksDirtiedBetweenGenerationAndUploadAreUploadedAnyway() {
        // maybe redundant with testChunkIsRegeneratedIfDirty?
        // I guess the assertion is that the upload function did a thing
        // instead of skipping a thing.
    }

    @Test
    void testChunkCanBeRemovedBeforeMeshGeneration() {
        var chunk = mock(Chunk.class);
        worker.add(chunk);
        worker.remove(chunk);
        // drain
        // assert no work happened on chunk
    }

    @Test
    void testChunkCanBeRemovedBeforeUpload() {
        var chunk = mock(Chunk.class);
        worker.add(chunk);
        // tick so generation is finished, but not upload
        worker.remove(chunk);
        // drain
        // assert upload did not happen for chunk
    }

    @Test
    void testChunkCanBeRemovedByPosition() {
    }

    @Test
    void testWorkIsPrioritized() {
        var nearChunk = mock(Chunk.class);
        var farChunk = mock(Chunk.class);

        worker.add(farChunk);
        worker.add(nearChunk);
        // …add a few more so the result isn't just a coin toss.

        // tick
        // assert first one through the gate was nearChunk

        // change the state of the comparator
        // assert the next one through the gate is the one closest *now*
    }


    @Test
    void testWorkerStopsWhenShutDown() {
    }

    @Test
    void testSomethingAboutTheUpdateMethod() {
        // FIXME: What does this do, and what needs testing?
    }

    // What else? More parallelization tests?
}
