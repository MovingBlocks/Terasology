// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;

import org.terasology.persistence.typeHandling.reflection.SerializationSandbox;
import org.terasology.reflection.TypeInfo;

/**
 * Represents the context in which a {@link TypeHandlerFactory} creates {@link TypeHandler} instances.
 * <p>
 * The {@link TypeHandlerContext} is used to look up {@link TypeHandler} instances and load classes
 * while staying within the sandbox rules if called from a module.
 */
public class TypeHandlerContext {
    private TypeHandlerLibrary typeHandlerLibrary;
    private SerializationSandbox sandbox;

    public TypeHandlerContext(TypeHandlerLibrary typeHandlerLibrary, SerializationSandbox sandbox) {
        this.typeHandlerLibrary = typeHandlerLibrary;
        this.sandbox = sandbox;
    }

    /**
     * Returns the {@link TypeHandlerLibrary} that called the {@link TypeHandlerFactory#create(TypeInfo, TypeHandlerContext)} method.
     */
    public TypeHandlerLibrary getTypeHandlerLibrary() {
        return typeHandlerLibrary;
    }

    /**
     * Returns the {@link SerializationSandbox} to use to load classes in the
     * {@link TypeHandlerFactory#create(TypeInfo, TypeHandlerContext)} method.
     */
    public SerializationSandbox getSandbox() {
        return sandbox;
    }
}
