/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.persistence.typeSerialization.typeHandlers.core;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.module.UriUtil;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.persistence.typeSerialization.typeHandlers.SimpleTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.Locale;
import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class MappedContainerTypeHandler<T> extends SimpleTypeHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(MappedContainerTypeHandler.class);

    private Map<String, FieldMetadata<T, ?>> fieldByName = Maps.newHashMap();
    private Map<FieldMetadata<T, ?>, TypeHandler<?>> mappedFields;
    private Class<T> clazz;

    public MappedContainerTypeHandler(Class<T> clazz, Map<FieldMetadata<T, ?>, TypeHandler<?>> mappedFields) {
        this.clazz = clazz;
        this.mappedFields = mappedFields;
        for (FieldMetadata<T, ?> field : mappedFields.keySet()) {
            this.fieldByName.put(UriUtil.normalise(field.getName()), field);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public EntityData.Value serialize(T value) {
        if (value == null) {
            return null;
        }

        EntityData.Value.Builder result = EntityData.Value.newBuilder();

        for (Map.Entry<FieldMetadata<T, ?>, TypeHandler<?>> entry : mappedFields.entrySet()) {
            Object val = entry.getKey().getValue(value);
            TypeHandler handler = entry.getValue();
            EntityData.Value fieldValue = handler.serialize(val);
            if (fieldValue != null) {
                result.addNameValue(EntityData.NameValue.newBuilder().setName(entry.getKey().getName()).setValue(fieldValue).build());
            }
        }
        return result.build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(EntityData.Value value) {
        try {
            T result = clazz.newInstance();
            for (EntityData.NameValue entry : value.getNameValueList()) {
                FieldMetadata fieldInfo = fieldByName.get(entry.getName().toLowerCase(Locale.ENGLISH));
                if (fieldInfo != null) {
                    TypeHandler handler = mappedFields.get(fieldInfo);
                    Object val = handler.deserialize(entry.getValue());
                    fieldInfo.setValue(result, val);
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("Unable to deserialize {}", value, e);
        }
        return null;
    }

}
