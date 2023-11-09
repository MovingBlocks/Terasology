// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerContext;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.coreTypes.GenericMapTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.RuntimeDelegatingTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.StringMapTypeHandler;
import org.terasology.reflection.ReflectionUtil;
import org.terasology.reflection.TypeInfo;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

public class MapTypeHandlerFactory implements TypeHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringMapTypeHandler.class);

    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeHandlerContext context) {
        if (!Map.class.isAssignableFrom(typeInfo.getRawType())) {
            return Optional.empty();
        }

        Type keyType = ReflectionUtil.getTypeParameterForSuper(typeInfo.getType(), Map.class, 0);
        Type valueType = ReflectionUtil.getTypeParameterForSuper(typeInfo.getType(), Map.class, 1);


        if (valueType == null) {
            LOGGER.error("Map is not parameterized and cannot be serialized");
            return Optional.empty();
        }

        TypeHandler<?> valueTypeHandler = new RuntimeDelegatingTypeHandler<>(
                context.getTypeHandlerLibrary().<T>getTypeHandler(valueType).orElse(null),
                TypeInfo.of(valueType),
                context
        );

        TypeHandler<T> result;
        if (String.class.equals(keyType)) {
            //noinspection unchecked
            result = (TypeHandler<T>) new StringMapTypeHandler<>(valueTypeHandler);
        } else {
            TypeHandler<?> keyTypeHandler = new RuntimeDelegatingTypeHandler<>(
                    context.getTypeHandlerLibrary().getTypeHandler(keyType).orElse(null),
                    TypeInfo.of(keyType),
                    context
            );
            //noinspection unchecked
            result = (TypeHandler<T>) new GenericMapTypeHandler<>(keyTypeHandler, valueTypeHandler);
        }
        return Optional.of(result);
    }
}
