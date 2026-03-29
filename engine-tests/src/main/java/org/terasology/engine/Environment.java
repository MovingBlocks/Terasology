// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine;

import com.badlogic.gdx.physics.bullet.Bullet;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Lifetime;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.di.ServiceRegistry;
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
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        RecordAndReplayCurrentStatus recordAndReplayCurrentStatus = new RecordAndReplayCurrentStatus();
        serviceRegistry.with(RecordAndReplayCurrentStatus.class).lifetime(Lifetime.Singleton).use(() -> recordAndReplayCurrentStatus);
        CoreRegistry.setContext(context);

        setupPathManager();

        Bullet.init(true, false);

        setupConfig(serviceRegistry);

        ModuleManager moduleManager = setupModuleManager(serviceRegistry, moduleNames);

        setupDisplay(serviceRegistry);

        setupAudio(serviceRegistry);

        AssetManager assetManager = setupAssetManager(moduleManager, serviceRegistry);

        setupBlockManager(serviceRegistry, assetManager);

        setupExtraDataManager(serviceRegistry, context);

        setupCollisionManager(serviceRegistry);

        setupNetwork(serviceRegistry);

        setupEntitySystem(serviceRegistry);

        setupStorageManager(serviceRegistry);

        setupComponentManager(serviceRegistry);

        setupWorldProvider(serviceRegistry);

        setupCelestialSystem(serviceRegistry);

        this.context = new ContextImpl(serviceRegistry);

        registerBlockTypeHandlers(this.context);
        registerCollisionTypeHandlers(this.context);
        initComponentManager(this.context);

        loadPrefabs();
    }

    protected void loadPrefabs() {
        // empty
    }

    protected void setupComponentManager(ServiceRegistry serviceRegistry) {
        // empty
    }

    protected void initComponentManager(Context context) {
        // empty
    }

    protected void setupPathManager() throws IOException {
        PathManager.getInstance();
    }

    protected ModuleManager setupModuleManager(ServiceRegistry serviceRegistry, Set<Name> moduleNames) {
        // empty
        return null;
    }

    protected void setupDisplay(ServiceRegistry serviceRegistry) {
        // empty
    }

    protected void setupConfig(ServiceRegistry serviceRegistry) {
        // empty
    }

    protected void setupAudio(ServiceRegistry serviceRegistry) {
        // empty
    }

    protected AssetManager setupAssetManager(ModuleManager moduleManager, ServiceRegistry serviceRegistry) {
        // empty
        return null;
    }

    protected AssetManager setupEmptyAssetManager(ModuleManager moduleManager, ServiceRegistry serviceRegistry) {
        // empty
        return null;
    }

    protected void setupBlockManager(ServiceRegistry serviceRegistry, AssetManager assetManager) {
        // empty
    }

    protected void registerBlockTypeHandlers(Context context) {
        // empty
    }

    protected void setupExtraDataManager(ServiceRegistry serviceRegistry, Context context) {
        // empty
    }

    protected void setupCollisionManager(ServiceRegistry serviceRegistry) {
        // empty
    }

    protected void registerCollisionTypeHandlers(Context context) {
        // empty
    }

    protected void setupEntitySystem(ServiceRegistry serviceRegistry) {
        // empty
    }

    protected void setupNetwork(ServiceRegistry serviceRegistry) {
        // empty
    }

    protected void setupStorageManager(ServiceRegistry serviceRegistry) throws IOException {
        // empty
    }

    protected void setupWorldProvider(ServiceRegistry serviceRegistry) {
        // empty
    }

    protected void setupCelestialSystem(ServiceRegistry serviceRegistry) {
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

