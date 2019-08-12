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
package org.terasology.config.flexible.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.flexible.Setting;
import org.terasology.config.flexible.constraints.SettingConstraint;
import org.terasology.engine.SimpleUri;
import org.terasology.reflection.TypeInfo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Set;

/**
 * {@inheritDoc}
 *
 * @param <T> The type of the value this {@link SettingImpl} contains.
 */
class SettingImpl<T> implements Setting<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingImpl.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final SimpleUri id;
    private final String warningFormatString;

    private final T defaultValue;
    private final TypeInfo<T> valueType;

    private final String humanReadableName;
    private final String description;

    private final SettingConstraint<T> constraint;
    private final Set<PropertyChangeListener> subscribers = Sets.newHashSet();

    protected T value;

    /**
     * Creates a new {@link SettingImpl} with the given id, default value and constraint.
     *  @param id                The id of the setting.
     * @param valueType
     * @param defaultValue      The default value of the setting.
     * @param constraint        The constraint that the setting values must satisfy.
     * @param humanReadableName The human readable name of the setting.
     * @param description       A description of the setting.
     */
    SettingImpl(SimpleUri id, TypeInfo<T> valueType, T defaultValue, SettingConstraint<T> constraint,
                String humanReadableName, String description) {
        this.id = id;
        this.valueType = valueType;
        this.humanReadableName = humanReadableName;
        this.description = description;

        this.warningFormatString = MessageFormat.format("Setting {0}: '{'0}'", this.id);

        this.constraint = constraint;

        if (isConstraintUnsatisfiedBy(defaultValue)) {
            throw new IllegalArgumentException("The default value must be a valid value. " +
                    "Check the logs for more information.");
        }

        Preconditions.checkNotNull(defaultValue, formatWarning("The default value cannot be null."));

        this.defaultValue = defaultValue;
        this.value = this.defaultValue;
    }

    private String formatWarning(String s) {
        return MessageFormat.format(warningFormatString, s);
    }

    private void dispatchChangedEvent(PropertyChangeEvent event) {
        for (PropertyChangeListener subscriber : subscribers) {
            subscriber.propertyChange(event);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean subscribe(PropertyChangeListener listener) {
        if (listener == null) {
            LOGGER.warn(formatWarning("A null subscriber cannot be added."));
            return false;
        }

        if (subscribers.contains(listener)) {
            LOGGER.warn(formatWarning("The listener has already been subscribed."));
            return false;
        }

        subscribers.add(listener);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unsubscribe(PropertyChangeListener listener) {
        if (!subscribers.contains(listener)) {
            LOGGER.warn(formatWarning("The listener does not exist in the subscriber list."));
            return false;
        }

        subscribers.remove(listener);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSubscribers() {
        return !subscribers.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleUri getId() {
        return id;
    }

    @Override
    public TypeInfo<T> getValueType() {
        return valueType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SettingConstraint<T> getConstraint() {
        return constraint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setValue(T newValue) {
        Preconditions.checkNotNull(newValue, formatWarning("The value of a setting cannot be null."));

        if (isConstraintUnsatisfiedBy(newValue)) {
            return false;
        }

        PropertyChangeEvent event = new PropertyChangeEvent(this, id.toString(), this.value, newValue);
        this.value = newValue;
        dispatchChangedEvent(event);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHumanReadableName() {
        return humanReadableName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setValueFromJson(String json) {
        value = GSON.fromJson(json, valueType.getRawType());
    }

    @Override
    public JsonElement getValueAsJson() {
        return GSON.toJsonTree(value);
    }
}
