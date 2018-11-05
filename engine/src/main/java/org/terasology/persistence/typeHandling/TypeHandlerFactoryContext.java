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

import org.terasology.reflection.TypeInfo;

import java.util.Arrays;

/**
 * Represents the context in which a {@link TypeHandlerFactory} creates {@link TypeHandler} instances.
 * <p>
 * The {@link TypeHandlerFactoryContext} is used to look up {@link TypeHandler} instances and load classes
 * while staying within the sandbox rules if called from a module.
 */
public class TypeHandlerFactoryContext {
    private TypeSerializationLibrary typeSerializationLibrary;
    private ClassLoader[] classLoaders;

    public TypeHandlerFactoryContext(TypeSerializationLibrary typeSerializationLibrary, Class<?>... classes) {
        this(
                typeSerializationLibrary,
                Arrays.stream(classes)
                        .map(Class::getClassLoader)
                        .toArray(ClassLoader[]::new)
        );
    }

    public TypeHandlerFactoryContext(TypeSerializationLibrary typeSerializationLibrary, ClassLoader... classLoaders) {
        this.typeSerializationLibrary = typeSerializationLibrary;
        this.classLoaders = classLoaders;
    }

    /**
     * Returns the {@link TypeSerializationLibrary} that called the {@link TypeHandlerFactory#create(TypeInfo, TypeHandlerFactoryContext)} method.
     */
    public TypeSerializationLibrary getTypeSerializationLibrary() {
        return typeSerializationLibrary;
    }

    /**
     * Returns the {@link ClassLoader}s to use to load classes in the
     * {@link TypeHandlerFactory#create(TypeInfo, TypeHandlerFactoryContext)} method.
     * <p>
     * If classes are loaded manually using a method like {@link Class#forName(String)}, these
     * {@link ClassLoader}s must be used so that modules remain sandboxed.
     */
    public ClassLoader[] getClassLoaders() {
        return classLoaders;
    }
}
