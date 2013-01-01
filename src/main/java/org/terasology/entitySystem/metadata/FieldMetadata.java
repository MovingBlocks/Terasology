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
package org.terasology.entitySystem.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.network.Replicate;
import org.terasology.protobuf.EntityData;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class FieldMetadata {
    private static final Logger logger = LoggerFactory.getLogger(FieldMetadata.class);

    private Field field;
    private Method getter;
    private Method setter;
    private TypeHandler serializationHandler;
    private Replicate replicationInfo;

    public FieldMetadata(Field field, Class type, TypeHandler handler) {
        this.field = field;
        this.serializationHandler = handler;
        this.replicationInfo = field.getAnnotation(Replicate.class);
        getter = findGetter(type, field);
        setter = findSetter(type, field);
    }

    public Object deserialize(EntityData.Value value) {
        return serializationHandler.deserialize(value);
    }

    public Object copy(Object field) {
        return serializationHandler.copy(field);
    }

    public String getName() {
        return field.getName();
    }

    public Class<?> getType() {
        return field.getType();
    }

    public Object getValue(Object obj) throws IllegalAccessException, InvocationTargetException {
        if (getter != null) {
            return getter.invoke(obj);
        }
        return field.get(obj);
    }

    public void setValue(Object target, Object value) throws IllegalAccessException, InvocationTargetException {
        if (setter != null) {
            setter.invoke(target, value);
        } else {
            field.set(target, value);
        }
    }

    public boolean isReplicated() {
        return replicationInfo != null;
    }

    public Replicate getReplicationInfo() {
        return replicationInfo;
    }

    private Method findGetter(Class type, Field field) {
        Method result = findMethod(type, "get" + field.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) + field.getName().substring(1));
        if (result != null && field.getType().equals(result.getReturnType())) {
            return result;
        }
        result = findMethod(type, "is" + field.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) + field.getName().substring(1));
        if (result != null && field.getType().equals(result.getReturnType())) {
            return result;
        }
        return null;
    }

    private Method findSetter(Class type, Field field) {
        return findMethod(type, "set" + field.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) + field.getName().substring(1), field.getType());
    }

    private Method findMethod(Class type, String methodName, Class<?>... parameters) {
        try {
            return type.getMethod(methodName, parameters);
        } catch (NoSuchMethodException nsme) {
            // Not really that exceptional
        }
        return null;
    }

    /**
     * Serializes the given value, that was originally obtained from this field.
     * <p/>
     * This is provided for performance, to avoid obtaining the same value twice via reflection.
     * @param rawValue
     * @return
     */
    public EntityData.Value serializeValue(Object rawValue) {
        return serializationHandler.serialize(rawValue);
    }

    /**
     * Serializes the field for the given object
     * @param event
     * @return
     */
    public EntityData.NameValue serialize(Object event) {
        try {
            Object rawValue = getValue(event);
            if (rawValue == null) {
                return null;
            }

            EntityData.Value value = serializeValue(rawValue);
            if (value != null) {
                return EntityData.NameValue.newBuilder().setName(field.getName()).setValue(value).build();
            }
        } catch (IllegalAccessException e) {
            logger.error("Exception during serializing of {}", event.getClass(), e);
        } catch (InvocationTargetException e) {
            logger.error("Exception during serializing of {}", event.getClass(), e);
        }
        return null;
    }
}
