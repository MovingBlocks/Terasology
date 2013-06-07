/*
 * Copyright 2013 Moving Blocks
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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.collection.CircularBuffer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withModifier;

/**
 * The console handles commands and messages.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class Console {
    private static final Logger logger = LoggerFactory.getLogger(Console.class);
    private static final int MAX_MESSAGE_HISTORY = 255;

    private final List<CommandInfo> commands = Lists.newArrayList();
    private final Table<String, Integer, CommandInfo> commandLookup = HashBasedTable.create();
    private final CircularBuffer<Message> messageHistory = CircularBuffer.create(MAX_MESSAGE_HISTORY);

    private final Set<ConsoleSubscriber> messageSubscribers = Sets.newSetFromMap(new MapMaker().weakKeys().<ConsoleSubscriber, Boolean>makeMap());

    private boolean commandsSorted = false;

    public Console() {
        addMessage("Welcome to the wonderful world of Terasology!\n\nType 'help' to see a list with available commands.\nTo see a detailed command description try '/help \"<commandName>\"'.\nBe sure to surround text type parameters in quotes.\nNo commas needed for multiple parameters.\nCommands are case-sensitive, block names and such are not.");
    }

    public void registerCommandProvider(Object provider) {
        Predicate<? super Method> predicate = Predicates.<Method>and(withModifier(Modifier.PUBLIC), withAnnotation(Command.class));
        Set<Method> commandMethods = Reflections.getAllMethods(provider.getClass(), predicate);
        if (!commandMethods.isEmpty()) {
            for (Method method : commandMethods) {
                CommandInfo command = new CommandInfo(method, provider);
                commands.add(command);
                commandLookup.put(command.getName(), command.getParameterCount(), command);
            }
            commandsSorted = false;
        }
    }

    public void addMessage(String message) {
        addMessage(new Message(message));
    }

    public void addMessage(String message, MessageType type) {
        addMessage(new Message(message, type));
    }

    public void addMessage(Message message) {
        logger.info("[{}] {}", message.getType(), message.getMessage());
        messageHistory.add(message);
        for (ConsoleSubscriber subscriber : messageSubscribers) {
            subscriber.onNewConsoleMessage(message);
        }
    }

    public Iterable<Message> getMessages() {
        return messageHistory;
    }

    public void subscribe(ConsoleSubscriber subscriber) {
        this.messageSubscribers.add(subscriber);
    }

    public void unsubscribe(ConsoleSubscriber subscriber) {
        this.messageSubscribers.remove(subscriber);
    }

    /**
     * Execute a command.
     *
     * @param str The whole string of the command including the command name and the optional parameters.
     * @return Returns true if the command was executed successfully.
     */
    public boolean execute(String str) {
        //remove double spaces
        str = str.replaceAll("\\s\\s+", " ");

        //get the command name
        int commandEndIndex = str.indexOf(" ");
        String commandName;
        if (commandEndIndex >= 0) {
            commandName = str.substring(0, commandEndIndex);
        } else {
            commandName = str;
            str = "";
            commandEndIndex = 0;
        }

        //remove command name from string
        str = str.substring(commandEndIndex).trim();

        //get the parameters
        String[] params = str.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        String paramsStr = "";
        int paramsCount = 0;

        for (String s : params) {
            if (s.trim().isEmpty()) {
                continue;
            }

            if (!paramsStr.isEmpty()) {
                paramsStr += ",";
            }
            paramsStr += s;
            paramsCount++;
        }

        //get the command
        CommandInfo cmd = commandLookup.get(commandName, paramsCount);

        //check if the command is loaded
        if (cmd == null) {
            if (commandLookup.containsRow(commandName)) {
                addMessage("Incorrect number of parameters");
            } else {
                addMessage("Unknown command '" + commandName + "'");
            }

            return false;
        }
        String executeString = paramsStr;
        logger.debug("Execute command with params '{}'", executeString);

        try {
            String result = cmd.execute(paramsStr);
            if (result != null && !result.isEmpty()) {
                addMessage(result);
            }

            return true;
        } catch (Exception e) {
            // TODO: better error handling and error message
            addMessage(cmd.getUsageMessage());
            addMessage("Error executing command '" + commandName + "'.");
            logger.warn("Failed to execute command", e);

            return false;
        }
    }

    /**
     * Get a group of commands by their name. These will vary by the number of parameters they accept
     *
     * @param name The name of the command.
     * @return An iterator over the commands.
     */
    public Collection<CommandInfo> getCommand(String name) {
        return commandLookup.row(name).values();
    }

    /**
     * Get the list of all loaded commands.
     *
     * @return Returns the command list.
     */
    public List<CommandInfo> getCommandList() {
        if (!commandsSorted) {
            Collections.sort(commands, new Comparator<CommandInfo>() {
                @Override
                public int compare(CommandInfo o1, CommandInfo o2) {
                    int nameComp = o1.getName().compareTo(o2.getName());
                    if (nameComp == 0) {
                        return o1.getParameterCount() - o2.getParameterCount();
                    }
                    return nameComp;
                }
            });
            commandsSorted = true;
        }
        return commands;
    }
}
