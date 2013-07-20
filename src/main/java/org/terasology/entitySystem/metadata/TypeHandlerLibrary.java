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

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.metadata.core.EnumTypeHandler;
import org.terasology.entitySystem.metadata.core.ListTypeHandler;
import org.terasology.entitySystem.metadata.core.MappedContainerTypeHandler;
import org.terasology.entitySystem.metadata.core.SetTypeHandler;
import org.terasology.entitySystem.metadata.core.StringMapTypeHandler;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
public class TypeHandlerLibrary implements Iterable<Map.Entry<Class<?>, TypeHandler<?>>> {
    private static final Logger logger = LoggerFactory.getLogger(TypeHandlerLibrary.class);
    private static final int MAX_SERIALIZATION_DEPTH = 10;

    private Map<Class<?>, TypeHandler<?>> typeHandlers;

    public TypeHandlerLibrary(Map<Class<?>, TypeHandler<?>> typeHandlers) {
        this.typeHandlers = ImmutableMap.copyOf(typeHandlers);
    }

    @SuppressWarnings("unchecked")
    public <T> TypeHandler<? super T> getTypeHandler(Class<T> forClass) {
        return (TypeHandler<? super T>) typeHandlers.get(forClass);
    }

    public <T> ClassMetadata<T> build(Class<T> forClass, boolean replicateFieldsByDefault, String... names) {
        ClassMetadata<T> info;
        try {
            info = new ClassMetadata<T>(forClass, names);
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register class {}: Default Constructor Required", forClass.getSimpleName(), e);
            return null;
        }

        populateFields(forClass, info, replicateFieldsByDefault);
        return info;
    }


    public <T> void populateFields(Class<T> forClass, ClassMetadata<T> info, boolean replicateFieldsByDefault) {
        for (Field field : Reflections.getAllFields(forClass, Predicates.alwaysTrue())) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
                continue;
            field.setAccessible(true);
            TypeHandler typeHandler = getHandlerFor(field.getGenericType(), 0);
            if (typeHandler == null) {
                logger.error("Unsupported field type in component type {}, {} : {}", forClass.getSimpleName(), field.getName(), field.getGenericType());
            } else {

                info.addField(new FieldMetadata(field, typeHandler, replicateFieldsByDefault));
            }
        }
    }

    // TODO: Refactor
    private TypeHandler getHandlerFor(Type type, int depth) {
        Class<?> typeClass = ReflectionUtil.getClassOfType(type);
        if (typeClass == null) {
            logger.error("Cannot obtain class for type {}", type);
            return null;
        }

        if (Enum.class.isAssignableFrom(typeClass)) {
            return new EnumTypeHandler(typeClass);
        }
        // For lists, createEntityRef the handler for the contained type and wrap in a list type handler
        else if (List.class.isAssignableFrom(typeClass)) {
            Type parameter = ReflectionUtil.getTypeParameter(type, 0);
            if (parameter != null) {
                TypeHandler innerHandler = getHandlerFor(parameter, depth);
                if (innerHandler != null) {
                    return new ListTypeHandler(innerHandler);
                }
            }
            logger.error("List field is not parameterized, or holds unsupported type");
            return null;
        }
        // For sets:
        else if (Set.class.isAssignableFrom(typeClass)) {
            Type parameter = ReflectionUtil.getTypeParameter(type, 0);
            if (parameter != null) {
                TypeHandler innerHandler = getHandlerFor(parameter, depth);
                if (innerHandler != null) {
                    return new SetTypeHandler(innerHandler);
                }
            }
            logger.error("Set field is not parameterized, or holds unsupported type");
            return null;
        }
        // For Maps, createEntityRef the handler for the value type (and maybe key too?)
        else if (Map.class.isAssignableFrom(typeClass)) {
            Type keyParameter = ReflectionUtil.getTypeParameter(type, 0);
            Type contentsParameter = ReflectionUtil.getTypeParameter(type, 1);
            if (keyParameter != null && contentsParameter != null && String.class == keyParameter) {
                TypeHandler valueHandler = getHandlerFor(contentsParameter, depth);
                if (valueHandler != null) {
                    return new StringMapTypeHandler(valueHandler);
                }
            }
            logger.error("Map field is not parameterized, does not have a String key, or holds unsupported values");
        }
        // For know types, just use the handler
        else if (typeHandlers.containsKey(typeClass)) {
            return typeHandlers.get(typeClass);
        }
        // For unknown types annotated by MappedContainer annotation, map them
        else if (depth <= MAX_SERIALIZATION_DEPTH && !Modifier.isAbstract(typeClass.getModifiers()) && !typeClass.isLocalClass() && !(typeClass.isMemberClass() && !Modifier.isStatic(typeClass.getModifiers()))) {
            if (typeClass.getAnnotation(MappedContainer.class) == null) {
                logger.error("Unable to register field of type {}: not a supported type or MappedContainer", typeClass.getSimpleName());
                return null;
            }
            try {
                // Check if constructor exists
                typeClass.getConstructor();
            } catch (NoSuchMethodException e) {
                logger.error("Unable to register field of type {}: no publicly accessible default constructor", typeClass.getSimpleName());
                return null;
            }

            MappedContainerTypeHandler mappedHandler = new MappedContainerTypeHandler(typeClass);
            for (Field field : typeClass.getDeclaredFields()) {
                if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
                    continue;

                field.setAccessible(true);
                TypeHandler handler = getHandlerFor(field.getGenericType(), depth + 1);
                if (handler == null) {
                    logger.error("Unsupported field type in component type {}, {} : {}", typeClass.getSimpleName(), field.getName(), field.getGenericType());
                } else {
                    mappedHandler.addField(new FieldMetadata(field, handler, false));
                }
            }
            return mappedHandler;
        }

        return null;
    }

    @Override
    public Iterator<Map.Entry<Class<?>, TypeHandler<?>>> iterator() {
        return typeHandlers.entrySet().iterator();
    }
}
