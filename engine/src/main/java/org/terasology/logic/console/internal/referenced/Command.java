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

package org.terasology.logic.console.internal.referenced;

import org.terasology.logic.permission.PermissionManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be available as a command in the console, if it is in a ComponentSystem.
 * <p/>
 * Command names are case-insensitive.
 * Command methods can have an {@link org.terasology.entitySystem.entity.EntityRef} parameter at the end,
 * which will be populated with the entity of the client calling the command.
 * Parameters should be annotated by the {@link CommandParam} annotation.
 * <p/>
 * It is possible (and encouraged) to use other parameter types instead of {@link String}.
 * The parameter type can also be a one-dimensional array. The default delimiter is a comma ({@code ,}),
 * to change the delimiter, provide the {@code arrayDelimiter} argument to the linked {@link CommandParam}.
 * Array delimiters can be escaped using the {@code Command.ARRAY_DELIMITER_ESCAPE_CHARACTER}.
 * To make a command accept variable amount of arguments (varargs), set the {@link CommandParam}'s
 * {@code arrayDelimiter} to {@code Command.ARRAY_DELIMITER_VARARGS}.
 * <p/>
 * An example varargs command:
 * <pre>{@code
 * //Filled out Command annotation here
 * public String exampleCommand(
 *      @CommandParam("time") int time,
 *      @CommandParam(value = "options", arrayDelimiter = Command.ARRAY_DELIMITER_VARARGS) String[] varargs
 * ) {
 *     return "Hello, World!";
 * }
 * }</pre>
 * There can be registered more commands with the same name, but their parameters must be different.
 * The executed command is selected by the following priorities:
 * - argument count matches the argument count of the {@link Command} and the {@link Command} doesn't have a varargs array;
 * - argument count is higher or equal to the {@link Command} containing a varargs array. More argument matches preferred.
 *
 * @author Immortius, Limeth
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    public static final char ARRAY_DELIMITER_DEFAULT = ',';
    public static final char ARRAY_DELIMITER_VARARGS = ' ';
    public static final char ARRAY_DELIMITER_ESCAPE_CHARACTER = '\\';

    /**
     * @return The name of the command, if specified. Otherwise the method name is used.
     */
    String name() default "";

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

    String requiredPermission() default PermissionManager.OPERATOR_PERMISSION;
}
