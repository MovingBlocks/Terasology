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
package org.terasology.reflection.metadata;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.reflect.FieldAccessor;
import org.terasology.reflection.reflect.InaccessibleFieldException;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Provides information on a field, and the ability to set and get that field.
 *
 * @param <T> The type of the object this field belongs to
 * @param <U> The type of the field itself
 */
public class FieldMetadata<T, U> {

    private final ClassMetadata<T, ?> owner;
    private final Class<U> type;
    private final Field field;
    private final FieldAccessor<T, U> accessor;
    private final CopyStrategy<U> copyStrategy;

    private final String serializationName;

    private byte id;

    /**
     * @param owner        The ClassMetadata that owns this field
     * @param field        The field this metadata is for
     * @param copyStrategy The CopyStrategy appropriate for the type of the field
     * @param factory      The reflection provider
     */
    @SuppressWarnings("unchecked")
    public FieldMetadata(ClassMetadata<T, ?> owner, Field field, CopyStrategy<U> copyStrategy, ReflectFactory factory) throws InaccessibleFieldException {
        this.owner = owner;
        this.copyStrategy = copyStrategy;
        this.type = (Class<U>) determineType(field, owner.getType());
        this.accessor = factory.createFieldAccessor(owner.getType(), field, type);
        this.field = field;
        SerializedName name = field.getAnnotation(SerializedName.class);
        if (name != null) {
            serializationName = name.value();
        } else {
            serializationName = field.getName();
        }

    }

    private static Class<?> determineType(Field field, Class<?> ownerType) {
        Method getter = ReflectionUtil.findGetter(field.getName(), ownerType);
        if (getter != null && getter.getReturnType() != null) {
            if (ReflectionUtil.findSetter(field.getName(), ownerType, getter.getReturnType()) != null) {
                return getter.getReturnType();
            }
        }
        return field.getType();
    }

    /**
     * @return The class that owns this field
     */
    public ClassMetadata<T, ?> getOwner() {
        return owner;
    }

    /**
     * @return The name of the field
     */
    public String getName() {
        return field.getName();
    }

    /**
     * @return The serialization name of the field
     */
    public String getSerializationName() {
        return serializationName;
    }

    /**
     * @return The underlying java field
     */
    public Field getField() {
        return field;
    }

    /**
     * @return The type of the field
     */
    public Class<U> getType() {
        return type;
    }

    /**
     * @return The assigned id for this field, if any
     */
    public byte getId() {
        return id;
    }

    /**
     * @param id The id to assign for this field
     */
    public void setId(byte id) {
        this.id = id;
        owner.setFieldId(this, id);

    }

    /**
     * Obtains the value of the field from a object which is an instance of the owning type.
     *
     * @param from The object to obtain the value of this field from
     * @return The value of the field
     */
    @SuppressWarnings("unchecked")
    public U getValue(Object from) {
        return accessor.getValue((T) from);
    }

    /**
     * Obtains the value of the field from a object which is an instance of the owning type.
     * This method is checked to conform to the generic parameters of the FieldMetadata
     *
     * @param from The object to obtain the value of this field from
     * @return The value of the field
     */
    public U getValueChecked(T from) {
        return getValue(from);
    }

    /**
     * For types that need to be copied (e.g. Vector3f) for safe usage, this method will create a new copy of a field from an object.
     * Otherwise it behaves the same as getValue
     *
     * @param from The object to copy the field from
     * @return A safe to use copy of the value of this field in the given object
     */
    public U getCopyOfValue(Object from) {
        return copyStrategy.copy(getValue(from));
    }

    /**
     * For types that need to be copied (e.g. Vector3f) for safe usage, this method will create a new copy of a field from an object.
     * Otherwise it behaves the same as getValue
     * This method is checked to conform to the generic parameters of the FieldMetadata
     *
     * @param from The object to copy the field from
     * @return A safe to use copy of the value of this field in the given object
     */
    public U getCopyOfValueChecked(T from) {
        return getCopyOfValue(from);
    }

    /**
     * Sets the value of this field in a target object
     *
     * @param target The object to set the field of
     * @param value  The value to set the field to
     */
    @SuppressWarnings("unchecked")
    public void setValue(Object target, Object value) {
        accessor.setValue((T) target, (U) value);
    }

    /**
     * Sets the value of this field in a target object
     * This method is checked to conform to the generic parameters of the FieldMetadata
     *
     * @param target The object to set the field of
     * @param value  The value to set the field to
     */
    public void setValueChecked(T target, U value) {
        accessor.setValue(target, value);
    }

    @Override
    public String toString() {
        return field.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof FieldMetadata) {
            FieldMetadata<?, ?> other = (FieldMetadata<?, ?>) obj;
            return Objects.equal(field, other.field);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(field);
    }
}
