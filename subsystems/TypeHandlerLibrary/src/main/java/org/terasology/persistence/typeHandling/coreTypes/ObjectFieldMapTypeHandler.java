// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.base.Defaults;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.annotations.SerializedName;
import org.terasology.reflection.ReflectionUtil;
import org.terasology.reflection.reflect.ObjectConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Serializes objects as a fieldName → fieldValue map. It is used as the last resort while serializing an
 * object through a {@link TypeHandlerLibrary}.
 */
public class ObjectFieldMapTypeHandler<T> extends TypeHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(ObjectFieldMapTypeHandler.class);

    private Map<String, Field> fieldByName = Maps.newHashMap();
    private Map<Field, TypeHandler<?>> mappedFields;
    private ObjectConstructor<T> constructor;

    public ObjectFieldMapTypeHandler(ObjectConstructor<T> constructor, Map<Field, TypeHandler<?>> fieldTypeHandlers) {
        this.constructor = constructor;
        this.mappedFields = fieldTypeHandlers;
        for (Field field : fieldTypeHandlers.keySet()) {
            this.fieldByName.put(getFieldName(field), field);
        }
    }

    @Override
    public PersistedData serializeNonNull(T value, PersistedDataSerializer serializer) {
        Map<String, PersistedData> mappedData = Maps.newLinkedHashMap();
        for (Map.Entry<Field, TypeHandler<?>> entry : mappedFields.entrySet()) {
            Field field = entry.getKey();

            Object val;

            try {
                if (Modifier.isPrivate(field.getModifiers())) {
                    val = ReflectionUtil.findGetter(field).invoke(value);
                } else {
                    val = field.get(value);
                }
            } catch (IllegalAccessException e) {
                logger.error("Field {} is inaccessible", field);
                continue;
            } catch (InvocationTargetException e) {
                logger.error("Failed to invoke getter for field {}", field);
                continue;
            }

            if (!Objects.equals(val, Defaults.defaultValue(field.getType()))) {
                TypeHandler handler = entry.getValue();
                try {
                    PersistedData fieldValue = handler.serialize(val, serializer);
                    if (fieldValue != null) {
                        mappedData.put(getFieldName(field), fieldValue);
                    }
                } catch (StackOverflowError e) {
                    logger.error("Likely circular reference in field {}.", field);
                    throw e;
                }
            }
        }
        return serializer.serialize(mappedData);
    }

    private String getFieldName(Field field) {
        SerializedName serializedName = field.getAnnotation(SerializedName.class);

        if (serializedName == null) {
            return field.getName();
        }
        
        return serializedName.value();
    }

    @Override
    public Optional<T> deserialize(PersistedData data) {
        if (!data.isValueMap()) {
            return Optional.empty();
        }

        try {
            T result = constructor.construct();
            for (Map.Entry<String, PersistedData> entry : data.getAsValueMap().entrySet()) {
                String fieldName = entry.getKey();
                Field field = fieldByName.get(fieldName);

                if (field == null) {
                    logger.error("Could not find field with name {}", fieldName);
                    continue;
                }

                TypeHandler handler = mappedFields.get(field);
                Optional<?> fieldValue = handler.deserialize(entry.getValue());

                if (fieldValue.isPresent()) {
                    if (Modifier.isPrivate(field.getModifiers())) {
                        try {
                            ReflectionUtil.findSetter(field).invoke(result);
                        } catch (InvocationTargetException e) {
                            logger.error("Failed to invoke setter for field {}", field);
                        }
                    } else {
                        field.set(result, fieldValue.get());
                    }
                } else {
                    logger.error("Could not deserialize field {}", field.getName());
                }
            }
            return Optional.ofNullable(result);
        } catch (Exception e) {
            logger.error("Unable to deserialize {}", data, e);
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fields", fieldByName.keySet())
                .add("constructor", constructor)
                .toString();
    }
}
