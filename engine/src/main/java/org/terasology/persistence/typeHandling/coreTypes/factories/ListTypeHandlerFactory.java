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
import org.terasology.persistence.typeHandling.coreTypes.ListTypeHandler;
import org.terasology.reflection.TypeInfo;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * Creates a {@link ListTypeHandler} for {@link List} types.
 */
public class ListTypeHandlerFactory implements TypeHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListTypeHandlerFactory.class);

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeSerializationLibrary typeSerializationLibrary) {
        if (!List.class.isAssignableFrom(typeInfo.getRawType())) {
            return Optional.empty();
        }

        Type listElementType = ReflectionUtil.getTypeParameter(typeInfo.getType(), 0);

        if (listElementType == null) {
            LOGGER.error("List is not parameterized and cannot be serialized");
            return Optional.empty();
        }

        TypeHandler<T> listElementTypeHandler = (TypeHandler<T>) typeSerializationLibrary.getHandlerFor(listElementType);

        return Optional.of((TypeHandler<T>) new ListTypeHandler<>(listElementTypeHandler));
    }
}
