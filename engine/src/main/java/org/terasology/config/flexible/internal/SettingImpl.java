/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.engine.config.flexible.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.flexible.Setting;
import org.terasology.engine.config.flexible.SettingChangeListener;
import org.terasology.engine.config.flexible.constraints.SettingConstraint;
import org.terasology.reflection.TypeInfo;

import java.util.Set;

/**
 * {@inheritDoc}
 *
 * @param <T> The type of the value this {@link SettingImpl} contains.
 */
class SettingImpl<T> implements Setting<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingImpl.class);

    private final T defaultValue;
    private final TypeInfo<T> valueType;

    private final String humanReadableName;
    private final String description;

    private final SettingConstraint<T> constraint;
    private final Set<SettingChangeListener<T>> subscribers = Sets.newHashSet();

    protected T value;

    /**
     * Creates a new {@link SettingImpl} with the given id, default value and constraint.
     *
     * @param valueType The {@link TypeInfo} describing the type of values t
     * @param defaultValue      The default value of the setting.
     * @param constraint        The constraint that the setting values must satisfy.
     * @param humanReadableName The human readable name of the setting.
     * @param description       A description of the setting.
     */
    SettingImpl(TypeInfo<T> valueType, T defaultValue, SettingConstraint<T> constraint,
                String humanReadableName, String description) {
        this.valueType = valueType;
        this.humanReadableName = humanReadableName;
        this.description = description;

        this.constraint = constraint;

        Preconditions.checkNotNull(defaultValue, "The default value for a Setting cannot be null.");

        if (isConstraintUnsatisfiedBy(defaultValue)) {
            throw new IllegalArgumentException("The default value must be a valid value. " +
                                                   "Check the logs for more information.");
        }

        this.defaultValue = defaultValue;
        this.value = this.defaultValue;
    }

    private void dispatchChangedEvent(T oldValue) {
        for (SettingChangeListener<T> subscriber : subscribers) {
            subscriber.onValueChanged(this, oldValue);
        }
    }

    private boolean isConstraintUnsatisfiedBy(T theValue) {
        if (constraint == null || constraint.isSatisfiedBy(theValue)) {
            return false;
        } else {
            constraint.warnUnsatisfiedBy(theValue);
            return true;
        }
    }

    @Override
    public boolean subscribe(SettingChangeListener<T> listener) {
        if (listener == null) {
            LOGGER.warn("Cannot add a null subscriber to a Setting.");
            return false;
        }

        if (subscribers.contains(listener)) {
            return false;
        }

        subscribers.add(listener);

        return true;
    }

    @Override
    public boolean unsubscribe(SettingChangeListener<T> listener) {
        if (!subscribers.contains(listener)) {
            return false;
        }

        subscribers.remove(listener);

        return true;
    }

    @Override
    public boolean hasSubscribers() {
        return !subscribers.isEmpty();
    }

    @Override
    public TypeInfo<T> getValueType() {
        return valueType;
    }

    @Override
    public SettingConstraint<T> getConstraint() {
        return constraint;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean set(T newValue) {
        Preconditions.checkNotNull(newValue, "The value of a setting cannot be null.");

        if (isConstraintUnsatisfiedBy(newValue)) {
            return false;
        }

        if (newValue.equals(this.value)) {
            return false;
        }

        T oldValue = this.value;

        this.value = newValue;

        dispatchChangedEvent(oldValue);

        return true;
    }

    @Override
    public String getHumanReadableName() {
        return humanReadableName;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
