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

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.metadata.reflected.ReflectedFieldMetadata;
import org.terasology.entitySystem.metadata.typeHandlers.core.EnumTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.core.ListTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.core.MappedContainerTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.core.SetTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.core.StringMapTypeHandler;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A library of type handlers. This is used for the construction of class metadata.
 *
 * @author Immortius
 */
public class TypeHandlerLibrary implements Iterable<Map.Entry<Class<?>, TypeHandler<?>>> {
    private static final Logger logger = LoggerFactory.getLogger(TypeHandlerLibrary.class);
    private static final int MAX_SERIALIZATION_DEPTH = 10;

    private Map<Class<?>, TypeHandler<?>> typeHandlers;

    /**
     * @param typeHandlers A map of type->TypeHandler to populate the library with
     */
    public TypeHandlerLibrary(Map<Class<?>, TypeHandler<?>> typeHandlers) {
        this.typeHandlers = ImmutableMap.copyOf(typeHandlers);
    }

    /**
     * Obtains the type handler for the given type.
     * @param forClass The class of the type to get a handler for
     * @param <T> The type to get a handler for
     * @return The type handler, or null if none is registered
     */
    @SuppressWarnings("unchecked")
    public <T> TypeHandler<? super T> getTypeHandler(Class<T> forClass) {
        return (TypeHandler<? super T>) typeHandlers.get(forClass);
    }

    @Override
    public Iterator<Map.Entry<Class<?>, TypeHandler<?>>> iterator() {
        return typeHandlers.entrySet().iterator();
    }

    /*public <T> ClassMetadata<T> build(Class<T> forClass, boolean replicateFieldsByDefault, String primaryName, String... additionalNames) {
        ClassMetadata<T> info;
        try {
            info = new ReflectedClassMetadata<T>(forClass, primaryName, additionalNames);
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register class {}: Default Constructor Required", forClass.getSimpleName(), e);
            return null;
        }

        populateFields(forClass, info, replicateFieldsByDefault);
        return info;
    }


    public <T> void populateFields(Class<T> forClass, ClassMetadata<T> info, boolean replicateFieldsByDefault) {
        for (Field field : Reflections.getAllFields(forClass, Predicates.alwaysTrue())) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            TypeHandler typeHandler = getTypeHandlerFor(field.getGenericType(), 0);
            if (typeHandler == null) {
                logger.error("Unsupported field type in component type {}, {} : {}", forClass.getSimpleName(), field.getName(), field.getGenericType());
            } else {
                info.addField(new ReflectedFieldMetadata(field, typeHandler, replicateFieldsByDefault));
            }
        }
    }*/

    public TypeHandler getTypeHandlerFor(Type type) {
        return getHandlerFor(type, 0);
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
        } else if (List.class.isAssignableFrom(typeClass)) {
            // For lists, createEntityRef the handler for the contained type and wrap in a list type handler
            Type parameter = ReflectionUtil.getTypeParameter(type, 0);
            if (parameter != null) {
                TypeHandler innerHandler = getHandlerFor(parameter, depth);
                if (innerHandler != null) {
                    return new ListTypeHandler(innerHandler);
                }
            }
            logger.error("List field is not parameterized, or holds unsupported type");
            return null;

        } else if (Set.class.isAssignableFrom(typeClass)) {
            // For sets:
            Type parameter = ReflectionUtil.getTypeParameter(type, 0);
            if (parameter != null) {
                TypeHandler innerHandler = getHandlerFor(parameter, depth);
                if (innerHandler != null) {
                    return new SetTypeHandler(innerHandler);
                }
            }
            logger.error("Set field is not parameterized, or holds unsupported type");
            return null;

        } else if (Map.class.isAssignableFrom(typeClass)) {
            // For Maps, createEntityRef the handler for the value type (and maybe key too?)
            Type keyParameter = ReflectionUtil.getTypeParameter(type, 0);
            Type contentsParameter = ReflectionUtil.getTypeParameter(type, 1);
            if (keyParameter != null && contentsParameter != null && String.class == keyParameter) {
                TypeHandler valueHandler = getHandlerFor(contentsParameter, depth);
                if (valueHandler != null) {
                    return new StringMapTypeHandler(valueHandler);
                }
            }
            logger.error("Map field is not parameterized, does not have a String key, or holds unsupported values");

        } else if (typeHandlers.containsKey(typeClass)) {
            // For know types, just use the handler
            return typeHandlers.get(typeClass);

        } else if (depth <= MAX_SERIALIZATION_DEPTH
                && !Modifier.isAbstract(typeClass.getModifiers())
                && !typeClass.isLocalClass()
                && !(typeClass.isMemberClass()
                && !Modifier.isStatic(typeClass.getModifiers()))) {
            // For unknown types annotated by MappedContainer annotation, map them
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
                if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);
                TypeHandler handler = getHandlerFor(field.getGenericType(), depth + 1);
                if (handler == null) {
                    logger.error("Unsupported field type in component type {}, {} : {}", typeClass.getSimpleName(), field.getName(), field.getGenericType());
                } else {
                    mappedHandler.addField(new ReflectedFieldMetadata(field, handler, false));
                }
            }
            return mappedHandler;
        }

        return null;
    }

}
