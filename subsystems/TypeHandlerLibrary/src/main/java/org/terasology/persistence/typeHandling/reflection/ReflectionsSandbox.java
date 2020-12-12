// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.reflection;

import org.reflections.Reflections;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.reflection.TypeInfo;

import java.util.Optional;
import java.util.Set;

/**
 * Provides an interface to the sandboxed environment that serialization may be performed in.
 * This allows serialization to load types according to the rules specified in the sandbox it
 * is being executed in.
 * Uses {@link Reflections} for find and load types.
 */
public class ReflectionsSandbox implements SerializationSandbox {
    private final Reflections reflections;

    public ReflectionsSandbox(Reflections reflections) {
        this.reflections = reflections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Optional<Class<? extends T>> findSubTypeOf(String subTypeIdentifier, Class<T> clazz) {
        Set<Class<? extends T>> subTypes = reflections.getSubTypesOf(clazz);

        subTypes.removeIf(subType -> !subTypeIdentifier.equals(subType.getName()));

        if (subTypes.size() == 1) {
            return Optional.ofNullable(subTypes.iterator().next());
        }

        // If there are multiple/no possibilities, return empty Optional
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean isValidTypeHandlerDeclaration(TypeInfo<T> type, TypeHandler<T> typeHandler) {
        return true;
    }
}
