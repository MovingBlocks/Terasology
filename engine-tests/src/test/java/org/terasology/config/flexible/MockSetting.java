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

import com.google.gson.JsonElement;
import org.terasology.config.flexible.validators.SettingValueValidator;
import org.terasology.engine.SimpleUri;

import java.beans.PropertyChangeListener;

class MockSetting<T> implements Setting<T> {
    private final SimpleUri id;
    private boolean isSubscribedTo;

    MockSetting(SimpleUri id) {
        this.id = id;
    }

    @Override
    public boolean subscribe(PropertyChangeListener changeListener) {
        isSubscribedTo = true;
        return true;
    }

    @Override
    public boolean unsubscribe(PropertyChangeListener changeListener) {
        isSubscribedTo = false;
        return false;
    }

    @Override
    public SimpleUri getId() {
        return id;
    }

    @Override
    public SettingValueValidator<T> getValidator() {
        return null;
    }

    @Override
    public T getDefaultValue() {
        return null;
    }

    @Override
    public T getValue() {
        return null;
    }

    @Override
    public boolean setValue(T newValue) {
        return false;
    }

    @Override
    public String getHumanReadableName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean hasSubscribers() {
        return isSubscribedTo;
    }

    @Override
    public void setValueFromJson(String json) { }

    @Override
    public JsonElement getValueAsJson() {
        return null;
    }
}
