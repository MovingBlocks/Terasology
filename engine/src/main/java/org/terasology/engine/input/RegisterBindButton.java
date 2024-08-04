// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input;

import org.terasology.context.annotation.Index;
import org.terasology.input.ActivateMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Index
public @interface RegisterBindButton {
    String id();

    /**
     * @return The category this bind button belongs to, if not explicitly listed in the category
     */
    String category() default "";

    String description() default "";

    ActivateMode mode() default ActivateMode.BOTH;

    boolean repeating() default false;
}
