/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.persistence.typeHandling.coreTypes.factories;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.coreTypes.ObjectFieldMapTypeHandler;
import org.terasology.reflection.MappedContainer;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.DefaultClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.reflect.ReflectFactory;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;

public class ObjectFieldMapTypeHandlerFactory implements TypeHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectFieldMapTypeHandlerFactory.class);

    private ReflectFactory reflectFactory;
    private CopyStrategyLibrary copyStrategies;

    public ObjectFieldMapTypeHandlerFactory(ReflectFactory reflectFactory, CopyStrategyLibrary copyStrategies) {
        this.reflectFactory = reflectFactory;
        this.copyStrategies = copyStrategies;
    }

    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeSerializationLibrary typeSerializationLibrary) {
        Class<? super T> typeClass = typeInfo.getRawType();

        if (typeClass.getAnnotation(MappedContainer.class) != null
                && !Modifier.isAbstract(typeClass.getModifiers())
                && !typeClass.isLocalClass()
                && !(typeClass.isMemberClass()
                && !Modifier.isStatic(typeClass.getModifiers()))) {
            try {
                ClassMetadata<?, ?> metadata = new DefaultClassMetadata<>(new SimpleUri(), typeClass,
                        reflectFactory, copyStrategies);

                @SuppressWarnings({"unchecked"})
                ObjectFieldMapTypeHandler<T> mappedHandler = new ObjectFieldMapTypeHandler(typeClass,
                        getFieldHandlerMap(metadata, typeSerializationLibrary));

                return Optional.of(mappedHandler);
            } catch (NoSuchMethodException e) {
                LOGGER.error("Unable to register field of type {}: no publicly accessible default constructor",
                        typeClass.getSimpleName());
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private Map<FieldMetadata<?, ?>, TypeHandler> getFieldHandlerMap(ClassMetadata<?, ?> type, TypeSerializationLibrary typeSerializationLibrary) {
        Map<FieldMetadata<?, ?>, TypeHandler> handlerMap = Maps.newHashMap();
        for (FieldMetadata<?, ?> field : type.getFields()) {
            TypeHandler<?> handler = typeSerializationLibrary.getTypeHandler(field.getField().getGenericType());
            if (handler != null) {
                handlerMap.put(field, handler);
            } else {
                LOGGER.info("Unsupported field: '{}.{}'", type.getUri(), field.getName());
            }
        }
        return handlerMap;
    }
}
