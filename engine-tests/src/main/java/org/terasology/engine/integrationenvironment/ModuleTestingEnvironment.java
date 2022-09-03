// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import com.google.common.util.concurrent.ListenableFuture;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

/**
 * The public methods that were available via ModuleTestingHelper v0.3.2.
 */
public interface ModuleTestingEnvironment {
    long DEFAULT_SAFETY_TIMEOUT = 60000;
    long DEFAULT_GAME_TIME_TIMEOUT = 30000;
    String DEFAULT_WORLD_GENERATOR = "unittest:dummy";

    /**
     * Creates a dummy entity with RelevanceRegion component to force a chunk's generation and availability. Blocks while waiting for the
     * chunk to become loaded
     *
     * @param blockPos the block position of the dummy entity. Only the chunk containing this position will be available
     */
    void forceAndWaitForGeneration(Vector3ic blockPos);

    /**
     * @param blocks blocks to mark as relevant
     * @return relevant chunks
     */
    ListenableFuture<ChunkRegionFuture> makeBlocksRelevant(BlockRegionc blocks);

    ListenableFuture<ChunkRegionFuture> makeChunksRelevant(BlockRegion chunks);

    ListenableFuture<ChunkRegionFuture> makeChunksRelevant(BlockRegion chunks, Vector3fc centerBlock);

    <T> T runUntil(ListenableFuture<T> future);

    /**
     * Runs tick() on the engine until f evaluates to true or DEFAULT_GAME_TIME_TIMEOUT milliseconds have passed in game time
     *
     * @return true if execution timed out
     */
    boolean runUntil(Supplier<Boolean> f);

    /**
     * Runs tick() on the engine until f evaluates to true or gameTimeTimeoutMs has passed in game time
     *
     * @return true if execution timed out
     */
    boolean runUntil(long gameTimeTimeoutMs, Supplier<Boolean> f);

    /**
     * Runs tick() on the engine while f evaluates to true or until DEFAULT_GAME_TIME_TIMEOUT milliseconds have passed
     *
     * @return true if execution timed out
     */
    boolean runWhile(Supplier<Boolean> f);

    /**
     * Runs tick() on the engine while f evaluates to true or until gameTimeTimeoutMs has passed in game time.
     *
     * @return true if execution timed out
     */
    boolean runWhile(long gameTimeTimeoutMs, Supplier<Boolean> f);

    /**
     * Creates a new client and connects it to the host
     *
     * @return the created client's context object
     */
    Context createClient() throws IOException;

    /**
     * The engines active in this instance of the module testing environment.
     * <p>
     * Engines are created for the host and connecting clients.
     *
     * @return list of active engines
     */
    List<TerasologyEngine> getEngines();

    /**
     * Get the host context for this module testing environment.
     * <p>
     * The host context will be null if the testing environment has not been set up via {@link Engines#setup()}
     * beforehand.
     *
     * @return the engine's host context, or null if not set up yet
     */
    Context getHostContext();

    /**
     * @return the current safety timeout
     */
    long getSafetyTimeoutMs();

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
    void setSafetyTimeoutMs(long safetyTimeoutMs);
}
