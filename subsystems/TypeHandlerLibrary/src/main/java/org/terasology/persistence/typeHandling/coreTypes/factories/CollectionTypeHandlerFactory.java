// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerContext;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.coreTypes.CollectionTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.RuntimeDelegatingTypeHandler;
import org.terasology.reflection.ReflectionUtil;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;

/**
 * Creates type handlers for {@link Collection} types.
 */
public class CollectionTypeHandlerFactory implements TypeHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionTypeHandlerFactory.class);

    public CollectionTypeHandlerFactory() { }

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

        var declaredElementTypeHandler =
                context.getTypeHandlerLibrary().getTypeHandler(elementType);

        var elementTypeHandler = new RuntimeDelegatingTypeHandler<>(
                declaredElementTypeHandler.orElse(null),
                TypeInfo.of(elementType),
                context
        );

        var constructor = ConstructorLibrary.getCollectionCopyConstructor((TypeInfo) typeInfo);

        TypeHandler<T> typeHandler = new CollectionTypeHandler<>(elementTypeHandler, constructor);

        return Optional.of(typeHandler);
    }

}
