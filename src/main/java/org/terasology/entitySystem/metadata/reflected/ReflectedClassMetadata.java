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

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.entitySystem.metadata.TypeHandlerLibrary;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Immortius <immortius@gmail.com>
 */
public abstract class ReflectedClassMetadata<T> implements ClassMetadata<T> {
    private static final Logger logger = LoggerFactory.getLogger(ReflectedClassMetadata.class);

    private Map<String, FieldMetadata<T, ?>> fields = Maps.newHashMap();
    private Class<T> clazz;
    private Constructor<T> constructor;
    private final String primaryName;
    private final ImmutableSet<String> names;
    private TIntObjectMap<FieldMetadata<T, ?>> fieldsById = new TIntObjectHashMap<>();

    public ReflectedClassMetadata(Class<T> simpleClass, TypeHandlerLibrary typeHandlers, String name, String... alternateNames) throws NoSuchMethodException {
        checkNotNull(name);

        this.clazz = simpleClass;
        this.primaryName = name;
        this.names = ImmutableSet.<String>builder().add(name).addAll(Arrays.asList(alternateNames)).build();
        constructor = simpleClass.getDeclaredConstructor();
        constructor.setAccessible(true);

        addFields(typeHandlers);
    }

    protected final void addFields(TypeHandlerLibrary typeHandlers) {
        for (Field field : Reflections.getAllFields(clazz, Predicates.alwaysTrue())) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            TypeHandler typeHandler = typeHandlers.getTypeHandlerFor(field.getGenericType());
            if (typeHandler == null) {
                logger.error("Unsupported field type in component type {}, {} : {}", clazz.getSimpleName(), field.getName(), field.getGenericType());
            } else {
                addField(field, typeHandler);
            }
        }
    }

    protected abstract FieldMetadata<T, ?> addField(Field field, TypeHandler typeHandler);

    @Override
    public Collection<String> getNames() {
        return names;
    }

    @Override
    public String getName() {
        return primaryName;
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
    public FieldMetadata<T, ?> getField(String name) {
        return fields.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public Collection<FieldMetadata<T, ?>> getFields() {
        return ImmutableList.copyOf(fields.values());
    }

    @Override
    public T newInstance() {
        try {
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Exception instantiating type: {}", clazz, e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T copy(T object) {
        try {
            T result = constructor.newInstance();
            // The same field is used to copy and set the value, so the type of the object is the same.
            // So we ignore the unchecked warning
            for (FieldMetadata field : fields.values()) {
                field.setValue(result, field.getCopyOfValue(object));
            }
            return result;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Exception during serializing type: {}", clazz, e);
        }
        return null;
    }

    @Override
    public int getFieldCount() {
        return fields.size();
    }

    @Override
    public String toString() {
        return getName();
    }

    void addField(FieldMetadata<T, ?> fieldInfo) {
        fields.put(fieldInfo.getName().toLowerCase(Locale.ENGLISH), fieldInfo);
    }

    void setFieldId(FieldMetadata<T, ?> field, byte id) {
        if (fields.containsValue(field)) {
            fieldsById.put(id, field);
        }
    }
}
