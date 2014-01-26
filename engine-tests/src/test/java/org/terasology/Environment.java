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

/**
 * Setup an empty Terasology environment
 * @author Martin Steiger
 */
public class Environment {

    private static final Logger logger = LoggerFactory.getLogger(Environment.class);

    /**
     * Default setup order
     */
    public Environment() {
        try {
            reset();
        } catch (Exception e) {
            logger.error("Error", e);
            throw new RuntimeException(e);
        }
    }
    
    protected void reset() throws Exception {

        setupPathManager();

        setupConfig();

        setupModuleManager();

        setupDisplay();

        setupAudio();

        setupAssetManager();

        setupBlockManager();

        setupCollisionManager();

        setupNetwork();

        setupEntitySystem();

        setupStorageManager();

        setupComponentManager();

        loadPrefabs();
    }

    protected void activateAllModules() {
        // empty
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

    protected void setupModuleManager() {
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

    protected void setupAssetManager() {
        // empty
    }

    protected void setupEmptyAssetManager() {
        // empty
    }

    protected void setupBlockManager() {
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

    protected void setupStorageManager() {
        // empty
    }

    /**
     * Cleans up all resources (similar to AutoCloseable)
     * @throws Exception if something goes wrong
     */
    public void close() throws Exception {
        // nothing to do
    }

}

