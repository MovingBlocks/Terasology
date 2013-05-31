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

import com.google.common.collect.Maps;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ClassMetadata<T> {
    private static final Logger logger = LoggerFactory.getLogger(ClassMetadata.class);

    private Map<String, FieldMetadata> fields = Maps.newHashMap();
    private Class<T> clazz;
    private Constructor<T> constructor;
    private String[] names;
    private TIntObjectMap<FieldMetadata> fieldsById = new TIntObjectHashMap<FieldMetadata>();

    public ClassMetadata(Class<T> simpleClass, String... names) throws NoSuchMethodException {
        this.clazz = simpleClass;
        this.names = Arrays.copyOf(names, names.length);
        constructor = simpleClass.getDeclaredConstructor();
        constructor.setAccessible(true);
    }

    public String[] getNames() {
        return Arrays.copyOf(names, names.length);
    }

    public String getName() {
        return names[0];
    }

    public Class<T> getType() {
        return clazz;
    }

    public void setFieldId(FieldMetadata field, byte id) {
        if (fields.containsValue(field)) {
            field.setId(id);
            fieldsById.put(id, field);
        }
    }

    public FieldMetadata getFieldById(int id) {
        return fieldsById.get(id);
    }

    public void addField(FieldMetadata fieldInfo) {
        fields.put(fieldInfo.getName().toLowerCase(Locale.ENGLISH), fieldInfo);
    }

    public FieldMetadata getField(String name) {
        return fields.get(name.toLowerCase(Locale.ENGLISH));
    }

    public Iterable<FieldMetadata> iterateFields() {
        return fields.values();
    }

    public T newInstance() {
        try {
            return constructor.newInstance();
        } catch (InstantiationException e) {
            logger.error("Exception instantiating type: {}", clazz, e);
        } catch (IllegalAccessException e) {
            logger.error("Exception instantiating type: {}", clazz, e);
        } catch (InvocationTargetException e) {
            logger.error("Exception instantiating type: {}", clazz, e);
        }
        return null;
    }

    public T clone(T component) {
        try {
            T result = constructor.newInstance();
            for (FieldMetadata field : fields.values()) {
                field.setValue(result, field.copy(field.getValue(component)));
            }
            return result;
        } catch (InstantiationException e) {
            logger.error("Exception during serializing type: {}", clazz, e);
        } catch (IllegalAccessException e) {
            logger.error("Exception during serializing type: {}", clazz, e);
        } catch (InvocationTargetException e) {
            logger.error("Exception during serializing type: {}", clazz, e);
        }
        return null;
    }

    public int size() {
        return fields.size();
    }

    @Override
    public String toString() {
        return getName();
    }
}
