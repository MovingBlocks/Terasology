/*
 * Copyright 2018 MovingBlocks
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

import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
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
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeSerializationLibrary typeSerializationLibrary) {
        Type type = typeInfo.getType();

        if (!(type instanceof GenericArrayType || type instanceof Class && ((Class<?>) type).isArray())) {
            return Optional.empty();
        }

        Type elementType = type instanceof GenericArrayType
                ? ((GenericArrayType) type).getGenericComponentType()
                : ((Class<?>) type).getComponentType();

        TypeInfo<?> elementTypeInfo = TypeInfo.of(elementType);

        Optional<TypeHandler<?>> declaredElementTypeHandler = typeSerializationLibrary.getTypeHandler(elementType);

        @SuppressWarnings({"unchecked"})
        TypeHandler<?> elementTypeHandler = new RuntimeDelegatingTypeHandler(
                declaredElementTypeHandler.orElse(null),
                elementTypeInfo,
                typeSerializationLibrary
        );

        @SuppressWarnings({"unchecked"})
        TypeHandler<T> typeHandler = new ArrayTypeHandler(elementTypeHandler, elementTypeInfo);

        return Optional.of(typeHandler);
    }
}
