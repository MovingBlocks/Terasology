// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerContext;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.coreTypes.RuntimeDelegatingTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.StringMapTypeHandler;
import org.terasology.reflection.ReflectionUtil;
import org.terasology.reflection.TypeInfo;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

public class StringMapTypeHandlerFactory implements TypeHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringMapTypeHandler.class);

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeHandlerContext context) {
        if (!Map.class.isAssignableFrom(typeInfo.getRawType())) {
            return Optional.empty();
        }

        Type keyType = ReflectionUtil.getTypeParameterForSuper(typeInfo.getType(), Map.class, 0);
        Type valueType = ReflectionUtil.getTypeParameterForSuper(typeInfo.getType(), Map.class, 1);

        if (!String.class.equals(keyType)) {
            return Optional.empty();
        }

        if (valueType == null) {
            LOGGER.error("Map is not parameterized and cannot be serialized");
            return Optional.empty();
        }

        Optional<TypeHandler<?>> declaredValueTypeHandler =
                context.getTypeHandlerLibrary().getTypeHandler(valueType);

        TypeInfo<?> valueTypeInfo = TypeInfo.of(valueType);

        @SuppressWarnings({"unchecked"})
        TypeHandler<?> valueTypeHandler = new RuntimeDelegatingTypeHandler(
                declaredValueTypeHandler.orElse(null),
                valueTypeInfo,
                context
        );

        return Optional.of((TypeHandler<T>) new StringMapTypeHandler<>(valueTypeHandler));
    }
}
