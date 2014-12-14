/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.console.ui;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleColors;
import org.terasology.logic.console.CoreMessageType;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.dynamic.CommandSuggestionException;
import org.terasology.logic.console.dynamic.ICommand;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.FontColor;
import org.terasology.utilities.CamelCaseMatcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A text completion engine with cycle-through functionality
 *
 * @author Martin Steiger
 */
public class CyclingTabCompletionEngine implements TabCompletionEngine {
    private static final int MAX_CYCLES = 10;
    private final Console console;
    private int selectionIndex;
    private String[] previousMatches;
    private Message previousMessage;
    private Collection<String> commandNames;
    private String query;

    public CyclingTabCompletionEngine(Console console) {
        this.console = console;
    }

    private boolean updateCommandNamesIfNecessary() {
        Collection<ICommand> commands = console.getCommands();

        if (commandNames != null && commandNames.size() == commands.size()) {
            return false;
        }

        commandNames = Collections2.transform(commands, new Function<ICommand, String>() {
            @Override
            public String apply(ICommand input) {
                return input.getName();
            }
        });

        return true;
    }

    private String[] findMatches(String commandName, List<String> commandParameters,
                                 ICommand command, int suggestedIndex) {
        if (suggestedIndex <= 0) {
            updateCommandNamesIfNecessary();
            List<String> matches = Lists.newArrayList(CamelCaseMatcher.getMatches(commandName, commandNames));
            Collections.sort(matches);
            return matches.toArray(new String[matches.size()]);
        }

        List<String> finishedParameters = Lists.newArrayList();

        for (int i = 0; i < suggestedIndex - 1; i++) {
            finishedParameters.add(commandParameters.get(i));
        }

        EntityRef sender = CoreRegistry.get(LocalPlayer.class).getClientEntity();

        try {
            return command.suggestRaw(finishedParameters, sender);
        } catch (CommandSuggestionException e) {
            Throwable cause = e.getCause();
            String causeMessage = e.getLocalizedMessage();

            e.printStackTrace();

            if (causeMessage == null || causeMessage.isEmpty()) {
                causeMessage = cause.getMessage();

                if (causeMessage == null || causeMessage.isEmpty()) {
                    causeMessage = cause.toString();

                    if (causeMessage == null || causeMessage.isEmpty()) {
                        return null;
                    }
                }
            }

            console.addMessage("Error when suggesting command: " + causeMessage, CoreMessageType.ERROR);
            return null;
        }
    }

    @Override
    public String complete(String rawCommand) {
        if (rawCommand.length() <= 0) {
            reset();
            previousMessage = new Message("Type 'help' to list all commands.");
            console.addMessage(previousMessage);
            return null;
        } else if (query == null) {
            query = rawCommand;
        }

        String commandName = console.processCommandName(query);
        List<String> commandParameters = console.processParameters(query);
        ICommand command = console.getCommand(commandName);
        int suggestedIndex = commandParameters.size() + (query.charAt(query.length() - 1) == ' ' ? 1 : 0);
        String[] matches = findMatches(commandName, commandParameters, command, suggestedIndex);

        if (matches == null || matches.length <= 0) {
            return query;
        }

        if (!Arrays.equals(matches, previousMatches)) {
            reset(false);

            if (matches.length == 1) {
                return generateResult(matches[0], commandName, commandParameters, suggestedIndex);
            }

            if (matches.length > MAX_CYCLES) {
                console.addMessage(new Message("Too many hits, please refine your search"));
                return query;
            }

            previousMatches = matches;
        }

        StringBuilder commandMatches = new StringBuilder();

        for (int i = 0; i < previousMatches.length; i++) {
            if (i > 0) {
                commandMatches.append(' ');
            }

            String name = previousMatches[i];

            if (selectionIndex == i) {
                name = FontColor.getColored(name, ConsoleColors.COMMAND);
            }

            commandMatches.append(name);
        }

        Message message = new Message(commandMatches.toString());
        String suggestion = previousMatches[selectionIndex];

        if (previousMessage != null) {
            console.replaceMessage(previousMessage, message);
        } else {
            console.addMessage(message);
        }

        previousMessage = message;
        selectionIndex = (selectionIndex + 1) % previousMatches.length;

        return generateResult(suggestion, commandName, commandParameters, suggestedIndex);
    }

    private String generateResult(String suggestion, String commandName,
                                  List<String> commandParameters, int suggestedIndex) {
        if (suggestedIndex <= 0) {
            return suggestion;
        } else {
            String result = commandName;

            for (int i = 0; i < suggestedIndex - 1; i++) {
                result += " " + commandParameters.get(i);
            }

            return result + " " + suggestion;
        }
    }

    private void reset(boolean removeQuery) {
        if (previousMessage != null) {
            console.removeMessage(previousMessage);
        }

        if (removeQuery) {
            query = null;
        }

        previousMessage = null;
        previousMatches = null;
        selectionIndex = 0;
    }

    @Override
    public void reset() {
        reset(true);
    }
}
