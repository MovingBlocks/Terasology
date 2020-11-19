// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes.factories;

import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerContext;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.coreTypes.ArrayTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.RuntimeDelegatingTypeHandler;
import org.terasology.reflection.TypeInfo;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Optional;


/**
 * Creates type handlers for arrays.
 */
public class ArrayTypeHandlerFactory implements TypeHandlerFactory {
    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeHandlerContext context) {
        Type type = typeInfo.getType();

        if (!(type instanceof GenericArrayType || type instanceof Class && ((Class<?>) type).isArray())) {
            return Optional.empty();
        }

        Type elementType = type instanceof GenericArrayType
                ? ((GenericArrayType) type).getGenericComponentType()
                : ((Class<?>) type).getComponentType();

        TypeInfo<?> elementTypeInfo = TypeInfo.of(elementType);

        Optional<TypeHandler<?>> declaredElementTypeHandler =
                context.getTypeHandlerLibrary().getTypeHandler(elementType);

        @SuppressWarnings({"unchecked"})
        TypeHandler<?> elementTypeHandler = new RuntimeDelegatingTypeHandler(
                declaredElementTypeHandler.orElse(null),
                elementTypeInfo,
                context
        );

        @SuppressWarnings({"unchecked"})
        TypeHandler<T> typeHandler = new ArrayTypeHandler(elementTypeHandler, elementTypeInfo);

        return Optional.of(typeHandler);
    }
}
