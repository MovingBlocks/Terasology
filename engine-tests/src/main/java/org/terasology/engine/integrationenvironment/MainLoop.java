// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import org.joml.Matrix4f;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.localChunkProvider.RelevanceSystem;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Methods to run the main loop of the game.
 * <p>
 * Engines can be run while a condition is true via {@link #runWhile(Supplier)} <br>{@code mainLoop.runWhile(()-> true);}
 * <p>
 * or conversely run until a condition is true via {@link #runUntil(Supplier)} <br>{@code mainLoop.runUntil(()-> false);}
 * <p>
 * Test scenarios which take place in a particular location of the world must first make sure that location is loaded
 * with {@link #makeBlocksRelevant makeBlocksRelevant} or {@link #makeChunksRelevant makeChunksRelevant}.
 * <pre><code>
 *     // Load everything within 40 blocks of x=123, z=456
 *     mainLoop.runUntil(makeBlocksRelevant(
 *         new BlockRegion(123, SURFACE_HEIGHT, 456).expand(40, 40, 40)));
 * </code></pre>
 * <p>
 * {@link MTEExtension} provides tests with a game engine, configured with a module environment
 * and a world. The engine is ready by the time a test method is executed, but does not <em>run</em>
 * until you use one of these methods.
 * <p>
 * If there are multiple engines (a host and one or more clients), they will tick in a round-robin fashion.
 * <p>
 * This class is available via dependency injection with the {@link org.terasology.engine.registry.In} annotation
 * or as a parameter to a JUnit {@link org.junit.jupiter.api.Test} method; see {@link MTEExtension}.
 */
public class MainLoop {
    // TODO: Can we get rid of this by making sure our main loop is compatible with JUnit's timeout spec?
    long safetyTimeoutMs = ModuleTestingEnvironment.DEFAULT_SAFETY_TIMEOUT;

    private final Engines engines;

    public MainLoop(Engines engines) {
        this.engines = engines;
    }

    /**
     * Creates a dummy entity with RelevanceRegion component to force a chunk's generation and availability. Blocks while waiting for the
     * chunk to become loaded
     *
     * @param blockPos the block position of the dummy entity. Only the chunk containing this position will be available
     */
    public void forceAndWaitForGeneration(Vector3ic blockPos) {
        WorldProvider worldProvider = engines.getHostContext().get(WorldProvider.class);
        if (worldProvider.isBlockRelevant(blockPos)) {
            return;
        }

        ListenableFuture<ChunkRegionFuture> chunkRegion = makeBlocksRelevant(new BlockRegion(blockPos));
        runUntil(chunkRegion);
    }

    /**
     * Makes sure the area containing these blocks is loaded.
     * <p>
     * This method is asynchronous. Pass the result to {@link #runUntil(ListenableFuture)} if you need to wait until the area is ready.
     *
     * @see #makeChunksRelevant(BlockRegion) makeChunksRelevant if you have chunk coordinates instead of block coordinates.
     *
     * @param blocks blocks to mark as relevant
     * @return relevant chunks
     */
    public ListenableFuture<ChunkRegionFuture> makeBlocksRelevant(BlockRegionc blocks) {
        BlockRegion desiredChunkRegion = Chunks.toChunkRegion(new BlockRegion(blocks));
        return makeChunksRelevant(desiredChunkRegion, blocks.center(new Vector3f()));
    }

    /**
     * Makes sure the area containing these chunks is loaded.
     * <p>
     * This method is asynchronous. Pass the result to {@link #runUntil(ListenableFuture)} if you need to wait until the area is ready.
     *
     * @see #makeBlocksRelevant(BlockRegionc) makeBlocksRelevant if you have block coordinates instead of chunk coordinates.
     *
     * @param chunks to mark as relevant
     * @return relevant chunks
     */
    @SuppressWarnings("unused")
    public ListenableFuture<ChunkRegionFuture> makeChunksRelevant(BlockRegion chunks) {
        // Pick a central point (in block coordinates).
        Vector3f centerPoint = chunkRegionToNewBlockRegion(chunks).center(new Vector3f());

        return makeChunksRelevant(chunks, centerPoint);
    }

    public ListenableFuture<ChunkRegionFuture> makeChunksRelevant(BlockRegion chunks, Vector3fc centerBlock) {
        Preconditions.checkArgument(chunks.contains(Chunks.toChunkPos(new Vector3i(centerBlock, RoundingMode.FLOOR))),
                "centerBlock should %s be within the region %s",
                centerBlock, chunkRegionToNewBlockRegion(chunks));
        Vector3i desiredSize = chunks.getSize(new Vector3i());

        EntityManager entityManager = Verify.verifyNotNull(engines.getHostContext().get(EntityManager.class));
        RelevanceSystem relevanceSystem = Verify.verifyNotNull(engines.getHostContext().get(RelevanceSystem.class));
        ChunkRegionFuture listener = ChunkRegionFuture.create(entityManager, relevanceSystem, centerBlock, desiredSize);
        return listener.getFuture();
    }

    BlockRegionc chunkRegionToNewBlockRegion(BlockRegionc chunks) {
        BlockRegion blocks = new BlockRegion(chunks);
        return blocks.transform(new Matrix4f().scaling(new Vector3f(Chunks.CHUNK_SIZE)));
    }

    /**
     * Runs until this future is complete.
     *
     * @return the result of the future
     */
    public <T> T runUntil(ListenableFuture<T> future) {
        boolean timedOut = runUntil(future::isDone);
        if (timedOut) {
            // TODO: if runUntil returns timedOut but does not throw an exception, it
            //     means it hit DEFAULT_GAME_TIME_TIMEOUT but not SAFETY_TIMEOUT, and
            //     that's a weird interface due for a revision.
            future.cancel(true);  // let it know we no longer expect results
            throw new UncheckedTimeoutException("No result within default timeout.");
        }
        try {
            return future.get(0, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw new UncheckedExecutionException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while waiting for " + future, e);
        } catch (TimeoutException e) {
            throw new UncheckedTimeoutException(
                    "Checked isDone before calling get, so this shouldn't happen.", e);
        }
    }

    /**
     * Runs tick() on the engine until f evaluates to true or DEFAULT_GAME_TIME_TIMEOUT milliseconds have passed in game time
     *
     * @return true if execution timed out
     */
    public boolean runUntil(Supplier<Boolean> f) {
        return runWhile(() -> !f.get());
    }

    /**
     * Runs tick() on the engine until f evaluates to true or gameTimeTimeoutMs has passed in game time
     *
     * @return true if execution timed out
     */
    public boolean runUntil(long gameTimeTimeoutMs, Supplier<Boolean> f) {
        return runWhile(gameTimeTimeoutMs, () -> !f.get());
    }

    /**
     * Runs tick() on the engine while f evaluates to true or until DEFAULT_GAME_TIME_TIMEOUT milliseconds have passed
     *
     * @return true if execution timed out
     */
    public boolean runWhile(Supplier<Boolean> f) {
        return runWhile(ModuleTestingEnvironment.DEFAULT_GAME_TIME_TIMEOUT, f);
    }

    /**
     * Runs tick() on the engine while f evaluates to true or until gameTimeTimeoutMs has passed in game time.
     *
     * @return true if execution timed out
     */
    public boolean runWhile(long gameTimeTimeoutMs, Supplier<Boolean> f) {
        boolean timedOut = false;
        Time hostTime = engines.getHostContext().get(Time.class);
        long startRealTime = System.currentTimeMillis();
        long startGameTime = hostTime.getGameTimeInMs();

        while (f.get() && !timedOut) {
            Thread.yield();
            if (Thread.currentThread().isInterrupted()) {
                throw new RuntimeException(String.format("Thread %s interrupted while waiting for %s.",
                        Thread.currentThread(), f));
            }
            for (TerasologyEngine terasologyEngine : engines.getEngines()) {
                boolean keepRunning = terasologyEngine.tick();
                if (!keepRunning && terasologyEngine == engines.host) {
                    throw new RuntimeException("Host has shut down: " + engines.host.getStatus());
                }
            }

            // handle safety timeout
            if (System.currentTimeMillis() - startRealTime > safetyTimeoutMs) {
                timedOut = true;
                // If we've passed the _safety_ timeout, throw an exception.
                throw new UncheckedTimeoutException("MTE Safety timeout exceeded. See setSafetyTimeoutMs()");
            }

            // handle game time timeout
            if (hostTime.getGameTimeInMs() - startGameTime > gameTimeTimeoutMs) {
                // If we've passed the user-specified timeout but are still under the
                // safety threshold, set timed-out status without throwing.
                timedOut = true;
            }
        }

        return timedOut;
    }

    /**
     * @return the current safety timeout
     */
    public long getSafetyTimeoutMs() {
        return safetyTimeoutMs;
    }

    /**
     * Sets the safety timeout (default 30s).
     *
     * @param safetyTimeoutMs The safety timeout applies to {@link #runWhile runWhile} and related helpers, and stops execution when
     *         the specified number of real time milliseconds has passed. Note that this is different from the timeout parameter of those
     *         methods, which is specified in game time.
     *         <p>
     *         When a single {@code run*} helper invocation exceeds the safety timeout, MTE asserts false to explicitly fail the test.
     *         <p>
     *         The safety timeout exists to prevent indefinite execution in Jenkins or long IDE test runs, and should be adjusted as needed
     *         so that tests pass reliably in all environments.
     */
    public void setSafetyTimeoutMs(long safetyTimeoutMs) {
        this.safetyTimeoutMs = safetyTimeoutMs;
    }
}
