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
import org.terasology.persistence.typeHandling.coreTypes.StringMapTypeHandler;
import org.terasology.reflection.TypeInfo;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

public class StringMapTypeHandlerFactory implements TypeHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringMapTypeHandler.class);

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeSerializationLibrary typeSerializationLibrary) {
        if (!Map.class.isAssignableFrom(typeInfo.getRawType())) {
            return Optional.empty();
        }

        Type keyType = ReflectionUtil.getTypeParameter(typeInfo.getType(), 0);
        Type valueType = ReflectionUtil.getTypeParameter(typeInfo.getType(), 1);

        if (!String.class.equals(keyType)) {
            return Optional.empty();
        }

        if (valueType == null) {
            LOGGER.error("Map is not parameterized and cannot be serialized");
            return Optional.empty();
        }

        TypeHandler<T> valueTypeHandler = (TypeHandler<T>) typeSerializationLibrary.getTypeHandler(valueType);

        return Optional.of((TypeHandler<T>) new StringMapTypeHandler<>(valueTypeHandler));
    }
}
