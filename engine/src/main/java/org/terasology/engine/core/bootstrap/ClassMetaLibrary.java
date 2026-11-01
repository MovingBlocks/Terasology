// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.bootstrap;

import org.terasology.context.annotation.API;
import org.terasology.gestalt.naming.Name;

import java.lang.annotation.Annotation;

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
