// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import com.google.common.util.concurrent.ListenableFuture;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.core.modes.StateIngame;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

/**
 * Methods for interacting with the engine in the test environment.
 * <p>
 * Most tests only need the methods of {@link MainLoop}. Expect this class to be deprecated after we figure out better
 * asynchronous methods for {@link #createClient()}.
 *
 * <h2>Client Engine Instances</h2>
 * Client instances can be easily created via {@link #createClient()} which returns the in-game context of the created
 * engine instance. When this method returns, the client will be in the {@link StateIngame} state and connected to the
 * host. Currently all engine instances are headless, though it is possible to use headed engines in the future.
 */
public class ModuleTestingHelper implements ModuleTestingEnvironment {

    final Engines engines;
    final MainLoop mainLoop;

    ModuleTestingHelper(Engines engines) {
        this.engines = engines;
        this.mainLoop = new MainLoop(engines);
    }

    @Override
    public void forceAndWaitForGeneration(Vector3ic blockPos) {
        mainLoop.forceAndWaitForGeneration(blockPos);
    }

    @Override
    public ListenableFuture<ChunkRegionFuture> makeBlocksRelevant(BlockRegionc blocks) {
        return mainLoop.makeBlocksRelevant(blocks);
    }

    @Override
    public ListenableFuture<ChunkRegionFuture> makeChunksRelevant(BlockRegion chunks) {
        return mainLoop.makeChunksRelevant(chunks);
    }

    @Override
    public ListenableFuture<ChunkRegionFuture> makeChunksRelevant(BlockRegion chunks, Vector3fc centerBlock) {
        return mainLoop.makeChunksRelevant(chunks, centerBlock);
    }

    @Override
    public <T> T runUntil(ListenableFuture<T> future) {
        return mainLoop.runUntil(future);
    }

    @Override
    public boolean runUntil(Supplier<Boolean> f) {
        return mainLoop.runUntil(f);
    }

    @Override
    public boolean runUntil(long gameTimeTimeoutMs, Supplier<Boolean> f) {
        return mainLoop.runUntil(gameTimeTimeoutMs, f);
    }

    @Override
    public boolean runWhile(Supplier<Boolean> f) {
        return mainLoop.runWhile(f);
    }

    @Override
    public boolean runWhile(long gameTimeTimeoutMs, Supplier<Boolean> f) {
        return mainLoop.runWhile(gameTimeTimeoutMs, f);
    }

    @Override
    public Context createClient() throws IOException {
        return engines.createClient(mainLoop);
    }

    @Override
    public List<TerasologyEngine> getEngines() {
        return engines.getEngines();
    }

    @Override
    public Context getHostContext() {
        return engines.getHostContext();
    }

    @Override
    public long getSafetyTimeoutMs() {
        return mainLoop.getSafetyTimeoutMs();
    }

    @Override
    public void setSafetyTimeoutMs(long safetyTimeoutMs) {
        mainLoop.setSafetyTimeoutMs(safetyTimeoutMs);
    }
}
