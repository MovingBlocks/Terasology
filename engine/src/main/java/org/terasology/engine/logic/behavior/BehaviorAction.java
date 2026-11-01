// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior;

import org.terasology.context.annotation.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Internally used name for a Action class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@API
public @interface BehaviorAction {
    String name();

    boolean isDecorator() default false;
}
