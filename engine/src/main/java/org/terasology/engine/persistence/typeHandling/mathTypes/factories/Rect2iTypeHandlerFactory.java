// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.mathTypes.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.engine.persistence.typeHandling.TypeHandler;
import org.terasology.engine.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.engine.persistence.typeHandling.TypeHandlerContext;
import org.terasology.engine.persistence.typeHandling.mathTypes.Rect2iTypeHandler;
import org.terasology.reflection.TypeInfo;

import java.util.Optional;

public class Rect2iTypeHandlerFactory implements TypeHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(Rect2iTypeHandlerFactory.class);

    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeHandlerContext context) {
        if (!typeInfo.equals(TypeInfo.of(Rect2i.class))) {
            return Optional.empty();
        }

        Optional<TypeHandler<Vector2i>> vector2iTypeHandler =
                context.getTypeHandlerLibrary().getTypeHandler(Vector2i.class);

        if (!vector2iTypeHandler.isPresent()) {
            LOGGER.error("No Vector2i type handler found");
            return Optional.empty();
        }

        Rect2iTypeHandler rect2iTypeHandler =
                new Rect2iTypeHandler(vector2iTypeHandler.get());

        return Optional.of((TypeHandler<T>) rect2iTypeHandler);

    }
}
