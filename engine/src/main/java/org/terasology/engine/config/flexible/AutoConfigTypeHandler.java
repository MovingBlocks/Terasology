// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config.flexible;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.TypeInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AutoConfigTypeHandler<T extends AutoConfig> extends TypeHandler<AutoConfig> {
    private final Logger logger = LoggerFactory.getLogger(AutoConfigTypeHandler.class);
    private final TypeHandlerLibrary typeHandlerLibrary;
    private final TypeInfo<T> typeInfo;

    public AutoConfigTypeHandler(TypeInfo<T> typeInfo, TypeHandlerLibrary typeHandlerLibrary) {
        this.typeHandlerLibrary = typeHandlerLibrary;
        this.typeInfo = typeInfo;
    }

    @Override
    protected PersistedData serializeNonNull(AutoConfig value, PersistedDataSerializer serializer) {
        Set<Field> settingFields = AutoConfig.getSettingFieldsIn(typeInfo.getRawType());
        Map<String, PersistedData> fields = new HashMap<>();
        for (Field field : settingFields) {
            try {
                Setting setting = (Setting) field.get(value);
                if (!Objects.equals(setting.getDefaultValue(), setting.get())) {
                    Optional<TypeHandler<Object>> typeHandler =
                            typeHandlerLibrary.getTypeHandler(setting.getValueType());
                    if (typeHandler.isPresent()) {
                        fields.put(field.getName(), typeHandler.get().serialize(setting.get(), serializer));
                    } else {
                        logger.error("Cannot serialize type [{}]", setting.getValueType()); //NOPMD
                    }
                }
            } catch (IllegalAccessException e) {
                // ignore, `AutoConfig.gstSettingFieldsIn` return pulbic field.
            }
        }
        return serializer.serialize(fields);
    }

    @Override
    public Optional<AutoConfig> deserialize(PersistedData data) {
        try {
            AutoConfig config = typeInfo.getRawType().getConstructor().newInstance();

            Map<String, Field> settingFields = AutoConfig.getSettingFieldsIn(typeInfo.getRawType())
                    .stream()
                    .collect(Collectors.toMap(Field::getName, f -> f));

            for (Map.Entry<String, PersistedData> entry : data.getAsValueMap().entrySet()) {
                Field settingField = settingFields.get(entry.getKey());
                if (settingField == null) {
                    logger.warn("Cannot to find setting field with name [{}]", entry.getKey()); //NOPMD
                    continue;
                }
                try {
                    Setting setting = (Setting) settingField.get(config);
                    Optional<TypeHandler<Object>> typeHandler =
                            typeHandlerLibrary.getTypeHandler(setting.getValueType());
                    if (typeHandler.isPresent()) {
                        Optional<Object> value = typeHandler.get().deserialize(entry.getValue());
                        if (value.isPresent()) {
                            setting.set(value.get());
                        } else {
                            logger.error("Cannot deserialize value [{}] to type [{}]", entry.getValue(), setting.getValueType()); //NOPMD
                        }
                    } else {
                        logger.error("Cannot deserialize type [{}]", setting.getValueType()); //NOPMD
                    }
                } catch (IllegalAccessException e) {
                    // ignore, AutoConfig.getSettingsFieldsIn return public fields.
                }
            }
            return Optional.of(config);
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            logger.error("Cannot create type [{}] for deserialization", typeInfo, e);
        }
        return Optional.empty();
    }
}
