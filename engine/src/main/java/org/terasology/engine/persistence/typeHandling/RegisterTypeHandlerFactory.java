// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling;


import org.terasology.context.annotation.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link org.terasology.persistence.typeHandling.TypeHandlerFactory} to be automatically registered to the
 * {@link org.terasology.persistence.typeHandling.TypeHandlerLibrary} on environment change.
 * This can be used to (de)serialize custom components.
 * <p>
 * The {@link org.terasology.engine.registry.In} annotation can be used to access objects
 * in the parent {@link org.terasology.engine.context.Context}.
 */
@API
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterTypeHandlerFactory {
}
