// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.world;

import org.joml.Vector3i;
import org.joml.Vector3ic;
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
import reactor.test.scheduler.VirtualTimeScheduler;
import reactor.test.subscriber.TestSubscriber;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ChunkMeshWorkerTest {
    static final Duration EXPECTED_DURATION = Duration.ofSeconds(4);

    static Vector3ic position0 = new Vector3i(123, 456, 789);

    final Vector3i currentPosition = new Vector3i(position0);

    Comparator<RenderableChunk> comparator = Comparator.comparingDouble(chunk ->
            chunk.getRenderPosition().distanceSquared(currentPosition.x, currentPosition.y, currentPosition.z)
    );
    ChunkMeshWorker worker;

    /** Creates a new mock ChunkMesh.
     * <p>
     * A simple work function for {@link ChunkMeshWorker}.
     */
    static Mono<Tuple2<Chunk, ChunkMesh>> alwaysCreateMesh(Chunk chunk) {
        chunk.setDirty(false);
        return Mono.just(Tuples.of(chunk, mock(ChunkMesh.class)));
    }

    /**
     * Create a new Chunk at this position.
     * <p>
     * The {@link DummyChunk} is marked {@code ready} and {@code dirty}.
     */
    static Chunk newDirtyChunk(Vector3ic position) {
        var chunk = new DummyChunk(position);
        chunk.markReady();
        chunk.setDirty(true);
        return chunk;
    }

    /**
     * Creates a new ChunkMeshWorker with a StepVerifier on its output.
     * <p>
     * Sets {@link #worker} to a new {@link ChunkMeshWorker}.
     *
     * @return A verifier for {@link ChunkMeshWorker#getCompletedChunks()}.
     */
    protected StepVerifier.Step<Chunk> completedChunksStepVerifier() {
        StepVerifier.setDefaultTimeout(EXPECTED_DURATION);

        // Use virtual time so we don't have to wait around in real time
        // to see whether there are more events pending.
        // Requires that the schedulers be created _inside_ the withVirtualTime supplier.
        return StepVerifier.withVirtualTime(() -> {
            worker = new ChunkMeshWorker(
                    ChunkMeshWorkerTest::alwaysCreateMesh,
                    comparator,
                    Schedulers.parallel(),
                    Schedulers.single()
            );
            return worker.getCompletedChunks();
        });
    }

    /**
     * Get completed Chunks as a list.
     * <p>
     * Applies the given function to a new {@link ChunkMeshWorker}, and returns the list of completed
     * {@link Chunk Chunks}.
     * <p>
     * Assumes the work will not be delayed by more than {@link #EXPECTED_DURATION}.
     */
    protected List<Chunk> getChunksThatResultFrom(Consumer<ChunkMeshWorker> withWorker) {
        // TODO: Make a VirtualTimeScheduler JUnit Extension so that we don't have these
        //       two different ways of creating schedulers for the ChunkMeshWorker.
        var scheduler = VirtualTimeScheduler.create();

        var workerB = new ChunkMeshWorker(
                ChunkMeshWorkerTest::alwaysCreateMesh,
                comparator,
                scheduler,
                scheduler
        );

        var completed = workerB.getCompletedChunks()
                .subscribeWith(TestSubscriber.create());

        withWorker.accept(workerB);

        // The Worker doesn't mark the flux as complete; it expects it'll still get more work.
        // That means we can't collect the the complete flux in to a list.
        // Instead, we use TestSubscriber's methods to see what it has output so far.
        //
        // Other things I have tried here:
        //  * Adding `.timeout(EXPECTED_DURATION)` to the flux, and waiting for the TimeoutError.
        //    That works, and perhaps allows for less ambiguity about what is happening, but it
        //    doesn't seem to be necessary.
        //
        //  * Using `.buffer(EXPECTED_DURATION).next()` instead of TestSubscriber.getReceived.
        //    Did not work; instead of giving me a buffer containing everything from that window,
        //    waiting on the result just timed out.
        //
        // See https://stackoverflow.com/a/72116182/9585 for some notes from the reactor-test author
        // about this test scenario.
        scheduler.advanceTimeBy(EXPECTED_DURATION);
        return completed.getReceivedOnNext();
    }

    @Test
    void testMultipleChunks() {
        var chunk1 = newDirtyChunk(position0);
        var chunk2 = newDirtyChunk(new Vector3i(position0).add(1, 0, 0));

        var resultingChunks = getChunksThatResultFrom(worker -> {
            worker.add(chunk1);
            worker.add(chunk2);
            worker.update();
        });

        assertThat(resultingChunks).containsExactly(chunk1, chunk2);
    }

    @Test
    void testChunkIsNotProcessedTwice() {
        var chunk1 = newDirtyChunk(position0);

        completedChunksStepVerifier().then(() -> {
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
                .verifyTimeout(EXPECTED_DURATION);
    }

    @Test
    void testChunkIsRegeneratedIfDirty() {
        var chunk1 = newDirtyChunk(position0);

        completedChunksStepVerifier().then(() -> {
                    worker.add(chunk1);
                    worker.update();
                })
                .expectNext(chunk1).as("initial generation")
                .then(() -> {
                    chunk1.setDirty(true);
                    worker.update();
                })
                .expectNext(chunk1).as("regenerating after dirty")
                .verifyTimeout(EXPECTED_DURATION);
    }

    @Test
    void testChunkCanBeRemovedBeforeMeshGeneration() {
        var chunk = newDirtyChunk(position0);
        completedChunksStepVerifier().then(() -> {
                    worker.add(chunk);
                    worker.remove(chunk);
                    worker.update();
                })
                // chunk was removed, no events expected
                .verifyTimeout(EXPECTED_DURATION);
    }

    @Test
    void testDoubleRemoveIsNoProblem() {
        var chunk = newDirtyChunk(position0);
        completedChunksStepVerifier().then(() -> {
                    worker.add(chunk);
                    worker.remove(chunk);
                    worker.update();
                })
                .then(() -> {
                    worker.remove(chunk);  // second time calling remove on the same chunk
                    worker.update();
                })
                // chunk was removed, no events expected
                .verifyTimeout(EXPECTED_DURATION);
    }

    @Test
    void testChunkCanBeRemovedByPosition() {
        var chunk = newDirtyChunk(position0);
        completedChunksStepVerifier().then(() -> {
                    worker.add(chunk);
                    worker.remove(position0);
                    worker.update();
                })
                // chunk was removed, no events expected
                .verifyTimeout(EXPECTED_DURATION);
    }

    @Test
    void testWorkIsPrioritized() {
        var nearChunk = newDirtyChunk(position0);
        var farChunk = newDirtyChunk(new Vector3i(position0).add(100, 0, 0));

        var completed = getChunksThatResultFrom(worker -> {
            worker.add(farChunk);
            worker.add(nearChunk);
            worker.update();
        });
        // TODO: this may be flaky due to parallelization.
        //   Given a scheduler with N threads, we should test it with more than N chunks.
        assertThat(completed).containsExactly(nearChunk, farChunk).inOrder();

        // TODO: change the state of the comparator
        // assert the next one through the gate is the one closest *now*
    }

    @Test
    @Disabled("TODO")
    void testWorkerStopsWhenShutDown() {
        fail("TODO: add shutdown method");
    }
}
