/*
 * Copyright 2020 MovingBlocks
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
