// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.gson;

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
