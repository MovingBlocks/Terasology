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
import org.terasology.config.flexible.validators.SettingValueValidator;
import org.terasology.engine.SimpleUri;
import org.terasology.utilities.subscribables.GeneralSubscribable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class Setting<T> implements GeneralSubscribable {
    private final T defaultValue;

    private SimpleUri id;

    private T value;

    private String name;

    private String description;
    private SettingValueValidator<T> valueValidator;
    private List<PropertyChangeListener> subscribers;

    public Setting(SimpleUri id, T defaultValue, SettingValueValidator<T> valueValidator) {
        this.id = id;

        this.defaultValue = defaultValue;
        this.value = this.defaultValue;

        this.valueValidator = valueValidator;

        this.subscribers = Lists.newArrayList();
    }

    public SettingValueValidator<T> getValueValidator() {
        return valueValidator;
    }

    private void dispatchChangedEvent(PropertyChangeEvent event) {
        for (PropertyChangeListener subscriber : subscribers) {
            subscriber.propertyChange(event);
        }
    }

    public void subscribe(PropertyChangeListener listener) {
        subscribers.add(listener);
    }

    public void unsubscribe(PropertyChangeListener listener) {
        subscribers.remove(listener);
    }

    public boolean hasSubscribers() {
        return !subscribers.isEmpty();
    }

    public SimpleUri getId() {
        return id;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getValue() {
        return value;
    }

    public boolean setValue(T value) {
        if (!valueValidator.validate(value))
            return false;

        PropertyChangeEvent event = new PropertyChangeEvent(this, id.toString(), this.value, value);

        this.value = value;

        dispatchChangedEvent(event);

        return true;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
