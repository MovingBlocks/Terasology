// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;

import org.terasology.context.annotation.IndexInherited;
import org.terasology.reflection.TypeInfo;

import java.util.Optional;

/**
 * Creates type handlers for a set of types. Type handler factories are generally used when a set of types
 * are similar in serialization structure.
 */
@IndexInherited
public interface TypeHandlerFactory {
    /**
     * Creates a {@link TypeHandler} for the given type {@link T}. If the type is not supported by
     * this {@link TypeHandlerFactory}, {@link Optional#empty()} is returned.
     *
     * This method is usually called only once for a type, so all expensive pre-computations and reflection
     * operations can be performed here so that the generated
     * {@link TypeHandler#serialize(Object, PersistedDataSerializer)} and
     * {@link TypeHandler#deserialize(PersistedData)} implementations are fast.
     *
     * @param <T> The type for which a {@link TypeHandler} must be generated.
     * @param typeInfo The {@link TypeInfo} of the type for which a {@link TypeHandler} must be generated.
     * @param context The {@link TypeHandlerLibrary} for which the {@link TypeHandler}
     *                                 is being created.
     * @return An {@link Optional} wrapping the created {@link TypeHandler}, or {@link Optional#empty()}
     * if the type is not supported by this {@link TypeHandlerFactory}.
     */
    <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeHandlerContext context);
}
