// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.config.flexible;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.serializers.gson.GsonPersistedData;
import org.terasology.persistence.serializers.gson.GsonPersistedDataSerializer;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataMap;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

public class AutoConfigSerializer<C extends AutoConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoConfigSerializer.class);

    private final Map<Field, TypeHandler<?>> settingFieldHandlers;
    private final Class<C> configType;

    public AutoConfigSerializer(Class<C> configType, TypeHandlerLibrary typeHandlerLibrary) {
        this.settingFieldHandlers = Maps.newHashMap();
        this.configType = configType;

        for (Field settingField : AutoConfig.getSettingFieldsIn(configType)) {
            // Should be a field of type Setting<T>, settingValueType will equal T
            Type settingValueType = ReflectionUtil.getTypeParameter(settingField.getGenericType(), 0);
            Optional<TypeHandler<?>> typeHandler = typeHandlerLibrary.getTypeHandler(settingValueType);

            if (!typeHandler.isPresent()) {
                LOGGER.error(
                    "Setting {} of type {} in config {} does not" +
                        " have an appropriate TypeHandler for its values",
                    settingField.getName(),
                    settingValueType.getTypeName(),
                    configType.getSimpleName()
                );
                continue;
            }

            settingFieldHandlers.put(settingField, typeHandler.get());
        }
    }

    public JsonElement serialize(C config) {
        GsonPersistedData serialized = (GsonPersistedData) serialize(config, new GsonPersistedDataSerializer());

        return serialized.getElement();
    }

    public PersistedData serialize(C config, PersistedDataSerializer serializer) {
        // TODO: Serialize config name and description?
        Map<String, PersistedData> persistedSettings = Maps.newLinkedHashMap();

        for (Map.Entry<Field, TypeHandler<?>> fieldHandlerEntry : settingFieldHandlers.entrySet()) {
            Field settingField = fieldHandlerEntry.getKey();

            Setting<?> setting = getSettingInField(config, settingField);

            if (setting == null) {
                continue;
            }

            if (!setting.get().equals(setting.getDefaultValue())) {
                TypeHandler typeHandler = fieldHandlerEntry.getValue();
                PersistedData persistedSetting = typeHandler.serialize(setting.get(), serializer);

                persistedSettings.put(getSettingPropertyName(settingField, setting), persistedSetting);
            }
        }

        return serializer.serialize(persistedSettings);
    }

    public void deserializeOnto(C config, JsonElement data) {
        deserializeOnto(config, new GsonPersistedData(data));
    }

    public void deserializeOnto(C config, PersistedData data) {
        if (!data.isValueMap()) {
            LOGGER.error("Config must always be specified as a value map");
        }

        PersistedDataMap persistedSettings = data.getAsValueMap();

        // Since all settings are public final fields, they should have been initialized
        // in the constructor  when the new config was created

        Map<String, Field> settingFieldsByName = Maps.newHashMap();

        for (Field field : settingFieldHandlers.keySet()) {
            Setting<?> setting = getSettingInField(config, field);

            if (setting == null) {
                continue;
            }

            settingFieldsByName.put(getSettingPropertyName(field, setting), field);
        }

        for (Map.Entry<String, PersistedData> persistedSetting : persistedSettings.entrySet()) {
            Field field = settingFieldsByName.get(persistedSetting.getKey());

            if (field == null) {
                LOGGER.warn(
                    "Could not find setting with name {} in config {}",
                    persistedSetting.getKey(),
                    config.getId()
                );
                continue;
            }

            TypeHandler<?> typeHandler = settingFieldHandlers.get(field);
            Optional<?> settingValue = typeHandler.deserialize(persistedSetting.getValue());

            if (!settingValue.isPresent()) {
                LOGGER.error(
                    "Could not deserialize value of setting {} in config {} from {}",
                    persistedSetting.getKey(),
                    config.getId(),
                    persistedSetting.getValue()
                );
                continue;
            }

            Setting setting = getSettingInField(config, field);

            if (!setting.set(settingValue.get())) {
                LOGGER.warn(
                    "Could not store deserialized value {} in setting {} in config {}",
                    settingValue.get(),
                    persistedSetting.getKey(),
                    config.getId()
                );
            }
        }
    }

    private String getSettingPropertyName(Field settingField, Setting<?> setting) {
        // TODO: Use id?
        String humanReadableName = setting.getHumanReadableName();

        if (humanReadableName == null || humanReadableName.isEmpty()) {
            return settingField.getName();
        }

        return humanReadableName;
    }

    private Setting<?> getSettingInField(C config, Field settingField) {
        try {
            return (Setting<?>) settingField.get(config);
        } catch (IllegalAccessException | ClassCastException e) {
            // This should never happen since fields are preprocessed
            LOGGER.error(
                "Field {} in config {} is not a public field of type Setting",
                settingField.getName(),
                settingField.getDeclaringClass().getSimpleName()
            );

            return null;
        }
    }
}
