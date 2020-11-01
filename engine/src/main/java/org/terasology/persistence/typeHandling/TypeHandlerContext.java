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
