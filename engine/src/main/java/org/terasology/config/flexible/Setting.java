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

import org.terasology.config.flexible.validators.SettingValueValidator;
import org.terasology.engine.SimpleUri;
import org.terasology.utilities.subscribables.GeneralSubscribable;

/**
 * Represents a setting stored in a {@link FlexibleConfig} identified by an id. Contains a value that can
 * optionally be validated by a {@link SettingValueValidator<T>} and notifies its subscribers when the value
 * stored is changed.
 * @param <T> The type of the value this {@link Setting} contains.
 */
public interface Setting<T> extends GeneralSubscribable {
    /**
     * Returns the id of this {@link Setting}.
     */
    SimpleUri getId();

    /**
     * Returns the {@link SettingValueValidator<T>} used by this {@link Setting<T>}.
     */
    SettingValueValidator<T> getValidator();

    /**
     * Returns the default value of this {@link Setting}.
     */
    T getDefaultValue();

    /**
     * Returns the value stored in this {@link Setting}.
     */
    T getValue();

    /**
     * Sets the value stored in this {@link Setting<T>} if the passed value is valid. If it is invalid, the stored
     * value is not updated. On successfully updating the value, the subscribers are notified of the change.
     * @param newValue The new value to store.
     * @return True if the value was valid and was stored successfully, false otherwise.
     */
    boolean setValue(T newValue);

    /**
     * Returns the human readable name of this {@link Setting}.
     */
    String getHumanReadableName();

    /**
     * Returns the description of this {@link Setting}.
     */
    String getDescription();

    /**
     * Returns a boolean stating whether this {@link Setting} has any subscribers.
     */
    boolean hasSubscribers();
}
