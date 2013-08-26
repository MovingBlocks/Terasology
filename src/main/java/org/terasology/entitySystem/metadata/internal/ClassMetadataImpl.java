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
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.entitySystem.metadata.copying.CopyStrategy;
import org.terasology.entitySystem.metadata.copying.CopyStrategyLibrary;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.reflect.ObjectConstructor;
import org.terasology.entitySystem.metadata.reflect.ReflectFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ClassMetadataImpl<T> implements ClassMetadata<T> {
    private static final Logger logger = LoggerFactory.getLogger(ClassMetadataImpl.class);

    private final Class<T> clazz;
    private final ObjectConstructor<T> constructor;
    private final String name;
    private Map<String, FieldMetadata<T, ?>> fields = Maps.newHashMap();
    private TIntObjectMap<FieldMetadata<T, ?>> fieldsById = new TIntObjectHashMap<>();

    public ClassMetadataImpl(Class<T> simpleClass, CopyStrategyLibrary copyStrategyLibrary, ReflectFactory factory, String name)
            throws NoSuchMethodException {
        checkNotNull(name);

        this.clazz = simpleClass;
        this.name = name;
        this.constructor = factory.createConstructor(simpleClass);

        addFields(copyStrategyLibrary, factory);
    }

    protected void addFields(CopyStrategyLibrary copyStrategyLibrary, ReflectFactory factory) {
        for (Field field : Reflections.getAllFields(clazz, Predicates.alwaysTrue())) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            CopyStrategy<?> copyStrategy = copyStrategyLibrary.getStrategy(field.getGenericType());
            FieldMetadata<T, ?> metadata = createField(field, copyStrategy, factory);
            fields.put(metadata.getName().toLowerCase(Locale.ENGLISH), metadata);
        }
    }

    @SuppressWarnings("unchecked")
    protected <U> FieldMetadata<T, U> createField(Field field, CopyStrategy<U> copyStrategy, ReflectFactory factory) {
        return new FieldMetadataImpl<>(this, field, copyStrategy, factory, false);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<T> getType() {
        return clazz;
    }

    @Override
    public FieldMetadata<T, ?> getField(int id) {
        return fieldsById.get(id);
    }

    @Override
    public FieldMetadata<T, ?> getField(String fieldName) {
        return fields.get(fieldName.toLowerCase(Locale.ENGLISH));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> FieldMetadata<T, U> getField(String fieldName, Class<U> fieldType) {
        FieldMetadata<T, ?> metadata = fields.get(fieldName.toLowerCase(Locale.ENGLISH));
        if (fieldType.isAssignableFrom(metadata.getType())) {
            return (FieldMetadata<T, U>) metadata;
        }
        return null;
    }

    @Override
    public Collection<FieldMetadata<T, ?>> getFields() {
        return ImmutableList.copyOf(fields.values());
    }

    @Override
    public T newInstance() {
        return constructor.construct();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T copy(T object) {
        T result = constructor.construct();
        if (result != null) {
            for (FieldMetadata field : fields.values()) {
                field.setValue(result, field.getCopyOfValue(object));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T copyRaw(Object object) {
        T result = constructor.construct();
        if (result != null) {
            for (FieldMetadata field : fields.values()) {
                field.setValue(result, field.getCopyOfValue(object));
            }
        }
        return result;
    }

    @Override
    public int getFieldCount() {
        return fields.size();
    }

    @Override
    public String toString() {
        return getName();
    }

    void setFieldId(FieldMetadata<T, ?> field, byte id) {
        if (fields.containsValue(field)) {
            fieldsById.put(id, field);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ClassMetadataImpl) {
            ClassMetadataImpl other = (ClassMetadataImpl) obj;
            return Objects.equal(other.clazz, clazz);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return clazz.hashCode();
    }
}
