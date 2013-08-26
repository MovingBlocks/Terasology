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
package org.terasology.entitySystem.metadata.internal;

import com.google.common.base.Objects;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Owns;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.copying.CopyStrategy;
import org.terasology.entitySystem.metadata.reflect.FieldAccessor;
import org.terasology.entitySystem.metadata.reflect.ReflectFactory;
import org.terasology.network.NoReplicate;
import org.terasology.network.Replicate;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class FieldMetadataImpl<T, U> implements FieldMetadata<T, U> {

    private final ClassMetadataImpl<T> owner;
    private final Class<U> type;
    private final Field field;
    private final FieldAccessor<T, U> accessor;
    private final CopyStrategy<U> copyStrategy;

    private byte id;
    private boolean replicated;
    private Replicate replicationInfo;
    private boolean ownedReference;

    @SuppressWarnings("unchecked")
    public FieldMetadataImpl(ClassMetadataImpl<T> owner, Field field, CopyStrategy<U> copyStrategy, ReflectFactory factory, boolean replicatedByDefault) {
        this.owner = owner;
        this.copyStrategy = copyStrategy;
        this.type = (Class<U>) field.getType();
        this.accessor = factory.createFieldAccessor(owner.getType(), field, type);
        this.field = field;

        // TODO: Maybe move these into child classes.
        this.replicated = replicatedByDefault;
        if (field.getAnnotation(NoReplicate.class) != null) {
            replicated = false;
        }
        if (field.getAnnotation(Replicate.class) != null) {
            replicated = true;
        }
        this.replicationInfo = field.getAnnotation(Replicate.class);
        ownedReference = field.getAnnotation(Owns.class) != null && (EntityRef.class.isAssignableFrom(field.getType())
                || isCollectionOf(EntityRef.class, field.getGenericType()));
    }

    private boolean isCollectionOf(Class<?> targetType, Type genericType) {
        return (Collection.class.isAssignableFrom(type) && ReflectionUtil.getTypeParameter(genericType, 0).equals(targetType))
                || (Map.class.isAssignableFrom(type) && ReflectionUtil.getTypeParameter(genericType, 1).equals(targetType));
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
    public Field getField() {
        return field;
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
    @SuppressWarnings("unchecked")
    public U getValue(Object from) {
        return accessor.getValue((T) from);
    }

    @Override
    public U getValueChecked(T from) {
        return getValue(from);
    }

    @Override
    @SuppressWarnings("unchecked")
    public U getCopyOfValue(Object from) {
        return copyStrategy.copy(getValue(from));
    }

    @Override
    public U getCopyOfValueChecked(T from) {
        return getCopyOfValue(from);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setValue(Object target, Object value) {
        accessor.setValue((T) target, (U) value);
    }

    @Override
    public void setValueChecked(T target, U value) {
        accessor.setValue(target, value);
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

    @Override
    public String toString() {
        return field.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof FieldMetadataImpl) {
            FieldMetadataImpl other = (FieldMetadataImpl) obj;
            return Objects.equal(field, other.field);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(field);
    }


}
