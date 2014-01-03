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

package org.terasology.logic.console.internal;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleMessageEvent;
import org.terasology.logic.console.ConsoleSubscriber;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.MessageType;
import org.terasology.network.Client;
import org.terasology.network.NetworkSystem;
import org.terasology.utilities.collection.CircularBuffer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.reflections.ReflectionUtils.withModifier;

/**
 * The console handles commands and messages.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class ConsoleImpl implements Console {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleImpl.class);
    private static final String PARAM_SPLIT_REGEX = " (?=([^\"]*\"[^\"]*\")*[^\"]*$)";
    private static final Joiner PARAMETER_JOINER = Joiner.on(", ");
    private static final int MAX_MESSAGE_HISTORY = 255;
    private static final int MAX_COMMAND_HISTORY = 30;

    private final List<CommandInfo> commands = Lists.newArrayList();
    private final Table<String, Integer, CommandInfo> commandLookup = HashBasedTable.create();
    private final CircularBuffer<Message> messageHistory = CircularBuffer.create(MAX_MESSAGE_HISTORY);
    private final CircularBuffer<String> localCommandHistory = CircularBuffer.create(MAX_COMMAND_HISTORY);

    private final Set<ConsoleSubscriber> messageSubscribers = Sets.newSetFromMap(new MapMaker().weakKeys().<ConsoleSubscriber, Boolean>makeMap());

    private NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);

    private boolean commandsSorted;

    public ConsoleImpl() {
        addMessage("Welcome to the wonderful world of Terasology!\n" +
                "\n" +
                "Type 'help' to see a list with available commands.\n" +
                "To see a detailed command description try '/help \"<commandName>\"'.\n" +
                "Be sure to surround text type parameters in quotes.\n" +
                "No commas needed for multiple parameters.\n" +
                "Commands are case-sensitive, block names and such are not.");
    }

    /**
     * Registers an object as a command provider - all methods annotated with @Command will be made available on the console.
     *
     * @param provider
     */
    @Override
    public void registerCommandProvider(Object provider) {
        Predicate<? super Method> predicate = Predicates.<Method>and(withModifier(Modifier.PUBLIC), ReflectionUtils.withAnnotation(Command.class));
        Set<Method> commandMethods = ReflectionUtils.getAllMethods(provider.getClass(), predicate);
        if (!commandMethods.isEmpty()) {
            for (Method method : commandMethods) {
                CommandInfo command = new CommandInfo(method, provider);
                commands.add(command);
                commandLookup.put(command.getName(), command.getParameterCount(), command);
            }
            commandsSorted = false;
        }
    }

    @Override
    public void dispose() {
        commands.clear();
        commandLookup.clear();
        messageHistory.clear();
    }

    /**
     * Adds a message to the console (as a CoreMessageType.CONSOLE message)
     *
     * @param message
     */
    @Override
    public void addMessage(String message) {
        addMessage(new Message(message));
    }

    /**
     * Adds a message to the console
     *
     * @param message
     * @param type
     */
    @Override
    public void addMessage(String message, MessageType type) {
        addMessage(new Message(message, type));
    }

    /**
     * Adds a message to the console
     *
     * @param message
     */
    @Override
    public void addMessage(Message message) {
        logger.info("[{}] {}", message.getType(), message.getMessage());
        messageHistory.add(message);
        for (ConsoleSubscriber subscriber : messageSubscribers) {
            subscriber.onNewConsoleMessage(message);
        }
    }

    /**
     * @return An iterator over all messages in the console
     */
    @Override
    public Iterable<Message> getMessages() {
        return messageHistory;
    }

    @Override
    public int previousCommandSize() {
        return localCommandHistory.size();
    }

    @Override
    public String getPreviousCommand(int index) {
        return localCommandHistory.get(index);
    }

    /**
     * Subscribe for notification of all messages added to the console
     *
     * @param subscriber
     */
    @Override
    public void subscribe(ConsoleSubscriber subscriber) {
        this.messageSubscribers.add(subscriber);
    }

    /**
     * Unsubscribe from receiving notification of messages being added to the console
     *
     * @param subscriber
     */
    @Override
    public void unsubscribe(ConsoleSubscriber subscriber) {
        this.messageSubscribers.remove(subscriber);
    }

    /**
     * Execute a command.
     *
     * @param command The whole string of the command including the command name and the optional parameters.
     * @return Returns true if the command was executed successfully.
     */
    @Override
    public boolean execute(String command, EntityRef callingClient) {
        if (command.length() == 0) {
            return false;
        }

        Client owner = networkSystem.getOwner(callingClient);
        if (owner != null && owner.isLocal()) {
            localCommandHistory.add(command);
        }

        //remove double spaces
        String cleanedCommand = command.replaceAll("\\s\\s+", " ");

        //get the command name
        int commandEndIndex = cleanedCommand.indexOf(" ");
        String commandName;
        if (commandEndIndex >= 0) {
            commandName = cleanedCommand.substring(0, commandEndIndex);
        } else {
            commandName = cleanedCommand;
            commandEndIndex = commandName.length();
        }

        //remove command name from string
        String parameterPart = cleanedCommand.substring(commandEndIndex).trim();

        //get the parameters
        List<String> params = splitParameters(parameterPart);

        //get the command
        CommandInfo cmd = commandLookup.get(commandName, params.size());

        //check if the command is loaded
        if (cmd == null) {
            if (commandLookup.containsRow(commandName)) {
                addMessage("Incorrect number of parameters");
            } else {
                addMessage("Unknown command '" + commandName + "'");
            }

            return false;
        }

        if (cmd.isRunOnServer() && !networkSystem.getMode().isAuthority()) {
            String paramsStr = PARAMETER_JOINER.join(params);
            callingClient.send(new CommandEvent(commandName, paramsStr));
            return true;
        } else {
            try {
                String result = cmd.execute(params, callingClient);
                if (result != null && !result.isEmpty()) {
                    if (callingClient.exists()) {
                        callingClient.send(new ConsoleMessageEvent(result));
                    } else {
                        addMessage(result);
                    }
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
    }

    @Override
    public List<String> splitParameters(String paramStr) {
        String[] rawParams = paramStr.split(PARAM_SPLIT_REGEX);
        List<String> params = Lists.newArrayList();
        for (String s : rawParams) {
            if (s.trim().isEmpty()) {
                continue;
            }
            params.add(s);
        }
        return params;
    }

    /**
     * Get a group of commands by their name. These will vary by the number of parameters they accept
     *
     * @param name The name of the command.
     * @return An iterator over the commands.
     */
    @Override
    public Collection<CommandInfo> getCommand(String name) {
        return commandLookup.row(name).values();
    }

    /**
     * Get the list of all loaded commands.
     *
     * @return Returns the command list.
     */
    @Override
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
