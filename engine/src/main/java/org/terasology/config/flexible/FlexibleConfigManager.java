/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.config.flexible;

import org.terasology.engine.SimpleUri;

/**
 * Stores multiple {@link FlexibleConfig} instances that can be retrieved using their id.
 * Also responsible for coordinating their serialization - to and from - disk.
 */
public interface FlexibleConfigManager {
    /**
     * Adds the given {@link FlexibleConfig} to this manager.
     *
     * @param configId A SimpleUri that effectively becomes the id of the config.
     * @param config The config that is to be added.
     * @throws RuntimeException if a FlexibleConfig with the given id already exists.
     */
    void addConfig(SimpleUri configId, FlexibleConfig config) throws RuntimeException;

    /**
     * Removes the config associated with the given id, and returns it.
     *
     * @param configId The id of the config to remove.
     * @return The config associated with the given id, or null if no config is associated with the given id.
     */
    FlexibleConfig removeConfig(SimpleUri configId);

    /**
     * Retrieves the config associated with the given id.
     *
     * @param configId The id of the config to retrieve.
     * @return The config associated with the given id, or null if no config is associated with the given id.
     */
    FlexibleConfig getConfig(SimpleUri configId);

    /**
     * Iterates over all the configs added to this manager, and loads the settings stored in them.
     */
    void loadAllConfigs();

    /**
     * Iterates over all the configs added to this manager, and saves the settings stored in them.
     */
    void saveAllConfigs();
}
