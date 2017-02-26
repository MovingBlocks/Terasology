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
}
