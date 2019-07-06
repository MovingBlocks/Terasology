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

import org.terasology.config.flexible.constraints.SettingConstraint;
import org.terasology.engine.SimpleUri;
import org.terasology.module.sandbox.API;

import java.io.Reader;
import java.io.Writer;
import java.util.Collection;

/**
 * Stores multiple {@link Setting} instances that can be retrieved using their id.
 */
@API
public interface FlexibleConfig {
    /**
     * Registers the addition of a new {@link Setting} to this {@link FlexibleConfig}
     * through a {@link SettingEntry}, which allows you to construct the new
     * {@link Setting} and add it to this {@link FlexibleConfig} once it is constructed.
     *
     * @param id        The id of the {@link Setting} that will be added.
     * @param valueType The type of values that the {@link Setting} will store.
     * @return The {@link SettingEntry} object that can construct a {@link Setting} and
     * then add it to this {@link FlexibleConfig}.
     */
    <V> SettingEntry<V> newSetting(SimpleUri id, Class<V> valueType);

    /**
     * Returns the {@link Setting<V>} with the given id. Null is returned if a setting with the given id does not
     * exist in the config.
     *
     * @param <V> The type of the value the retrieved {@link Setting} must contain.
     * @param id  The id of the {@link Setting} to retrieve.
     * @param valueType The {@link Class} of the type of the value the retrieved {@link Setting} must contain.
     * @return The {@link Setting<V>}, if found in the config. Null if a {@link Setting} with the given id
     * does not exist in the config.
     * @throws ClassCastException when {@link V} does not match the type of the values stored inside the retrieved
     *                            {@link Setting}.
     */
    <V> Setting<V> get(SimpleUri id, Class<V> valueType);

    /**
     * Removes the {@link Setting} with the given id if it exists in the config and if the {@link Setting} does
     * not have any subscribers. In case of failure warnings will be issued through the logger detailing the
     * exact nature of the failure.
     *
     * @param id The id of the {@link Setting} to remove.
     * @return True if the {@link Setting} was removed, false otherwise.
     */
    boolean remove(SimpleUri id);

    /**
     * Returns a boolean stating whether the config contains a {@link Setting} with the given id.
     */
    boolean contains(SimpleUri id);

    /**
     * Returns an unmodifiable {@link Collection} containing all the settings in
     * the {@link FlexibleConfig}. The returned collection is automatically updated
     * whenever a new {@link Setting} is added or removed from the {@link FlexibleConfig}.
     *
     * @return An unmodifiable collection containing all the settings.
     */
    Collection<Setting> getSettings();

    /**
     * Returns a potentially verbose, human-readable description regarding the purpose of this {@link FlexibleConfig}.
     * This description is also written to file when the {@link FlexibleConfig} is saved.
     */
    String getDescription();

    /**
     * Saves the values of all settings having non-default values, to enable persistence across sessions.
     * Also saved for documentation purposes is the description of the {@link FlexibleConfig}, as
     * determined by {@link #getDescription()}.
     * <p>
     * All the non-default values that were not used in this session and are still "parked" are also
     * saved as-is, to be used later.
     *
     * @param writer A writer that will serve as the destination of settings.
     */
    void save(Writer writer);

    /**
     * Loads the values of the settings having non-default values, to enable persistence across sessions.
     * <p>
     * All the non-default values are loaded and "parked", initially remaining inaccessible.
     * Once a Setting object is added to the config, a corresponding non-default value is sought
     * among the parked values. If one is found it is parsed and stored in the Setting object.
     * <p>
     * Note that this function should be called -before- adding any settings to the FlexibleConfig.
     * Otherwise any corresponding parked value will never be loaded.
     *
     * @param reader A reader that will serve as the source of the settings.
     */
    void load(Reader reader);

    /**
     * Represents an under-construction {@link Setting} storing values of type {@link T}
     * which will eventually be added to the {@link FlexibleConfig} that created this object.
     * <p>
     * This type follows a state machine-like Builder pattern to make it easier to set the
     * various required and optional components of a {@link Setting}. This interface is the
     * first entry point of the Builder pattern, which allows you to set the required components
     * of the {@link Setting}.
     *
     * @param <T> The type of values that will be stored by the setting under construction.
     */
    interface SettingEntry<T> {
        /**
         * Specifies the default value of the {@link Setting} that will be constructed.
         *
         * @param defaultValue The default value of the new Setting.
         * @return A builder object to continue the {@link Setting} building process.
         */
        Builder<T> setDefaultValue(T defaultValue);

        /**
         * A builder that allows you to set the optional components of a {@link Setting},
         * and add it to the {@link FlexibleConfig} that created this object.
         *
         * @param <T> The type of values that will be stored by the setting under construction.
         */
        interface Builder<T> {
            /**
             * Specifies the {@link SettingConstraint} that will be used by the {@link Setting}.
             *
             * @param constraint The constraint for the setting.
             * @return This builder object.
             */
            Builder<T> setConstraint(SettingConstraint<T> constraint);

            /**
             * Specifies the name of the {@link Setting} being created.
             *
             * @param humanReadableName The name of the setting.
             * @return This builder object.
             */
            Builder<T> setHumanReadableName(String humanReadableName);

            /**
             * Specifies the description of the {@link Setting} being created.
             *
             * @param description The setting description.
             * @return This builder object.
             */
            Builder<T> setDescription(String description);

            /**
             * Builds the {@link Setting} with the components that have already been specified
             * and adds the constructed {@link Setting} to the {@link FlexibleConfig} that created
             * this object. Returns a boolean specifying if the addition of the constructed
             * {@link Setting} to the {@link FlexibleConfig} was successful. In case of failure,
             * warnings will be logged detailing the issues.
             *
             * @return True if the constructed setting was successfully added to the config,
             * false otherwise.
             */
            boolean addToConfig();
        }
    }
}
