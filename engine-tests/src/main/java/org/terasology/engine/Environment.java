// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine;

import com.badlogic.gdx.physics.bullet.Bullet;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.naming.Name;

import java.io.IOException;
import java.util.Set;

/**
 * Set up an empty Terasology environment.
 * <p>
 * Not for use outside {@code engine-tests}. Modules should use ModuleTestingEnvironment.
 */
class Environment {

    private static final Logger logger = LoggerFactory.getLogger(Environment.class);

    protected Context context;

    /**
     * Default setup order
     *
     * @param moduleNames a list of module names
     */
    Environment(Name... moduleNames) {
        try {
            reset(Sets.newHashSet(moduleNames));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void reset(Set<Name> moduleNames) throws IOException {
        this.context = new ContextImpl();
        RecordAndReplayCurrentStatus recordAndReplayCurrentStatus = new RecordAndReplayCurrentStatus();
        context.put(RecordAndReplayCurrentStatus.class, recordAndReplayCurrentStatus);
        CoreRegistry.setContext(context);

        setupPathManager();

        Bullet.init(true, false);

        setupConfig();

        setupModuleManager(moduleNames);

        setupDisplay();

        setupAudio();

        AssetManager assetManager = setupAssetManager();

        setupBlockManager(assetManager);

        setupExtraDataManager(context);

        setupCollisionManager();

        setupNetwork();

        setupEntitySystem();

        setupStorageManager();

        setupComponentManager();

        setupWorldProvider();

        setupCelestialSystem();

        loadPrefabs();
    }

    protected void loadPrefabs() {
        // empty
    }

    protected void setupComponentManager() {
        // empty
    }

    protected void setupPathManager() throws IOException {
        PathManager.getInstance();
    }

    protected void setupModuleManager(Set<Name> moduleNames) {
        // empty
    }

    protected void setupDisplay() {
        // empty
    }

    protected void setupConfig() {
        // empty
    }

    protected void setupAudio() {
        // empty
    }

    protected AssetManager setupAssetManager() {
        // empty
        return null;
    }

    protected AssetManager setupEmptyAssetManager() {
        // empty
        return null;
    }

    protected void setupBlockManager(AssetManager assetManager) {
        // empty
    }

    protected void setupExtraDataManager(Context context) {
        // empty
    }

    protected void setupCollisionManager() {
        // empty
    }

    protected void setupEntitySystem() {
        // empty
    }

    protected void setupNetwork() {
        // empty
    }

    protected void setupStorageManager() throws IOException {
        // empty
    }

    protected void setupWorldProvider() {
        // empty
    }

    protected void setupCelestialSystem() {
        // empty
    }

    /**
     * Cleans up all resources (similar to AutoCloseable)
     *
     * @throws RuntimeException if something goes wrong
     */
    public void close() {
        CoreRegistry.setContext(null);
        context = null;
    }

    public Context getContext() {
        return context;
    }
}

