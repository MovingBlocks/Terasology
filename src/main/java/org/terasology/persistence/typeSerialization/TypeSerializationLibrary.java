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

package org.terasology.persistence.typeSerialization;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.classMetadata.ClassMetadata;
import org.terasology.classMetadata.DefaultClassMetadata;
import org.terasology.classMetadata.FieldMetadata;
import org.terasology.classMetadata.MappedContainer;
import org.terasology.classMetadata.copying.CopyStrategyLibrary;
import org.terasology.classMetadata.reflect.ReflectFactory;
import org.terasology.persistence.typeSerialization.typeHandlers.TypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.core.BooleanTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.core.ByteTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.core.DoubleTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.core.EnumTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.core.FloatTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.core.IntTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.core.ListTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.core.LongTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.core.MappedContainerTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.core.NumberTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.core.SetTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.core.StringMapTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.core.StringTypeHandler;
import org.terasology.utilities.ReflectionUtil;
import org.terasology.engine.SimpleUri;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A library of type handlers. This is used for the construction of class metadata.
 * This library should be initialised by adding a number of base type handlers, describing how to serialize each supported type.
 * It will then produce serializers for classes (through their ClassMetadata) on request.
 *
 * @author Immortius
 */
public class TypeSerializationLibrary {
    private static final Logger logger = LoggerFactory.getLogger(TypeSerializationLibrary.class);

    private Map<Class<?>, TypeHandler<?>> typeHandlers = Maps.newHashMap();
    private Set<Class<?>> coreTypeHandlers = Sets.newHashSet();
    private ReflectFactory reflectFactory;
    private CopyStrategyLibrary copyStrategies;

    private Map<ClassMetadata<?, ?>, Serializer> serializerMap = Maps.newHashMap();

    /**
     * @param factory        The factory providing reflect implementation.
     * @param copyStrategies The provider of copy strategies
     */
    public TypeSerializationLibrary(ReflectFactory factory, CopyStrategyLibrary copyStrategies) {
        this.reflectFactory = factory;
        this.copyStrategies = copyStrategies;
        add(Boolean.class, new BooleanTypeHandler());
        add(Boolean.TYPE, new BooleanTypeHandler());
        add(Byte.class, new ByteTypeHandler());
        add(Byte.TYPE, new ByteTypeHandler());
        add(Double.class, new DoubleTypeHandler());
        add(Double.TYPE, new DoubleTypeHandler());
        add(Float.class, new FloatTypeHandler());
        add(Float.TYPE, new FloatTypeHandler());
        add(Integer.class, new IntTypeHandler());
        add(Integer.TYPE, new IntTypeHandler());
        add(Long.class, new LongTypeHandler());
        add(Long.TYPE, new LongTypeHandler());
        add(String.class, new StringTypeHandler());
        add(Number.class, new NumberTypeHandler());
    }

    /**
     * Creates a copy of an existing serialization library. This copy is initialised with all type handlers that were added to the original, but does not retain any
     * serializers or type handlers that were generated. This can be used to override specific types handlers from another type serializer.
     *
     * @param original The original type serialization library to copy.
     */
    public TypeSerializationLibrary(TypeSerializationLibrary original) {
        this.reflectFactory = original.reflectFactory;
        this.copyStrategies = original.copyStrategies;
        for (Class<?> type : original.coreTypeHandlers) {
            typeHandlers.put(type, original.typeHandlers.get(type));
            coreTypeHandlers.add(type);
        }
    }

    /**
     * Adds a type handler that will be to serialize a specified type.
     * If a type handler was previously registered for that type, it will be replaced with the new type handler.
     * Existing serializers will not be updated.
     *
     * @param type    The type to handle.
     * @param handler The TypeHandler
     * @param <T>     The type to handle.
     */
    public <T> void add(Class<T> type, TypeHandler<? super T> handler) {
        typeHandlers.put(type, handler);
        coreTypeHandlers.add(type);
    }

    /**
     * Obtains a serializer for the given type
     *
     * @param type The ClassMetadata for the type of interest
     * @return A serializer for serializing/deserializing the type
     */
    @SuppressWarnings("unchecked")
    public Serializer getSerializerFor(ClassMetadata<?, ?> type) {
        Serializer serializer = serializerMap.get(type);
        if (serializer == null) {
            Map<FieldMetadata<?, ?>, TypeHandler> fieldHandlerMap = getFieldHandlerMap(type);
            serializer = new Serializer(fieldHandlerMap);
            serializerMap.put(type, serializer);
        }
        return serializer;
    }

    // TODO: Refactor
    @SuppressWarnings("unchecked")
    private TypeHandler<?> getHandlerFor(Type genericType) {
        Class<?> typeClass = ReflectionUtil.getClassOfType(genericType);
        if (typeClass == null) {
            logger.error("Unabled to get class from type {}", genericType);
            return null;
        }

        if (Enum.class.isAssignableFrom(typeClass)) {
            return new EnumTypeHandler(typeClass);
        } else if (List.class.isAssignableFrom(typeClass)) {
            // For lists, createEntityRef the handler for the contained type and wrap in a list type handler
            Type parameter = ReflectionUtil.getTypeParameter(genericType, 0);
            if (parameter != null) {
                TypeHandler<?> innerHandler = getHandlerFor(parameter);
                if (innerHandler != null) {
                    return new ListTypeHandler<>(innerHandler);
                }
            }
            logger.error("List field is not parametrized, or holds unsupported type");
            return null;

        } else if (Set.class.isAssignableFrom(typeClass)) {
            // For sets:
            Type parameter = ReflectionUtil.getTypeParameter(genericType, 0);
            if (parameter != null) {
                TypeHandler<?> innerHandler = getHandlerFor(parameter);
                if (innerHandler != null) {
                    return new SetTypeHandler<>(innerHandler);
                }
            }
            logger.error("Set field is not parametrized, or holds unsupported type");
            return null;

        } else if (Map.class.isAssignableFrom(typeClass)) {
            // For Maps, createEntityRef the handler for the value type (and maybe key too?)
            Type keyParameter = ReflectionUtil.getTypeParameter(genericType, 0);
            Type contentsParameter = ReflectionUtil.getTypeParameter(genericType, 1);
            if (keyParameter != null && contentsParameter != null && String.class == keyParameter) {
                TypeHandler<?> valueHandler = getHandlerFor(contentsParameter);
                if (valueHandler != null) {
                    return new StringMapTypeHandler<>(valueHandler);
                }
            }
            logger.error("Map field is not parametrized, does not have a String key, or holds unsupported values");

        } else if (typeHandlers.containsKey(typeClass)) {
            // For know types, just use the handler
            return typeHandlers.get(typeClass);

        } else if (typeClass.getAnnotation(MappedContainer.class) != null
                && !Modifier.isAbstract(typeClass.getModifiers())
                && !typeClass.isLocalClass()
                && !(typeClass.isMemberClass()
                && !Modifier.isStatic(typeClass.getModifiers()))) {
            try {
                ClassMetadata<?, ?> metadata = new DefaultClassMetadata<>(new SimpleUri(), typeClass, reflectFactory, copyStrategies);
                MappedContainerTypeHandler<?> mappedHandler = new MappedContainerTypeHandler(typeClass, getFieldHandlerMap(metadata));
                typeHandlers.put(typeClass, mappedHandler);
                return mappedHandler;
            } catch (NoSuchMethodException e) {
                logger.error("Unable to register field of type {}: no publicly accessible default constructor", typeClass.getSimpleName());
                return null;
            }
        } else {
            logger.error("Unable to register field of type {}: not a supported type or MappedContainer", typeClass.getSimpleName());
        }

        return null;
    }

    private Map<FieldMetadata<?, ?>, TypeHandler> getFieldHandlerMap(ClassMetadata<?, ?> type) {
        Map<FieldMetadata<?, ?>, TypeHandler> handlerMap = Maps.newHashMap();
        for (FieldMetadata<?, ?> field : type.getFields()) {
            TypeHandler<?> handler = getHandlerFor(field.getField().getGenericType());
            if (handler != null) {
                handlerMap.put(field, handler);
            }
        }
        return handlerMap;
    }
}
