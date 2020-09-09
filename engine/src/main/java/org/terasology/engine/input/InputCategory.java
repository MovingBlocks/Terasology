// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to declare categories of inputs to display in any input binding screens.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface InputCategory {
    /**
     * @return The id of the category, used within binds belonging to this category
     */
    String id();

    /**
     * @return The displayable name for this category
     */
    String displayName();

    /**
     * @return The ordering of binds within this category. Any binds not listed will appear in alphabetical order (by
     *         display name) at the end.
     */
    String[] ordering() default {};
}
