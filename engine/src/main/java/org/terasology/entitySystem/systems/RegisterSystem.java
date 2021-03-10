/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.engine.entitySystem.systems;

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
public @interface RegisterSystem {

    String[] requiresOptional() default {};

    RegisterMode value() default RegisterMode.ALWAYS;
}
