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
package org.terasology.logic.console.dynamic;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.terasology.entitySystem.entity.EntityRef;

import java.util.Comparator;
import java.util.List;

/**
 * @author Limeth
 */
public interface ICommand extends Comparable<ICommand> {
    public static final Comparator<ICommand> COMPARATOR = new Comparator<ICommand>() {
        @Override
        public int compare(ICommand o1, ICommand o2) {
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
    @NotNull
    String getName();

    /**
     * @return The parameter definitions of this command
     */
    @NotNull
    CommandParameter[] getParameters();

    /**
     * @return Whether this command is executed on the server
     */
    boolean isRunOnServer();

    /**
     * @return A short summary of what this Command does
     */
    String getDescription();

    /**
     * @return A detailed description of how to use this command
     */
    String getHelpText();

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
    @NotNull
    String getUsage();

    /**
     * Executes the command
     *
     * @param parameters Parameters in an Object[] array as defined in {@link org.terasology.logic.console.dynamic.Command#getParameters()}.
     * @return A reply to the sender.
     */
    @Nullable
    String executeRaw(List<String> parameters, EntityRef sender) throws CommandExecutionException;

    /**
     * Suggests valid parameters.
     *
     * @param parameters Currently provided parameters in an Object[] array.
     * @return A reply to the sender.
     */
    //TODO maybe return an array of serializable objects?
    @Nullable
    String[] suggestRaw(List<String> parameters, EntityRef sender) throws CommandSuggestionException;

    /**
     * @param command A command to compare this command to
     * @return The result of {@link #COMPARATOR}'s {@code compare(this, command)} command.
     */
    @Override
    int compareTo(ICommand command);
}
