// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.family;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark if a {@link BlockFamily} supports blocks that have freeform shapes.
 * It takes an argument of the boolean type to mark if the annotated BlockFamily supports freeform shapes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FreeFormSupported {
    boolean value() default false;
}
