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

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.flexible.validators.SettingValueValidator;
import org.terasology.engine.SimpleUri;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Set;

/**
 * {@inheritDoc}
 *
 * @param <T> The type of the value this {@link SettingImpl} contains.
 */
public class SettingImpl<T> implements Setting<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingImpl.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final SimpleUri id;
    private final String warningFormatString;

    private final T defaultValue;
    private final Class<T> valueClass;

    protected T value;

    private String humanReadableName;

    private String description;
    private SettingValueValidator<T> validator;
    private Set<PropertyChangeListener> subscribers;

    /**
     * Creates a new {@link SettingImpl} with the given id and default value but no validator.
     *
     * @param id           the id of the setting.
     * @param defaultValue the default value of the setting.
     */
    public SettingImpl(SimpleUri id, T defaultValue) {
        this(id, defaultValue, null);
    }

    /**
     * Creates a new {@link SettingImpl} with the given id, default value and validator.
     * @param id           the id of the setting.
     * @param defaultValue the default value of the setting.
     * @param validator    the validator to be used to validate values.
     */
    @SuppressWarnings("unchecked")
    public SettingImpl(SimpleUri id, T defaultValue, SettingValueValidator<T> validator) {
        this.id = id;
        this.warningFormatString = MessageFormat.format("Setting {0}: '{'0}'", this.id);

        this.validator = validator;

        if (!validate(defaultValue)) {
            throw new IllegalArgumentException("The default value must be a valid value. " +
                    "Check the logs for more information.");
        }

        Preconditions.checkNotNull(defaultValue, formatWarning("The default value cannot be null."));

        this.defaultValue = defaultValue;
        this.value = this.defaultValue;
        this.valueClass = (Class<T>) defaultValue.getClass();
    }

    private String formatWarning(String s) {
        return MessageFormat.format(warningFormatString, s);
    }

    private void dispatchChangedEvent(PropertyChangeEvent event) {
        if (subscribers != null) {
            for (PropertyChangeListener subscriber : subscribers) {
                subscriber.propertyChange(event);
            }
        }
    }

    private boolean validate(T valueToValidate) {
        return validator == null || validator.validate(valueToValidate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean subscribe(PropertyChangeListener listener) {
        if (subscribers == null) {
            subscribers = Sets.newHashSet();
        }

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

        if (subscribers.size() <= 0) {
            subscribers = null;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSubscribers() {
        return subscribers != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleUri getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SettingValueValidator<T> getValidator() {
        return validator;
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

        if (!validate(newValue)) {
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
        value = GSON.fromJson(json, valueClass);
    }

    @Override
    public JsonElement getValueAsJson() {
        return GSON.toJsonTree(value);
    }
}
