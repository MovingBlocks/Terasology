/*
 * Copyright 2019 MovingBlocks
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
    public <T> String getSubTypeIdentifier(Class<? extends T> subType, Class<T> baseType) {
        return subType.getName();
    }

    @Override
    public <T> boolean isValidTypeHandlerDeclaration(TypeInfo<T> type, TypeHandler<T> typeHandler) {
        return true;
    }
}
