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
package org.terasology.entitySystem.metadata.reflected;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Owns;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.TypeHandler;
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
public class ReflectedFieldMetadata<T, U> implements FieldMetadata<T, U> {
    private static final Logger logger = LoggerFactory.getLogger(ReflectedFieldMetadata.class);

    private ReflectedClassMetadata<T> owner;
    private Class<U> type;
    private byte id;
    private Field field;
    private Method getter;
    private Method setter;
    private TypeHandler<U> serializationHandler;
    private boolean replicated;
    private Replicate replicationInfo;
    private boolean ownedReference;

    @SuppressWarnings("unchecked")
    ReflectedFieldMetadata(ReflectedClassMetadata<T> owner, Field field, TypeHandler<U> handler, boolean replicatedByDefault) {
        this.owner = owner;
        this.field = field;
        this.serializationHandler = handler;
        type = (Class<U>) field.getType();
        getter = findGetter();
        setter = findSetter();
        owner.addField(this);

        // TODO: Maybe move these into child classes.
        this.replicated = replicatedByDefault;
        if (field.getAnnotation(NoReplicate.class) != null) {
            replicated = false;
        }
        if (field.getAnnotation(Replicate.class) != null) {
            replicated = true;
        }
        this.replicationInfo = field.getAnnotation(Replicate.class);
        ownedReference = field.getAnnotation(Owns.class) != null && (EntityRef.class.isAssignableFrom(field.getType()) || isCollectionOf(EntityRef.class));
    }

    private boolean isCollectionOf(Class<?> targetType) {
        return (Collection.class.isAssignableFrom(field.getType()) && ReflectionUtil.getTypeParameter(field.getGenericType(), 0) == targetType)
                || (Map.class.isAssignableFrom(field.getType()) && ReflectionUtil.getTypeParameter(field.getGenericType(), 1) == targetType);
    }

    @Override
    public U deserialize(EntityData.Value value) {
        return serializationHandler.deserialize(value);
    }

    @Override
    public void deserializeOnto(T target, EntityData.Value value) {
        U deserializedValue = deserialize(value);
        if (deserializedValue != null) {
            setValue(target, deserializedValue);
        }
    }

    @Override
    public ClassMetadata<T> getOwner() {
        return owner;
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Class<U> getType() {
        return type;
    }

    @Override
    public void setId(byte id) {
        this.id = id;
        owner.setFieldId(this, id);
    }

    @Override
    public byte getId() {
        return id;
    }

    @Override
    public U getValue(T obj) {
        try {
            if (getter != null) {
                return type.cast(getter.invoke(obj));
            }
            return type.cast(field.get(obj));
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.error("Exception during access of {} from {}", field.getName(), obj.getClass(), e);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public U getCopyOfValue(T obj) {
        return serializationHandler.copy(getValue(obj));
    }

    @Override
    public void setValue(T target, U value) {
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

    @Override
    public boolean isReplicated() {
        return replicated;
    }

    @Override
    public boolean isOwnedReference() {
        return ownedReference;
    }

    @Override
    public Replicate getReplicationInfo() {
        return replicationInfo;
    }

    /**
     * Serializes the given value, that was originally obtained from this field.
     * <p/>
     * This is provided for performance, to avoid obtaining the same value twice via reflection.
     *
     * @param rawValue The value to serialize
     * @return The serialized value
     */
    @Override
    @SuppressWarnings("unchecked")
    public EntityData.Value serializeValue(U rawValue) {
        return serializationHandler.serialize(rawValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public EntityData.Value serialize(T container) {
        U rawValue = getValue(container);
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
    @Override
    public EntityData.NameValue serializeNameValue(T container, boolean usingFieldIds) {
        U rawValue = getValue(container);
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

    private Method findGetter() {
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

    private Method findSetter() {
        String setterName = "set" + field.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) + field.getName().substring(1);
        Method result = findMethod(field.getDeclaringClass(), setterName, field.getType());
        if (result != null) {
            result.setAccessible(true);
        }
        return result;
    }

    private Method findMethod(Class<?> targetType, String methodName, Class<?>... parameters) {
        try {
            return targetType.getMethod(methodName, parameters);
        } catch (NoSuchMethodException me) {
            // We're expecting not to find methods
            return null;
        }
    }
}
