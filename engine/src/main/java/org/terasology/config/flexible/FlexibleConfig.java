/*
 * Copyright 2016 MovingBlocks
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

import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * Stores multiple {@link Setting} instances that can be retrieved using their id.
 */
public interface FlexibleConfig {
    /**
     * Adds a {@link Setting} to the config. In case of failure warnings will be issued through the logger
     * detailing the exact nature of the failure.
     * @param setting The {@link Setting} to add.
     * @return True if the {@link Setting} was added, false otherwise.
     */
    boolean add(Setting setting);

    /**
     * Returns the {@link Setting<V>} with the given id. Null is returned if a setting with the given id does not
     * exist in the config.
     * @param id The id of the {@link Setting} to retrieve.
     * @param <V> The type of the value the retrieved {@link Setting} must contain.
     * @return The {@link Setting<V>}, if found in the config. Null if a {@link Setting} with the given id
     * does not exist in the config.
     * @throws ClassCastException when {@link V} does not match the type of the values stored inside the retrieved
     * {@link Setting}.
     */
    <V> Setting<V> get(SimpleUri id);

    /**
     * Removes the {@link Setting} with the given id if it exists in the config and if the {@link Setting} does
     * not have any subscribers. In case of failure warnings will be issued through the logger detailing the
     * exact nature of the failure.
     * @param id The id of the {@link Setting} to remove.
     * @return True if the {@link Setting} was removed, false otherwise.
     */
    boolean remove(SimpleUri id);

    /**
     * Returns a boolean stating whether the config contains a {@link Setting} with the given id.
     */
    boolean contains(SimpleUri id);

    /**
     * Returns a map of all the settings, allowing iteration of all the settings.
     *
     * @return A map containing all the settings, along with their id.
     */
    Map<SimpleUri, Setting> getSettings();

    /**
     * Returns a potentially verbose, human-readable description regarding the purpose of this {@link FlexibleConfig}.
     * This description is also written to file when the {@link FlexibleConfig} is saved.
     */
    String getDescription();

    /**
     * Saves the values of all settings having non-default values, to enable persistence across sessions.
     * Also saved for documentation purposes is the description of the {@link FlexibleConfig}, as
     * determined by {@link #getDescription()}.
     *
     * All the non-default values that were not used in this session and are still "parked" are also
     * saved as-is, to be used later.
     *
     * @param writer A writer that will serve as the destination of settings.
     */
    void save(Writer writer);

    /**
     * Loads the values of the settings having non-default values, to enable persistence across sessions.
     *
     * All the non-default values are loaded and "parked", initially remaining inaccessible. 
     * Once a Setting object is added to the config, a corresponding non-default value is sought 
     * among the parked values. If one is found it is parsed and stored in the Setting object.
     *
     * Note that this function should be called -before- adding any settings to the FlexibleConfig. 
     * Otherwise any corresponding parked value will never be loaded.
     *
     * @param reader A reader that will serve as the source of the settings.
     */
    void load(Reader reader);
}
