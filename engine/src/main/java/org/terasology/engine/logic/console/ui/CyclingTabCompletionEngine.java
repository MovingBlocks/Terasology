// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.ui;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.console.commandSystem.ConsoleCommand;
import org.terasology.engine.logic.console.commandSystem.exceptions.CommandSuggestionException;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.ConsoleColors;
import org.terasology.engine.logic.console.CoreMessageType;
import org.terasology.engine.logic.console.Message;
import org.terasology.gestalt.naming.Name;
import org.terasology.nui.FontColor;
import org.terasology.engine.utilities.StringUtility;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A text completion engine with cycle-through functionality
 *
 */
public class CyclingTabCompletionEngine implements TabCompletionEngine {
    private final Console console;
    private int selectionIndex;
    private List<String> previousMatches; //Alphabetically ordered list of matches
    private Message previousMessage;
    private Collection<String> commandNames;
    private String query;
    private LocalPlayer localPlayer;

    public CyclingTabCompletionEngine(Console console, LocalPlayer localPlayer) {
        this.console = console;
        this.localPlayer = localPlayer;
    }

    private boolean updateCommandNamesIfNecessary() {
        Collection<ConsoleCommand> commands = console.getCommands();

        if (commandNames != null && commandNames.size() == commands.size()) {
            return false;
        }

        commandNames = Collections2.transform(commands, input -> input.getName().toString());
        return true;
    }

    private Set<String> findMatches(Name commandName, List<String> commandParameters,
                                 ConsoleCommand command, int suggestedIndex) {
        if (suggestedIndex <= 0) {
            updateCommandNamesIfNecessary();
            return StringUtility.wildcardMatch(commandName.toString(), commandNames, true);
        } else if (command == null) {
            return null;
        }

        List<String> finishedParameters = Lists.newArrayList();

        for (int i = 0; i < suggestedIndex - 1; i++) {
            finishedParameters.add(commandParameters.get(i));
        }

        String currentValue = commandParameters.size() >= suggestedIndex ? commandParameters.get(suggestedIndex - 1) : null;
        EntityRef sender = localPlayer.getClientEntity();

        try {
            return command.suggest(currentValue, finishedParameters, sender);
        } catch (CommandSuggestionException e) {
            String causeMessage = e.getLocalizedMessage();

            if (causeMessage == null) {
                Throwable cause = e.getCause();
                causeMessage = cause.getLocalizedMessage();

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

        String commandNameRaw = console.processCommandName(query);
        Name commandName = new Name(commandNameRaw);
        List<String> commandParameters = console.processParameters(query);
        ConsoleCommand command = console.getCommand(commandName);
        int suggestedIndex = commandParameters.size() + (query.charAt(query.length() - 1) == ' ' ? 1 : 0);
        Set<String> matches = findMatches(commandName, commandParameters, command, suggestedIndex);

        if (matches == null || matches.size() <= 0) {
            return query;
        }

        if (previousMatches == null || !matches.equals(Sets.newHashSet(previousMatches))) {
            reset(false);

            if (matches.size() == 1) {
                return generateResult(matches.iterator().next(), commandName, commandParameters, suggestedIndex);
            }

/*            if (matches.length > MAX_CYCLES) {
                console.addMessage(new Message("Too many hits, please refine your search"));
                return query;
            }*/ //TODO Find out a better way to handle too many results while returning useful information

            previousMatches = Lists.newArrayList(matches);
            Collections.sort(previousMatches);
        }

        StringBuilder matchMessageString = new StringBuilder();

        for (int i = 0; i < previousMatches.size(); i++) {
            if (i > 0) {
                matchMessageString.append(' ');
            }

            String match = previousMatches.get(i);

            if (selectionIndex == i) {
                match = FontColor.getColored(match, ConsoleColors.COMMAND);
            }

            matchMessageString.append(match);
        }

        Message matchMessage = new Message(matchMessageString.toString());
        String suggestion = previousMatches.get(selectionIndex);

        if (previousMessage != null) {
            console.replaceMessage(previousMessage, matchMessage);
        } else {
            console.addMessage(matchMessage);
        }

        previousMessage = matchMessage;
        selectionIndex = (selectionIndex + 1) % previousMatches.size();

        return generateResult(suggestion, commandName, commandParameters, suggestedIndex);
    }

    private String generateResult(String suggestion, Name commandName,
                                  List<String> commandParameters, int suggestedIndex) {
        if (suggestedIndex <= 0) {
            return suggestion + " ";
        } else {
            StringBuilder result = new StringBuilder();
            result.append(commandName.toString());

            for (int i = 0; i < suggestedIndex - 1; i++) {
                result.append(" ");
                result.append(commandParameters.get(i));
            }

            result.append(" ");
            result.append(suggestion);

            result.append(" ");

            return result.toString();
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
