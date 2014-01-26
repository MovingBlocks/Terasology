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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.benchmark.Profiler;

/**
 * Profiles a given Terasology environment setup
 * @author Martin Steiger
 */
public class ProfiledEnvironment extends Environment {

    private static final Logger logger = LoggerFactory.getLogger(ProfiledEnvironment.class);
    private final Environment delegate;

    /**
     * @param delegate the environment to delegate calls to
     */
    public ProfiledEnvironment(Environment delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void reset() throws Exception {
        Object total = Profiler.start();
        
        delegate.reset();

        logger.info("Total time: " + Profiler.getAsStringAndStop(total));
    }

    @Override
    protected void loadPrefabs() {
        Object id = Profiler.start();
        delegate.loadPrefabs();
        logger.info("loadPrefabs: " + Profiler.getAsStringAndStop(id));
    }

    @Override
    protected void setupComponentManager() {
        Object id = Profiler.start();
        delegate.setupComponentManager();
        logger.info("ComponentManager: " + Profiler.getAsStringAndStop(id));
    }

    @Override
    protected void setupPathManager() throws IOException {
        Object id = Profiler.start();
        delegate.setupPathManager();
        logger.info("PathManager: " + Profiler.getAsStringAndStop(id));
    }

    @Override
    protected void setupModuleManager() {
        Object id = Profiler.start();
        delegate.setupModuleManager();
        logger.info("ModuleManager: " + Profiler.getAsStringAndStop(id));
    }

    @Override
    protected void setupDisplay() {
        Object id = Profiler.start();
        delegate.setupDisplay();
        logger.info("Display: " + Profiler.getAsStringAndStop(id));
    }

    @Override
    protected void setupConfig() {
        Object id = Profiler.start();
        delegate.setupConfig();
        logger.info("Config: " + Profiler.getAsStringAndStop(id));
    }

    @Override
    protected void setupAudio() {
        Object id = Profiler.start();
        delegate.setupAudio();
        logger.info("Audio: " + Profiler.getAsStringAndStop(id));
    }

    @Override
    protected void setupAssetManager() {
        Object id = Profiler.start();
        delegate.setupAssetManager();
        logger.info("Asset: " + Profiler.getAsStringAndStop(id));
    }

    @Override
    protected void setupBlockManager() {
        Object id = Profiler.start();
        delegate.setupBlockManager();
        logger.info("BlockManager: " + Profiler.getAsStringAndStop(id));
    }

    @Override
    protected void setupCollisionManager() {
        Object id = Profiler.start();
        delegate.setupCollisionManager();
        logger.info("CollisionManager: " + Profiler.getAsStringAndStop(id));
    }

    @Override
    protected void setupEntitySystem() {
        Object id = Profiler.start();
        delegate.setupEntitySystem();
        logger.info("EntitySystem: " + Profiler.getAsStringAndStop(id));
    }

    @Override
    protected void setupNetwork() {
        Object id = Profiler.start();
        delegate.setupNetwork();
        logger.info("NetworkManager: " + Profiler.getAsStringAndStop(id));
    }

    @Override
    protected void setupStorageManager() {
        Object id = Profiler.start();
        delegate.setupStorageManager();
        logger.info("StorageManager: " + Profiler.getAsStringAndStop(id));
    }

    /**
     * Cleans up all resources (similar to Autocloseable)
     * @throws Exception if something goes wrong
     */
    @Override
    public void close() throws Exception {
        delegate.close();
    }

}

