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
package org.terasology.persistence.typeHandling.mathTypes.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.mathTypes.Rect2fTypeHandler;
import org.terasology.reflection.TypeInfo;

import java.util.Optional;

public class Rect2fTypeHandlerFactory implements TypeHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(Rect2fTypeHandlerFactory.class);

    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeSerializationLibrary typeSerializationLibrary) {
        if (!typeInfo.equals(TypeInfo.of(Rect2f.class))) {
            return Optional.empty();
        }

        Optional<TypeHandler<Vector2f>> vector2fTypeHandler = typeSerializationLibrary.getTypeHandler(Vector2f.class);

        if (!vector2fTypeHandler.isPresent()) {
            LOGGER.error("No Vector2f type handler found");
            return Optional.empty();
        }

        Rect2fTypeHandler rect2fTypeHandler =
                new Rect2fTypeHandler(vector2fTypeHandler.get());

        return Optional.of((TypeHandler<T>) rect2fTypeHandler);

    }
}
