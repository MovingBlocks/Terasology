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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * {@inheritDoc}
 */
public class FlexibleConfigImpl implements FlexibleConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlexibleConfigImpl.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String DESCRIPTION_PROPERTY_NAME = "description";

    private final Map<SimpleUri, Setting> settings = Maps.newHashMap();
    private final Map<SimpleUri, String> temporarilyParkedSettings = Maps.newHashMap();

    private final String description;

    public FlexibleConfigImpl(String description) {
        this.description = description;
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

        if (temporarilyParkedSettings.containsKey(id)) {
            setting.setValueFromJson(temporarilyParkedSettings.remove(id));
        }

        settings.put(id, setting);

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

        settings.remove(id);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <V> Setting<V> get(SimpleUri id) {
        return (Setting<V>) settings.get(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(SimpleUri id) {
        return settings.containsKey(id);
    }

    @Override
    public Map<SimpleUri, Setting> getSettings() {
        return settings;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void save(Writer writer) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty(DESCRIPTION_PROPERTY_NAME, description);

        for (Map.Entry<SimpleUri, Setting> entry : settings.entrySet()) {
            Setting setting = entry.getValue();
            if (!setting.getValue().equals(setting.getDefaultValue())) {
                jsonObject.add(entry.getKey().toString(), setting.getValueAsJson());
            }
        }

        // Add any temporarily parked setting that hasn't been used in this session of the application.
        for (Map.Entry<SimpleUri, String> entry : temporarilyParkedSettings.entrySet()) {
            jsonObject.addProperty(entry.getKey().toString(), entry.getValue());
        }

        gson.toJson(jsonObject, writer);
    }

    @Override
    public void load(Reader reader) {
        try {
            JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();

            for (Map.Entry<String, JsonElement> jsonEntry : jsonObject.entrySet()) {
                if (jsonEntry.getKey().equals(DESCRIPTION_PROPERTY_NAME)) {
                    continue;
                }

                SimpleUri id = new SimpleUri(jsonEntry.getKey());
                String valueJson = jsonEntry.getValue().toString();
                temporarilyParkedSettings.put(id, valueJson);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error parsing config file!");
        }
    }
}
