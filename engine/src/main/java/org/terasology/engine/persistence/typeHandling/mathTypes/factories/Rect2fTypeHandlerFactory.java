// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.mathTypes.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.engine.persistence.typeHandling.TypeHandler;
import org.terasology.engine.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.engine.persistence.typeHandling.TypeHandlerContext;
import org.terasology.engine.persistence.typeHandling.mathTypes.Rect2fTypeHandler;
import org.terasology.reflection.TypeInfo;

import java.util.Optional;

public class Rect2fTypeHandlerFactory implements TypeHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(Rect2fTypeHandlerFactory.class);

    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeHandlerContext context) {
        if (!typeInfo.equals(TypeInfo.of(Rect2f.class))) {
            return Optional.empty();
        }

        Optional<TypeHandler<Vector2f>> vector2fTypeHandler =
                context.getTypeHandlerLibrary().getTypeHandler(Vector2f.class);

        if (!vector2fTypeHandler.isPresent()) {
            LOGGER.error("No Vector2f type handler found");
            return Optional.empty();
        }

        Rect2fTypeHandler rect2fTypeHandler =
                new Rect2fTypeHandler(vector2fTypeHandler.get());

        return Optional.of((TypeHandler<T>) rect2fTypeHandler);

    }
}
