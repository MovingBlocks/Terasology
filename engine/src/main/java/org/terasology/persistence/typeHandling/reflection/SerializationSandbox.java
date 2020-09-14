// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.reflection;

import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.reflection.TypeInfo;

import java.util.Optional;

/**
 * Provides an interface to the sandboxed environment that serialization may be performed in.
 * This allows serialization to load types according to the rules specified in the sandbox it
 * is being executed in.
 */
public interface SerializationSandbox {
    /**
     * Finds a subtype of {@link T} with the given identifier. If zero or more than one
     * subtypes are identified by the given identifier, {@link Optional#empty()} is returned.
     *
     * @param subTypeIdentifier The identifier of the subtype to look up
     * @param clazz             The {@link Class} of the base type whose subtype is to be found.
     * @param <T>               The base type whose subtype is to be found.
     * @return An {@link Optional} containing the unique subtype of {@link T}, if found.
     */
    <T> Optional<Class<? extends T>> findSubTypeOf(String subTypeIdentifier, Class<T> clazz);

    /**
     * Returns a unique identifier of the given subtype of the given base type. This method is
     * guaranteed to not return the same identifier for any other subtype of the given base type.
     *
     * @param subType  The {@link Class} specifying the subtype which is to be identified.
     * @param baseType The {@link Class} specifying the base type.
     * @param <T>      The base type whose subtype needs to be identified.
     * @return The unique identifier for {@code subType}.
     */
    default <T> String getSubTypeIdentifier(Class<? extends T> subType, Class<T> baseType) {
        return subType.getName();
    }

    /**
     * Checks whether the given {@link TypeHandler} should be allowed to handle instances of
     * the given type according to the rules in the sandbox.
     *
     * @param type        The {@link TypeInfo} describing {@link T}.
     * @param typeHandler An instance of the {@link TypeHandler} implementation that handles
     *                    instances of type {@link T}.
     * @param <T>         The type being handled by the {@link TypeHandler}.
     * @return True if the sandbox allows this handler implementation, false otherwise.
     */
    <T> boolean isValidTypeHandlerDeclaration(TypeInfo<T> type, TypeHandler<T> typeHandler);
}
