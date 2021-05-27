// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Makes sure the value of a field is initialized, such that it references to an
 * instance from the core registry. The field can and should be private.
 *
 * For example, the following makes sure that the entityManager field is
 * initialized to refer to the engine's EntityManager:
 *
 * <pre>
 * &#064;In
 * private EntityManager entityManager;
 * </pre>
 *
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface In {
}
