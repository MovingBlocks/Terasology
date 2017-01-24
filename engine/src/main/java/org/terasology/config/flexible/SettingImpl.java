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

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.flexible.validators.SettingValueValidator;
import org.terasology.engine.SimpleUri;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class SettingImpl<T> implements Setting<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingImpl.class);
    private final T defaultValue;

    private SimpleUri id;

    private T value;

    private String humanReadableName;

    private String description;
    private SettingValueValidator<T> validator;
    private List<PropertyChangeListener> subscribers;

    public SettingImpl(SimpleUri id, T defaultValue) {
        this(id, defaultValue, null);
    }

    public SettingImpl(SimpleUri id, T defaultValue, SettingValueValidator<T> validator) {
        this.id = id;

        this.validator = validator;

        if (!validate(defaultValue))
            throw new IllegalArgumentException("The default value must be a valid value.");

        this.defaultValue = defaultValue;
        this.value = this.defaultValue;

        this.subscribers = null;
    }

    private void dispatchChangedEvent(PropertyChangeEvent event) {
        for (PropertyChangeListener subscriber : subscribers) {
            subscriber.propertyChange(event);
        }
    }

    private boolean validate(T value) {
        return validator == null || validator.validate(value);
    }

    public void subscribe(PropertyChangeListener listener) {
        if (subscribers == null) {
            subscribers = Lists.newArrayList();
        }

        subscribers.add(listener);
    }

    public void unsubscribe(PropertyChangeListener listener) {
        subscribers.remove(listener);

        if (subscribers.size() <= 0) {
            subscribers = null;
        }
    }

    public boolean hasSubscribers() {
        return subscribers != null;
    }

    public SimpleUri getId() {
        return id;
    }

    public SettingValueValidator<T> getValidator() {
        return validator;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getValue() {
        return value;
    }

    public boolean setValue(T newValue) {
        if (!validator.validate(newValue)) {
            LOGGER.warn("The passed value {} is invalid.", newValue);
            return false;
        }

        PropertyChangeEvent event = new PropertyChangeEvent(this, id.toString(), this.value, newValue);
        this.value = newValue;
        dispatchChangedEvent(event);

        return true;
    }

    public String getHumanReadableName() {
        return humanReadableName;
    }

    public String getDescription() {
        return description;
    }
}
