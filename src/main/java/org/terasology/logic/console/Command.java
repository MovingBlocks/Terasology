/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.logic.console;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be available as a command in the console, if it is in a ComponentSystem.
 * <p/>
 * Commands methods can have an EntityRef parameter at the end, which will be populated with the entity of the
 * client calling the command.
 *
 * @author Immortius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    /**
     * @return A short summary of what this Command does
     */
    String shortDescription() default "";

    /**
     * @return A detailed description of how to use this command
     */
    String helpText() default "";

    /**
     * @return Whether this command should be sent to and run on the server.
     */
    boolean runOnServer() default false;
}
