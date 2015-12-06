/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License"){ }
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.naming.Name;
import org.terasology.registry.CoreRegistry;

import java.io.IOException;
import java.util.Set;

/**
 * Setup an empty Terasology environment
 *
 */
public class Environment {

    private static final Logger logger = LoggerFactory.getLogger(Environment.class);

    protected Context context;

    /**
     * Default setup order
     *
     * @param moduleNames a list of module names
     */
    public Environment(Name... moduleNames) {
        try {
            reset(Sets.newHashSet(moduleNames));
        } catch (Exception e) {
            logger.error("Error", e);
            throw new RuntimeException(e);
        }
    }

    protected void reset(Set<Name> moduleNames) throws Exception {
        this.context = new ContextImpl();
        CoreRegistry.setContext(context);

        setupPathManager();

        setupConfig();

        setupModuleManager(moduleNames);

        setupDisplay();

        setupAudio();

        AssetManager assetManager = setupAssetManager();

        setupBlockManager(assetManager);

        setupCollisionManager();

        setupNetwork();

        setupEntitySystem();

        setupStorageManager();

        setupComponentManager();

        loadPrefabs();
    }

    protected void loadPrefabs() {
        // empty
    }

    protected void setupComponentManager() {
        // empty
    }

    protected void setupPathManager() throws IOException {
        // empty
    }

    protected void setupModuleManager(Set<Name> moduleNames) throws Exception {
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

    /**
     * Cleans up all resources (similar to AutoCloseable)
     *
     * @throws Exception if something goes wrong
     */
    public void close() throws Exception {
        CoreRegistry.setContext(null);
        context = null;
    }

    public Context getContext() {
        return context;
    }
}

