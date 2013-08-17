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
package org.terasology.entitySystem.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Owns;
import org.terasology.network.NoReplicate;
import org.terasology.network.Replicate;
import org.terasology.protobuf.EntityData;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

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
    private boolean ownedReference;

    public FieldMetadata(Field field, TypeHandler handler, boolean replicatedByDefault) {
        this.field = field;
        this.serializationHandler = handler;
        this.replicated = replicatedByDefault;
        if (field.getAnnotation(NoReplicate.class) != null) {
            replicated = false;
        }
        if (field.getAnnotation(Replicate.class) != null) {
            replicated = true;
        }
        this.replicationInfo = field.getAnnotation(Replicate.class);
        ownedReference = field.getAnnotation(Owns.class) != null && (EntityRef.class.isAssignableFrom(field.getType()) || isCollectionOf(EntityRef.class, field));
        getter = findGetter(field);
        setter = findSetter(field);
    }

    private boolean isCollectionOf(Class<?> targetType, Field field) {
        return (Collection.class.isAssignableFrom(field.getType()) && ReflectionUtil.getTypeParameter(field.getGenericType(), 0) == targetType)
                || (Map.class.isAssignableFrom(field.getType()) && ReflectionUtil.getTypeParameter(field.getGenericType(), 1) == targetType);
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
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.error("Exception during access of {} from {}", field.getName(), obj.getClass(), e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Object getCopyOfValue(Object obj) {
        return serializationHandler.copy(getValue(obj));
    }

    public void setValue(Object target, Object value) {
        try {
            if (setter != null) {
                setter.invoke(target, value);
            } else {
                field.set(target, value);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.error("Exception during setting of {} from {}", field.getName(), target.getClass(), e);
        }
    }

    public boolean isReplicated() {
        return replicated;
    }

    public boolean isOwnedReference() {
        return ownedReference;
    }

    public Replicate getReplicationInfo() {
        return replicationInfo;
    }

    private Method findGetter(Field field) {
        Method result = findMethod(field.getDeclaringClass(), "get" + field.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) + field.getName().substring(1));
        if (result != null && field.getType().equals(result.getReturnType())) {
            result.setAccessible(true);
            return result;
        }
        result = findMethod(field.getDeclaringClass(), "is" + field.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) + field.getName().substring(1));
        if (result != null && field.getType().equals(result.getReturnType())) {
            result.setAccessible(true);
            return result;
        }
        return null;
    }

    private Method findSetter(Field field) {
        String setterName = "set" + field.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) + field.getName().substring(1);
        Method result = findMethod(field.getDeclaringClass(), setterName, field.getType());
        if (result != null) {
            result.setAccessible(true);
        }
        return result;
    }

    private Method findMethod(Class<?> type, String methodName, Class<?>... parameters) {
        try {
            return type.getMethod(methodName, parameters);
        } catch (NoSuchMethodException me) {
            // We're expecting not to find methods
            return null;
        }
    }

    /**
     * Serializes the given value, that was originally obtained from this field.
     * <p/>
     * This is provided for performance, to avoid obtaining the same value twice via reflection.
     *
     * @param rawValue The value to serialize
     * @return The serialized value
     */
    @SuppressWarnings("unchecked")
    public EntityData.Value serializeValue(Object rawValue) {
        return serializationHandler.serialize(rawValue);
    }

    @SuppressWarnings("unchecked")
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
     * @param container The object containing this field
     * @return The Name-Value pair holding this field
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
