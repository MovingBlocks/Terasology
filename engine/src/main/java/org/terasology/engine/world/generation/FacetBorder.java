// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FacetBorder {
    int top() default 0;

    int bottom() default 0;

    int sides() default 0;
}
