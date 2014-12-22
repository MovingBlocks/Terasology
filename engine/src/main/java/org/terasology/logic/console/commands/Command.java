/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.console.commands;

import com.google.common.collect.ImmutableList;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.commands.exceptions.CommandExecutionException;
import org.terasology.logic.console.commands.exceptions.CommandSuggestionException;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Interface of commands used in a {@link org.terasology.logic.console.Console}
 *
 * @author Limeth
 */
public interface Command extends Comparable<Command> {
    public static final Comparator<Command> COMPARATOR = new Comparator<Command>() {
        @Override
        public int compare(Command o1, Command o2) {
            int nameComparison = o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());

            if (nameComparison != 0) {
                return nameComparison;
            }

            if (!o1.endsWithVarargs() && o2.endsWithVarargs()) {
                return -1;
            } else if (o1.endsWithVarargs() && !o2.endsWithVarargs()) {
                return 1;
            }

            if (o1.getRequiredParameterCount() > o2.getRequiredParameterCount()) {
                return -1;
            } else if (o1.getRequiredParameterCount() < o2.getRequiredParameterCount()) {
                return 1;
            }

            return 0;
        }
    };

    /**
     * The name must not be null or empty.
     *
     * @return The name of this command
     */
    String getName();

    /**
     * @return The parameter definitions of this command, never null.
     */
    ImmutableList<CommandParameter> getParameters();

    /**
     * @return Whether this command is executed on the server
     */
    boolean isRunOnServer();

    /**
     * @return The permission required to execute this command
     */
    String getRequiredPermission();

    /**
     * @return A short summary of what this Command does
     */
    String getDescription();

    /**
     * @return True, if the description is not null and is not empty
     */
    boolean hasDescription();

    /**
     * @return A detailed description of how to use this command
     */
    String getHelpText();

    /**
     * @return True, if the help text is not null and is not empty
     */
    boolean hasHelpText();

    /**
     * @return The required amount of parameters for this method to function properly
     */
    int getRequiredParameterCount();

    /**
     * @return Whether the command ends with a varargs array and the parameter amount can exceed
     * the result of {@link #getRequiredParameterCount()}
     */
    boolean endsWithVarargs();

    /**
     * The usage must not be null or empty.
     *
     * @return The usage hint of this command
     */
    String getUsage();

    /**
     * @return The object containing the command logic
     */
    Object getSource();

    /**
     * Executes the command
     *
     * @param parameters Parameters in an Object[] array as defined in {@link AbstractCommand#getParameters()}.
     * @return A reply to the sender.
     */
    String executeRaw(List<String> parameters, EntityRef sender) throws CommandExecutionException;

    /**
     * Suggests valid parameters.
     *
     * @param parameters Currently provided parameters in an Object[] array.
     * @return A set of suggestions. Never null.
     */
    //TODO maybe return an array of serializable objects?
    Set<String> suggestRaw(String currentValue, List<String> parameters, EntityRef sender) throws CommandSuggestionException;

    /**
     * @param command A command to compare this command to
     * @return The result of {@link #COMPARATOR}'s {@code compare(this, command)} command.
     */
    @Override
    int compareTo(Command command);
}
