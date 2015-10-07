/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.engine.bootstrap;

import java.lang.annotation.Annotation;

import org.terasology.module.sandbox.API;
import org.terasology.naming.Name;

/**
 * Provides information on available classes.
 */
@API
public interface ClassMetaLibrary {

    /**
     * @param type The type to find subtypes of
     * @param <U>  The type to find subtypes of
     * @param name the name of the name to look for (case-sensitive)
     * @return An Iterable over all matching subtypes that appear in the module environment
     */
    <U> Iterable<Class<? extends U>> getSubtypesOf(Class<U> type, String name);

    /**
     * @param type The type to find subtypes of
     * @param <U>  The type to find subtypes of
     * @return A Iterable over all subtypes of type that appear in the module environment
     */
    <U> Iterable<Class<? extends U>> getSubtypesOf(Class<U> type);

    /**
     * @param annotation The annotation of interest
     * @return All types in the environment that are either marked by the given annotation, or are subtypes
     *         of a type marked with the annotation if the annotation is marked as @Inherited
     */
    Iterable<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation);

    /**
     * Determines the module from which the give class originates from.
     *
     * @param type The type to find the module for
     * @return The module providing the class, or null if it doesn't come from a module.
     */
    Name getModuleProviding(Class<?> type);
}
