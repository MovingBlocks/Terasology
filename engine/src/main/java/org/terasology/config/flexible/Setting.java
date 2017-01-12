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

import org.terasology.assets.ResourceUrn;
import org.terasology.utilities.subscribables.GeneralSubscribable;

import java.beans.PropertyChangeListener;
import java.util.List;

public class Setting<T extends SettingValue> implements GeneralSubscribable {
    private final T defaultValue;
    private ResourceUrn id;
    private T value;

    private String name;
    private String description;

    private List<PropertyChangeListener> subscribers;

    public Setting(ResourceUrn id, T defaultValue) {
        this.id = id;
        this.defaultValue = defaultValue;
    }

    public void subscribe(PropertyChangeListener listener) {
        subscribers.add(listener);
    }

    public void unsubscribe(PropertyChangeListener listener) {
        subscribers.remove(listener);
    }

    public ResourceUrn getId() {
        return id;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
