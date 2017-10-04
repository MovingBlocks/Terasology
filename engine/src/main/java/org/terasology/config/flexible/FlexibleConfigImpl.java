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

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;

import java.util.Map;
import java.util.Map.Entry;

/**
 * {@inheritDoc}
 */
public class FlexibleConfigImpl implements FlexibleConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlexibleConfigImpl.class);

    private Map<SimpleUri, Setting> settingMap;
    private Map<SimpleUri, String> unusedSettings;

    /**
     * Creates a new {@link FlexibleConfigImpl} instance.
     */
    public FlexibleConfigImpl() {
        this.settingMap = Maps.newHashMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(Setting setting) {
        SimpleUri id = setting.getId();

        if (id == null) {
            LOGGER.warn("The id of a setting cannot be null.");
            return false;
        } else if (contains(id)) {
            LOGGER.warn("A Setting with the id \"{}\" already exists.", id);
            return false;
        }

        if (unusedSettings != null) {
            if (unusedSettings.containsKey(id)) {
                setting.setValueFromString(unusedSettings.remove(id));
            }
        }

        settingMap.put(id, setting);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(SimpleUri id) {
        Setting setting = get(id);

        if (setting == null) {
            LOGGER.warn("Setting \"{}\" does not exist.", id);
            return false;
        } else if (setting.hasSubscribers()) {
            LOGGER.warn("Setting \"{}\" cannot be removed while it has subscribers.", id);
            return false;
        }

        settingMap.remove(id);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <V> Setting<V> get(SimpleUri id) {
        return (Setting<V>) settingMap.get(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(SimpleUri id) {
        return settingMap.containsKey(id);
    }

    @Override
    public void setUnusedSettings(Map<SimpleUri, String> unusedSettings) {
        this.unusedSettings = unusedSettings;
    }

    @Override
    public Map<SimpleUri, String> getUnusedSettings() {
        return unusedSettings;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();

        for (Entry<SimpleUri, Setting> entry : settingMap.entrySet()) {
            Setting setting = entry.getValue();
            if (!setting.getValue().equals(setting.getDefaultValue())) {
                jsonObject.addProperty(entry.getKey().toString(), setting.getValue().toString());
            }
        }

        // Add all the non-default settings that were not used in this session
        if (unusedSettings != null) {
            for (Entry<SimpleUri, String> unusedSettings : unusedSettings.entrySet()) {
                jsonObject.addProperty(unusedSettings.getKey().toString(), unusedSettings.getValue());
            }
        }

        return jsonObject;
    }

    /*
    public static class Adapter implements JsonSerializer<FlexibleConfigImpl> {
        @Override
        public JsonElement serialize(FlexibleConfigImpl flexibleConfigImpl, Type T, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();

            for (Entry<SimpleUri, Setting> entry : flexibleConfigImpl.settingMap.entrySet()) {
                Setting setting = entry.getValue();
                if (!setting.getValue().equals(setting.getDefaultValue())) {
                    jsonObject.addProperty(entry.getKey().toString(), setting.getValue().toString());
                }
            }

            return jsonObject;
        }
    }
    */
}
