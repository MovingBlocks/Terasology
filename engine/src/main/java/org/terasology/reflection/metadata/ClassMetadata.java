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
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.InaccessibleFieldException;
import org.terasology.reflection.reflect.ObjectConstructor;
import org.terasology.reflection.reflect.ReflectFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.Permission;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * Class Metadata provides information on a class and its fields, and the ability to create, copy or manipulate an instance of the class.
 * <br><br>
 * Subclasses can be created to hold additional information for specific types of objects.  These may override createField()
 * to change how fields are processed and possibly switch to a subtype of FieldMetadata that holds additional information.
 * <br><br>
 * Consumed classes are required to have a default constructor (this may be private)
 *
 */
public abstract class ClassMetadata<T, FIELD extends FieldMetadata<T, ?>> {

    private static final Logger logger = LoggerFactory.getLogger(ClassMetadata.class);
    private static final Permission CREATE_CLASS_METADATA = new RuntimePermission("createClassMetadata");

    private final SimpleUri uri;
    private final Class<T> clazz;
    private final ObjectConstructor<T> constructor;
    private Map<String, FIELD> fields = Maps.newHashMap();
    private TIntObjectMap<FIELD> fieldsById = new TIntObjectHashMap<>();

    /**
     * Creates a class metatdata
     *
     * @param uri                 The uri that identifies this type
     * @param type                The type to create the metadata for
     * @param factory             A reflection library to provide class construction and field get/set functionality
     * @param copyStrategyLibrary A copy strategy library
     * @throws NoSuchMethodException If the class has no default constructor
     */
    public ClassMetadata(SimpleUri uri, Class<T> type, ReflectFactory factory, CopyStrategyLibrary copyStrategyLibrary, Predicate<Field> includedFieldPredicate)
            throws NoSuchMethodException {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(CREATE_CLASS_METADATA);
        }

        this.uri = uri;
        this.clazz = type;
        if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
            this.constructor = factory.createConstructor(type);
        } else {
            this.constructor = null;
        }

        addFields(copyStrategyLibrary, factory, includedFieldPredicate);
    }

    public final SimpleUri getUri() {
        return uri;
    }

    /**
     * Scans the class this metadata describes, adding all fields to the class' metadata.
     *
     * @param copyStrategyLibrary The library of copy strategies
     * @param factory             The reflection provider
     */
    private void addFields(CopyStrategyLibrary copyStrategyLibrary, ReflectFactory factory, Predicate<Field> includedFieldsPredicate) {
        for (Field field : ReflectionUtils.getAllFields(clazz, includedFieldsPredicate)) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            CopyStrategy<?> copyStrategy = copyStrategyLibrary.getStrategy(field.getGenericType());

            try {
                FIELD metadata = createField(field, copyStrategy, factory);
                if (metadata != null) {
                    fields.put(metadata.getName().toLowerCase(Locale.ENGLISH), metadata);
                }
            } catch (InaccessibleFieldException e) {
                logger.error("Could not create metadata for field '{}' of type '{}', may be private.'", field, clazz);
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
    protected abstract <V> FIELD createField(Field field, CopyStrategy<V> copyStrategy, ReflectFactory factory) throws InaccessibleFieldException;

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
    public FIELD getField(int id) {
        return fieldsById.get(id);
    }

    /**
     * @param fieldName The name of the field
     * @return The field identified by the given name, or null if there is no such field
     */
    public FIELD getField(String fieldName) {
        return fields.get(fieldName.toLowerCase(Locale.ENGLISH));
    }

    /**
     * @return The fields that this class has.
     */
    public Collection<FIELD> getFields() {
        return ImmutableList.copyOf(fields.values());
    }

    public boolean isConstructable() {
        return constructor != null;
    }

    /**
     * @return A new instance of this class.
     */
    public T newInstance() {
        Preconditions.checkState(isConstructable(), "Cannot construct '" + this + "' - no accessible default constructor");
        return constructor.construct();
    }

    /**
     * @param object The instance of this class to copy
     * @return A copy of the given object
     */
    public T copy(T object) {
        T result = constructor.construct();
        if (result != null) {
            for (FIELD field : fields.values()) {
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
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ClassMetadata) {
            ClassMetadata<?, ?> other = (ClassMetadata<?, ?>) obj;
            return Objects.equal(other.clazz, clazz);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return clazz.hashCode();
    }

    @Override
    public String toString() {
        if (uri.isValid()) {
            return uri.toString();
        }
        return getType().toString();
    }

    /**
     * Used by FieldMetadata to update the id lookup table
     *
     * @param field The field to update the id for
     * @param id    The new id of the field
     */
    @SuppressWarnings("unchecked")
    void setFieldId(FieldMetadata<T, ?> field, byte id) {
        if (fields.containsValue(field)) {
            fieldsById.put(id, (FIELD) field);
        }
    }


}
