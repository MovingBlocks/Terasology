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

import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.coreTypes.EnumTypeHandler;
import org.terasology.reflection.TypeInfo;

import java.util.Optional;

/**
 * A {@link TypeHandlerFactory} that generates an {@link EnumTypeHandler} for enum types.
 */
public class EnumTypeHandlerFactory implements TypeHandlerFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeSerializationLibrary typeSerializationLibrary) {
        Class<? super T> enumClass = typeInfo.getRawType();
        if (!Enum.class.isAssignableFrom(enumClass) || Enum.class.equals(enumClass)) {
            return Optional.empty();
        }

        while (!enumClass.isEnum()) {
            enumClass = enumClass.getSuperclass();
        }

        return Optional.of((TypeHandler<T>) new EnumTypeHandler(enumClass));
    }
}
