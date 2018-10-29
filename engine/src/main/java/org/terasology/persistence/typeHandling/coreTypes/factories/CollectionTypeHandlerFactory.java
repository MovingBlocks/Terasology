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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.coreTypes.CollectionTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.RuntimeDelegatingTypeHandler;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.reflection.reflect.ObjectConstructor;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;

/**
 * Creates type handlers for {@link Collection} types.
 */
public class CollectionTypeHandlerFactory implements TypeHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionTypeHandlerFactory.class);

    private ConstructorLibrary constructorLibrary;

    public CollectionTypeHandlerFactory(ConstructorLibrary constructorLibrary) {
        this.constructorLibrary = constructorLibrary;
    }

    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeSerializationLibrary typeSerializationLibrary) {
        Class<? super T> rawType = typeInfo.getRawType();

        if (!Collection.class.isAssignableFrom(rawType)) {
            return Optional.empty();
        }

        Type elementType = ReflectionUtil.getTypeParameterForSuper(typeInfo.getType(), Collection.class, 0);

        if (elementType == null) {
            LOGGER.error("Collection is not parameterized and cannot be serialized");
            return Optional.empty();
        }

        TypeInfo<?> elementTypeInfo = TypeInfo.of(elementType);

        Optional<TypeHandler<?>> declaredElementTypeHandler = typeSerializationLibrary.getTypeHandler(elementType);

        @SuppressWarnings({"unchecked"})
        TypeHandler<?> elementTypeHandler = new RuntimeDelegatingTypeHandler(
                declaredElementTypeHandler.orElse(null),
                elementTypeInfo,
                typeSerializationLibrary
        );

        ObjectConstructor<T> collectionConstructor = constructorLibrary.get(typeInfo);

        @SuppressWarnings({"unchecked"})
        TypeHandler<T> typeHandler = new CollectionTypeHandler(elementTypeHandler, collectionConstructor);

        return Optional.of(typeHandler);
    }
}
