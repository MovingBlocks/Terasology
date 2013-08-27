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
package org.terasology.classMetadata;

import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.reflections.Reflections;
import org.terasology.classMetadata.copying.CopyStrategy;
import org.terasology.classMetadata.copying.CopyStrategyLibrary;
import org.terasology.classMetadata.reflect.ObjectConstructor;
import org.terasology.classMetadata.reflect.ReflectFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class Metadata provides information on a class and its fields, and the ability to create, copy or manipulate an instance of the class.
 * <p/>
 * Subclasses can be created to hold additional information for specific types of objects.  These may override createField()
 * to change how fields are processed and possibly switch to a subtype of FieldMetadata that holds additional information.
 * <p/>
 * Consumed classes are required to have a default constructor (this may be private)
 *
 * @author Immortius
 */
public abstract class ClassMetadata<T, U extends FieldMetadata<T, ?>> {

    private final Class<T> clazz;
    private final ObjectConstructor<T> constructor;
    private final String name;
    private Map<String, U> fields = Maps.newHashMap();
    private TIntObjectMap<U> fieldsById = new TIntObjectHashMap<>();

    /**
     * Creates a class metatdata
     *
     * @param type                The type to create the metadata for
     * @param factory             A reflection library to provide class construction and field get/set functionality
     * @param copyStrategyLibrary A copy strategy library
     * @param name                The name to identify the class with.
     * @throws NoSuchMethodException If the class has no default constructor
     */
    public ClassMetadata(Class<T> type, ReflectFactory factory, CopyStrategyLibrary copyStrategyLibrary, String name)
            throws NoSuchMethodException {
        checkNotNull(name);

        this.clazz = type;
        this.name = name;
        this.constructor = factory.createConstructor(type);

        addFields(copyStrategyLibrary, factory);
    }

    /**
     * Scans the class this metadata describes, adding all fields to the class' metadata.
     *
     * @param copyStrategyLibrary The library of copy strategies
     * @param factory             The reflection provider
     */
    private void addFields(CopyStrategyLibrary copyStrategyLibrary, ReflectFactory factory) {
        for (Field field : Reflections.getAllFields(clazz, Predicates.alwaysTrue())) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            CopyStrategy<?> copyStrategy = copyStrategyLibrary.getStrategy(field.getGenericType());
            U metadata = createField(field, copyStrategy, factory);
            if (metadata != null) {
                fields.put(metadata.getName().toLowerCase(Locale.ENGLISH), metadata);
            }
        }
    }

    /**
     * Creates the FieldMetadata describing a field
     *
     * @param field        The field to create metadata for
     * @param copyStrategy The copy strategy library
     * @param factory      The reflection provider
     * @return A FieldMetadata describing the field, or null to ignore this field
     */
    protected abstract <V> U createField(Field field, CopyStrategy<V> copyStrategy, ReflectFactory factory);

    /**
     * @return The name that identifies this class
     */
    public String getName() {
        return name;
    }

    /**
     * @return The class described by this metadata
     */
    public Class<T> getType() {
        return clazz;
    }

    /**
     * @param id The previously set id of the field
     * @return The field identified by the given id, or null if there is no such field
     */
    public U getField(int id) {
        return fieldsById.get(id);
    }

    /**
     * @param fieldName The name of the field
     * @return The field identified by the given name, or null if there is no such field
     */
    public U getField(String fieldName) {
        return fields.get(fieldName.toLowerCase(Locale.ENGLISH));
    }

    /**
     * @return The fields that this class has.
     */
    public Collection<U> getFields() {
        return ImmutableList.copyOf(fields.values());
    }

    /**
     * @return A new instance of this class.
     */
    public T newInstance() {
        return constructor.construct();
    }

    /**
     * @param object The instance of this class to copy
     * @return A copy of the given object
     */
    public T copy(T object) {
        T result = constructor.construct();
        if (result != null) {
            for (FieldMetadata field : fields.values()) {
                field.setValue(result, field.getCopyOfValue(object));
            }
        }
        return result;
    }

    /**
     * This method is for use in situations where metadata is being used generically and the actual type of the value cannot be
     *
     * @param object The instance of this class to copy
     * @return A copy of the given object, or null if object is not of the type described by this metadata.
     */
    @SuppressWarnings("unchecked")
    public T copyRaw(Object object) {
        if (getType().isInstance(object)) {
            return copy(getType().cast(object));
        }
        return null;
    }

    /**
     * @return The number of fields this class has
     */
    public int getFieldCount() {
        return fields.size();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ClassMetadata) {
            ClassMetadata other = (ClassMetadata) obj;
            return Objects.equal(other.clazz, clazz);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return clazz.hashCode();
    }

    /**
     * Used by FieldMetadata to update the id lookup table
     *
     * @param field The field to update the id for
     * @param id    The new id of the field
     */
    @SuppressWarnings("unchecked")
    void setFieldId(FieldMetadata<T, ?> field, byte id) {
        if (fields.containsValue((U) field)) {
            fieldsById.put(id, (U) field);
        }
    }

}
