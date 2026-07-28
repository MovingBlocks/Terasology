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
     * Runs the engine until {@code condition} holds, failing the test if it does not.
     * <p>
     * Prefer this over {@link #runUntil(Supplier)} in tests: a timeout throws with the description
     * instead of returning a status that is easy to drop.
     * <p>
     * Two timeouts can end the wait. The game-time one below throws {@link AssertionError}. The
     * real-time safety timeout is enforced further down in the main loop and throws
     * {@link com.google.common.util.concurrent.UncheckedTimeoutException} instead - and for a
     * condition that never becomes true because the engine has stopped progressing, that is the
     * likelier of the two, since game time only advances while the engine ticks.
     *
     * @param description what is being waited for, phrased to read after "timed out waiting for"
     * @throws AssertionError if the condition does not hold within DEFAULT_GAME_TIME_TIMEOUT of game time
     * @throws com.google.common.util.concurrent.UncheckedTimeoutException if the real-time safety
     *         timeout is exceeded first; see {@link #setSafetyTimeoutMs(long)}
     */
    void awaitUntil(String description, Supplier<Boolean> condition);

    /**
     * Runs the engine until {@code condition} holds, failing the test if it does not.
     *
     * @param gameTimeTimeoutMs how long to wait, in game time
     * @param description what is being waited for, phrased to read after "timed out waiting for"
     * @throws AssertionError if the condition does not hold within {@code gameTimeTimeoutMs} of game time
     * @throws com.google.common.util.concurrent.UncheckedTimeoutException if the real-time safety
     *         timeout is exceeded first; see {@link #setSafetyTimeoutMs(long)}
     * @see #awaitUntil(String, Supplier)
     */
    void awaitUntil(long gameTimeTimeoutMs, String description, Supplier<Boolean> condition);

    /**
     * Runs tick() on the engine until f evaluates to true or DEFAULT_GAME_TIME_TIMEOUT milliseconds have passed in game time
     *
     * @return true if execution timed out
     * @see #awaitUntil(String, Supplier) for a variant that fails the test on timeout
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
     * Runs the engines until at least {@code expectedClients} clients are registered with the host.
     * <p>
     * {@link #createClient()} returns once that client itself is in-game, which is not the same as the
     * host having finished registering it - so a test that creates clients and immediately inspects
     * {@code NetworkSystem.getPlayers()} can see fewer than it created. Waiting for the host's own view
     * is the fix, and doing it by hand is easy to get subtly wrong.
     *
     * @param expectedClients how many clients the host should know about; zero returns immediately
     * @throws IllegalArgumentException if {@code expectedClients} is negative, which no wait could satisfy
     *         meaningfully and which would otherwise pass silently
     * @throws AssertionError if that many never register within the default game-time timeout
     * @throws com.google.common.util.concurrent.UncheckedTimeoutException if the real-time safety
     *         timeout is exceeded first; see {@link #awaitUntil(String, Supplier)}
     */
    void awaitClients(int expectedClients);

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
