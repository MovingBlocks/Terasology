// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling;

import org.terasology.gestalt.module.sandbox.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link TypeHandler} to be automatically registered at a
 * {@link TypeHandlerLibrary TypeHandlerLibrary} on environment change.
 * This can be used to (de)serialize custom components.
 * <p>
 * By default, the TypeHandler must have an empty constructor.
 */
@API
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterTypeHandler {
}
