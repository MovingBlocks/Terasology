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
import org.terasology.persistence.typeHandling.coreTypes.QueueTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.SetTypeHandler;
import org.terasology.reflection.TypeInfo;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 * Creates type handlers for {@link Collection} types.
 */
public class CollectionTypeHandlerFactory implements TypeHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionTypeHandlerFactory.class);

    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeSerializationLibrary typeSerializationLibrary) {
        Class<? super T> rawType = typeInfo.getRawType();

        if (!Collection.class.isAssignableFrom(rawType)) {
            return Optional.empty();
        }

        Type elementType = ReflectionUtil.getTypeParameter(typeInfo.getType(), 0);

        if (elementType == null) {
            LOGGER.error("Collection is not parameterized and cannot be serialized");
            return Optional.empty();
        }

        TypeHandler<?> elementTypeHandler = typeSerializationLibrary.getHandlerFor(elementType);

        TypeHandler<?> typeHandler;

        // TODO: Replace with generic CollectionTypeHandler
        if (List.class.isAssignableFrom(rawType)) {
            typeHandler = new ListTypeHandler<>(elementTypeHandler);
        } else if (Queue.class.isAssignableFrom(rawType)) {
            typeHandler = new QueueTypeHandler<>(elementTypeHandler);
        }
        else if (Set.class.isAssignableFrom(rawType)) {
            typeHandler = new SetTypeHandler<>(elementTypeHandler);
        }
        else {
            return Optional.empty();
        }

        return Optional.of((TypeHandler<T>) typeHandler);
    }
}
