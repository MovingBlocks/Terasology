// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.reflection;

import org.reflections.Reflections;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.reflection.TypeInfo;

import java.util.Optional;
import java.util.Set;

public class ReflectionsSandbox implements SerializationSandbox {
    private final Reflections reflections;

    public ReflectionsSandbox(Reflections reflections) {
        this.reflections = reflections;
    }

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

    @Override
    public <T> boolean isValidTypeHandlerDeclaration(TypeInfo<T> type, TypeHandler<T> typeHandler) {
        return true;
    }
}
