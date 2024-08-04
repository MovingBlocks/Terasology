// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.systems;

import org.terasology.context.annotation.Index;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a {@link ComponentSystem} to be registered in the environment.
 * <br><br>
 * A system can be conditionally registered, depending on optional dependencies by passing a list of optional module IDs:
 * <br>
 * <code>@RegisterSystem(value = RegisterMode.ALWAYS, requiresOptional = {"ModuleA","ModuleB"})</code>
 * <br>
 * In this case, the system would only be registered if both, <code>"ModuleA"</code>
 * and <code>"ModuleB"</code> are contained in the environment.
 * <br><br>
 * By default, a system is registered with {@link RegisterMode#ALWAYS} and no optional requirements.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Index
public @interface RegisterSystem {

    String[] requiresOptional() default {};

    RegisterMode value() default RegisterMode.ALWAYS;
}
