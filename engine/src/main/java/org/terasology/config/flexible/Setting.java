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
import org.terasology.config.flexible.constraints.SettingConstraint;
import org.terasology.engine.SimpleUri;
import org.terasology.module.sandbox.API;
import org.terasology.reflection.TypeInfo;

import java.beans.PropertyChangeListener;

/**
 * Represents a setting uniquely identified by an id. Contains a value that may be constrained by a
 * {@link SettingConstraint} and notifies subscribers when the stored value is changed.
 *
 * @param <T> The type of the value this {@link Setting} contains.
 */
@API
public interface Setting<T> {
    /**
     * Returns the id of this {@link Setting}.
     */
    SimpleUri getId();

    /**
     * Returns a {@link TypeInfo} representing the type of values that can be stored in this
     * {@link Setting}.
     */
    TypeInfo<T> getValueType();

    /**
     * Returns the {@link SettingConstraint} used by this {@link Setting}, if present.
     * Returns null otherwise.
     */
    SettingConstraint<T> getConstraint();

    /**
     * Returns the default value of this {@link Setting}.
     */
    T getDefaultValue();

    /**
     * Returns the value stored in this {@link Setting}.
     */
    T getValue();

    /**
     * Sets the value stored in this {@link Setting<T>}. When no {@link SettingConstraint} is
     * present the new value immediately replaces the stored one and any subscriber is notified
     * of the change.
     *
     * If a {@link SettingConstraint} is present, the constraint must be satisfied by
     * the new value. If the constraint is satisfied, the new value replaces the
     * stored one and subscribers are notified.
     *
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
