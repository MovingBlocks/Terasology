// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;

import org.terasology.reflection.TypeInfo;

import java.util.Optional;

/**
 * Represents a {@link TypeHandlerFactory} that generates type handlers for a specific type.
 *
 * @param <T> The type for which this {@link TypeHandlerFactory} generates type handlers.
 */
public abstract class SpecificTypeHandlerFactory<T> implements TypeHandlerFactory {
    protected final TypeInfo<T> type;

    public SpecificTypeHandlerFactory(TypeInfo<T> type) {
        this.type = type;
    }

    public SpecificTypeHandlerFactory(Class<T> clazz) {
        this(TypeInfo.of(clazz));
    }

    @Override
    public <R> Optional<TypeHandler<R>> create(TypeInfo<R> typeInfo, TypeHandlerContext context) {
        if (typeInfo.equals(type)) {
            return Optional.of((TypeHandler<R>) createHandler(context));
        }
        return Optional.empty();
    }

    /**
     * Creates the {@link TypeHandler} for the specific type.
     *
     * @param context The context in which to create the {@link TypeHandler}.
     * @return The created type handler.
     */
    protected abstract TypeHandler<T> createHandler(TypeHandlerContext context);
}
