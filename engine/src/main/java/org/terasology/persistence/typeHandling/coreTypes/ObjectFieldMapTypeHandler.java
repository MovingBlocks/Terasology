/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.engine.module.UriUtil;
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Map;

/**
 */
public class ObjectFieldMapTypeHandler<T> implements TypeHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(ObjectFieldMapTypeHandler.class);

    private Map<String, FieldMetadata<T, ?>> fieldByName = Maps.newHashMap();
    private Map<FieldMetadata<T, ?>, TypeHandler<?>> mappedFields;
    private Class<T> clazz;

    public ObjectFieldMapTypeHandler(Class<T> clazz, Map<FieldMetadata<T, ?>, TypeHandler<?>> mappedFields) {
        this.clazz = clazz;
        this.mappedFields = mappedFields;
        for (FieldMetadata<T, ?> field : mappedFields.keySet()) {
            this.fieldByName.put(field.getName(), field);
        }
    }

    @Override
    public PersistedData serialize(T value, SerializationContext context) {
        if (value == null) {
            return context.createNull();
        }
        Map<String, PersistedData> mappedData = Maps.newLinkedHashMap();
        for (Map.Entry<FieldMetadata<T, ?>, TypeHandler<?>> entry : mappedFields.entrySet()) {
            Object val = entry.getKey().getValue(value);
            if (val != null) {
                TypeHandler handler = entry.getValue();
                PersistedData fieldValue = handler.serialize(val, context);
                if (fieldValue != null) {
                    mappedData.put(entry.getKey().getName(), fieldValue);
                }
            }
        }
        return context.create(mappedData);
    }

    @Override
    public T deserialize(PersistedData data, DeserializationContext context) {
        try {
            T result = clazz.newInstance();
            for (Map.Entry<String, PersistedData> entry : data.getAsValueMap().entrySet()) {
                FieldMetadata fieldInfo = fieldByName.get(entry.getKey());
                if (fieldInfo != null) {
                    TypeHandler handler = mappedFields.get(fieldInfo);
                    Object val = handler.deserialize(entry.getValue(), context);
                    fieldInfo.setValue(result, val);
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("Unable to deserialize {}", data, e);
        }
        return null;
    }

}
