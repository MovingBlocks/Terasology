/*
 * Copyright 2013 Moving Blocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.network.Replicate;
import org.terasology.protobuf.EntityData;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class FieldMetadata {
    private static final Logger logger = LoggerFactory.getLogger(FieldMetadata.class);

    private byte id;
    private Field field;
    private Method getter;
    private Method setter;
    private TypeHandler serializationHandler;
    private boolean replicated;
    private Replicate replicationInfo;

    public FieldMetadata(Field field, Class type, TypeHandler handler, boolean replicated) {
        this.field = field;
        this.serializationHandler = handler;
        this.replicated = replicated;
        this.replicationInfo = field.getAnnotation(Replicate.class);
        getter = findGetter(type, field);
        setter = findSetter(type, field);
    }

    public Object deserialize(EntityData.Value value) {
        return serializationHandler.deserialize(value);
    }

    public void deserializeOnto(Object target, EntityData.Value value) {
        Object deserializedValue = deserialize(value);
        if (deserializedValue != null) {
            setValue(target, deserializedValue);
        }
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

    void setId(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    public Object getValue(Object obj) {
        try {
            if (getter != null) {
                return getter.invoke(obj);
            }
            return field.get(obj);
        } catch (InvocationTargetException e) {
            logger.error("Exception during access of {} from {}", field.getName(), obj.getClass(), e);
        } catch (IllegalAccessException e) {
            logger.error("Exception during access of {} from {}", field.getName(), obj.getClass(), e);
        }
        return null;
    }

    public void setValue(Object target, Object value) {
        try {
            if (setter != null) {
                setter.invoke(target, value);
            } else {
                field.set(target, value);
            }
        } catch (InvocationTargetException e) {
            logger.error("Exception during setting of {} from {}", field.getName(), target.getClass(), e);
        } catch (IllegalAccessException e) {
            logger.error("Exception during setting of {} from {}", field.getName(), target.getClass(), e);
        }
    }

    public boolean isReplicated() {
        return replicated;
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
     *
     * @param rawValue
     * @return
     */
    public EntityData.Value serializeValue(Object rawValue) {
        return serializationHandler.serialize(rawValue);
    }

    public EntityData.Value serialize(Object container) {
        Object rawValue = getValue(container);
        if (rawValue != null) {
            return serializationHandler.serialize(rawValue);
        }
        return null;
    }

    /**
     * Serializes the field for the given object
     *
     * @param container
     * @return
     */
    public EntityData.NameValue serializeNameValue(Object container, boolean usingFieldIds) {
        Object rawValue = getValue(container);
        if (rawValue == null) {
            return null;
        }

        EntityData.Value value = serializeValue(rawValue);
        if (value != null) {
            if (usingFieldIds) {
                return EntityData.NameValue.newBuilder().setNameIndex(id).setValue(value).build();
            } else {
                return EntityData.NameValue.newBuilder().setName(field.getName()).setValue(value).build();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return field.getName();
    }
}
