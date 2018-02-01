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

import com.google.gson.JsonElement;
import org.terasology.config.flexible.validators.SettingValueValidator;
import org.terasology.engine.SimpleUri;

import java.beans.PropertyChangeListener;

/**
 * Represents a setting uniquely identified by an id. Contains a value that may be validated by a
 * {@link SettingValueValidator<T>} and notifies subscribers when the stored value is changed.
 * @param <T> The type of the value this {@link Setting} contains.
 */
public interface Setting<T> {
    /**
     * Returns the id of this {@link Setting}.
     */
    SimpleUri getId();

    /**
     * Returns the {@link SettingValueValidator<T>} used by this {@link Setting<T>}, if present.
     * Returns null otherwise.
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
     * Sets the value stored in this {@link Setting<T>}. When no Validator is
     * present the new value immediately replaces the stored one and any subscriber is notified of the change.
     * If a Validator is present, the value is first validated. Only if the value is valid it replaces the
     * stored one and subscribers are notified.
     * @param newValue The new value to store.
     * @return True if the value was stored successfully, false otherwise.
     */
    boolean setValue(T newValue);

    /**
     * Resets the value stored in this {@link Setting<T>} to the default value.
     */
    default void resetValue() {
        setValue(getDefaultValue());
    }

    /**
     * Returns the human readable name of this {@link Setting}.
     */
    String getHumanReadableName();

    /**
     * Returns a potentially verbose description of this {@link Setting}.
     */
    String getDescription();

    /**
     * Subscribe a {@link PropertyChangeListener} that will be notified when the value stored in the setting
     * changes. In case of failure warnings will be issued through the logger detailing the exact nature of the failure.
     *
     * @param listener The {@link PropertyChangeListener} to subscribe.
     * @return True if the {@link PropertyChangeListener} was subscribed, false otherwise.
     */
    boolean subscribe(PropertyChangeListener listener);

    /**
     * Unsubscribe a {@link PropertyChangeListener} that will be notified when the value stored in the setting
     * changes. In case of failure warnings will be issued through the logger detailing the exact nature of the failure.
     *
     * @param listener The {@link PropertyChangeListener} to unsubscribe.
     * @return True if the {@link PropertyChangeListener} was unsubscribed, false otherwise.
     */
    boolean unsubscribe(PropertyChangeListener listener);

    /**
     * Returns a boolean stating whether this {@link Setting} has any subscribers.
     */
    boolean hasSubscribers();

    /**
     * Sets the value of this {@link Setting} from a JSON representation encoded in a string.
     */
    void setValueFromJson(String json);

    /**
     * Returns a JSON representation of the value stored in this {@link Setting}.
     */
    JsonElement getValueAsJson();
}
