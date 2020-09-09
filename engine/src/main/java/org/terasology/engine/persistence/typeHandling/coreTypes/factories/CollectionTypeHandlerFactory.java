// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.coreTypes.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.persistence.typeHandling.TypeHandler;
import org.terasology.engine.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.engine.persistence.typeHandling.TypeHandlerContext;
import org.terasology.engine.persistence.typeHandling.coreTypes.CollectionTypeHandler;
import org.terasology.engine.persistence.typeHandling.coreTypes.RuntimeDelegatingTypeHandler;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.reflection.reflect.ObjectConstructor;
import org.terasology.engine.utilities.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;

/**
 * Creates type handlers for {@link Collection} types.
 */
public class CollectionTypeHandlerFactory implements TypeHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionTypeHandlerFactory.class);

    private final ConstructorLibrary constructorLibrary;

    public CollectionTypeHandlerFactory(ConstructorLibrary constructorLibrary) {
        this.constructorLibrary = constructorLibrary;
    }

    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeHandlerContext context) {
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

        Optional<TypeHandler<?>> declaredElementTypeHandler =
                context.getTypeHandlerLibrary().getTypeHandler(elementType);

        @SuppressWarnings({"unchecked"})
        TypeHandler<?> elementTypeHandler = new RuntimeDelegatingTypeHandler(
                declaredElementTypeHandler.orElse(null),
                elementTypeInfo,
                context
        );

        ObjectConstructor<T> collectionConstructor = constructorLibrary.get(typeInfo);

        @SuppressWarnings({"unchecked"})
        TypeHandler<T> typeHandler = new CollectionTypeHandler(elementTypeHandler, collectionConstructor);

        return Optional.of(typeHandler);
    }

}
