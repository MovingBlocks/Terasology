/*
 * Copyright 2017 MovingBlocks
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
 * Creates type handlers for a set of types. Type handler factories are generally used when a set of types
 * are similar in serialization structure.
 */
public interface TypeHandlerFactory {
    /**
     * Creates a {@link TypeHandler} for the given type {@link T}. If the type is not supported by
     * this {@link TypeHandlerFactory}, {@link Optional#empty()} is returned.
     *
     * This method is usually called only once for a type, so all expensive pre-computations and reflection
     * operations can be performed here so that the generated
     * {@link TypeHandler#serialize(Object, SerializationContext)} and
     * {@link TypeHandler#deserialize(PersistedData)} implementations are fast.
     *
     * @param typeInfo The {@link TypeInfo} of the type for which a {@link TypeHandler} must be generated.
     * @param typeSerializationLibrary The {@link TypeSerializationLibrary} for which the {@link TypeHandler}
     *                                 is being created.
     * @param <T> The type for which a {@link TypeHandler} must be generated.
     * @return An {@link Optional} wrapping the created {@link TypeHandler}, or {@link Optional#empty()}
     * if the type is not supported by this {@link TypeHandlerFactory}.
     */
    <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeSerializationLibrary typeSerializationLibrary);
}
