/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.entitySystem.metadata.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.terasology.entitySystem.metadata.AbstractTypeHandler;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.protobuf.EntityData;

import com.google.common.collect.Maps;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class MappedContainerTypeHandler<T> extends AbstractTypeHandler<T> {

    private static Logger logger = Logger.getLogger(MappedContainerTypeHandler.class.getName());

    private Class<T> clazz;
    private Map<String, FieldMetadata> fields = Maps.newHashMap();

    public MappedContainerTypeHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void addField(FieldMetadata info) {
        fields.put(info.getName().toLowerCase(Locale.ENGLISH), info);
    }

    public EntityData.Value serialize(T value) {
        if (value == null) return null;

        EntityData.Value.Builder result = EntityData.Value.newBuilder();

        try {
            for (FieldMetadata fieldInfo : fields.values()) {
                Object rawValue = fieldInfo.getValue(value);
                if (rawValue == null)
                    continue;

                EntityData.Value fieldValue = fieldInfo.serialize(rawValue);
                if (fieldValue != null) {
                    result.addNameValue(EntityData.NameValue.newBuilder().setName(fieldInfo.getName()).setValue(fieldValue).build());
                }
            }
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Unable to serialize field of " + value.getClass(), e);
        } catch (InvocationTargetException e) {
            logger.log(Level.SEVERE, "Unable to serialize field of " + value.getClass(), e);
        }
        return result.build();
    }

    public T deserialize(EntityData.Value value) {
        try {
            T result = clazz.newInstance();
            for (EntityData.NameValue entry : value.getNameValueList()) {
                FieldMetadata fieldInfo = fields.get(entry.getName().toLowerCase(Locale.ENGLISH));
                if (fieldInfo != null) {
                    Object content = fieldInfo.deserialize(entry.getValue());
                    if (content != null) {
                        fieldInfo.setValue(result, content);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to deserialize " + value, e);
        }
        return null;
    }

    public T copy(T value) {
        if (value != null) {
            try {
                T result = clazz.newInstance();
                for (FieldMetadata field : fields.values()) {
                    field.setValue(result, field.copy(field.getValue(value)));
                }
                return result;
            } catch (InstantiationException e) {
                logger.log(Level.SEVERE, "Unable to clone " + value.getClass(), e);
            } catch (IllegalAccessException e) {
                logger.log(Level.SEVERE, "Unable to clone " + value.getClass(), e);
            } catch (InvocationTargetException e) {
                logger.log(Level.SEVERE, "Unable to clone " + value.getClass(), e);
            }
        }
        return null;
    }

}
