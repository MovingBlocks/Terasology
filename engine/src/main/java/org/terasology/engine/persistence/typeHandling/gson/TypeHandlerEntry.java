/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.persistence.typeHandling.gson;

import org.terasology.persistence.typeHandling.TypeHandler;

/**
 * A class containing a {@link TypeHandler} and the {@link Class} of the type it handles.
 *
 * @param <T> The type handled by the {@link TypeHandler} contained in the {@link TypeHandlerEntry}.
 */
public class TypeHandlerEntry<T> {
    public final Class<T> type;
    public final TypeHandler<T> typeHandler;

    private TypeHandlerEntry(Class<T> type, TypeHandler<T> typeHandler) {
        this.type = type;
        this.typeHandler = typeHandler;
    }

    /**
     * Creates a {@link TypeHandlerEntry} for a {@link TypeHandler} of the type {@link U}.
     */
    public static <U> TypeHandlerEntry<U> of(Class<U> type, TypeHandler<U> typeHandler) {
        return new TypeHandlerEntry<>(type, typeHandler);
    }
}
