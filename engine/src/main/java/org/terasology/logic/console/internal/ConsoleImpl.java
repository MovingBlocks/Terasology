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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.*;
import org.terasology.network.Client;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.FontColor;
import org.terasology.utilities.collection.CircularBuffer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.reflections.ReflectionUtils.withModifier;

/**
 * The console handles commands and messages.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class ConsoleImpl implements Console {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleImpl.class);
    private static final String PARAM_SPLIT_REGEX = " (?=([^\"]*\"[^\"]*\")*[^\"]*$)";
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
        addMessage("Welcome to the wonderful world of Terasology!" + Message.NEW_LINE +
                Message.NEW_LINE +
                "Type 'help' to see a list with available commands or 'help \"<commandName>\"' for command details." + Message.NEW_LINE +
                "Text parameters should be in quotes, no commas needed between multiple parameters." + Message.NEW_LINE +
                "Commands are case-sensitive, block names and such are not." + Message.NEW_LINE +
                "You can use auto-completion by typing a partial command then hitting 'tab' - examples:" + Message.NEW_LINE +
                "'gh' + 'tab' = 'ghost'" + Message.NEW_LINE +
                "'lS' + 'tab' = 'listShapes' (camel casing abbreviated commands)" + Message.NEW_LINE);
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
                if (commandLookup.contains(command.getName(), command.getParameterCount())) {
                    logger.warn("Command already registered with same name and param count: {} : {}, skipping", command.getName(), command.getParameterCount());
                } else {
                    commands.add(command);
                    commandLookup.put(command.getName(), command.getParameterCount(), command);
                }
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

    private void addErrorMessage(String message) {
        addMessage(new Message(message, CoreMessageType.ERROR));
    }

    /**
     * Adds a message to the console
     *
     * @param message
     */
    @Override
    public void addMessage(Message message) {
        String uncoloredText = FontColor.stripColor(message.getMessage());
        logger.info("[{}] {}", message.getType(), uncoloredText);
        messageHistory.add(message);
        for (ConsoleSubscriber subscriber : messageSubscribers) {
            subscriber.onNewConsoleMessage(message);
        }
    }

    @Override
    public void removeMessage(Message message) {
        messageHistory.remove(message);
    }

    @Override
    public void replaceMessage(Message oldMsg, Message newMsg) {
        int idx = messageHistory.indexOf(oldMsg);
        if (idx >= 0) {
            messageHistory.set(idx, newMsg);
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
    public List<String> getPreviousCommands() {
        return ImmutableList.copyOf(localCommandHistory);
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

    @Override
    public boolean execute(String command, EntityRef callingClient) {

        // trim and remove double spaces
        String cleanedCommand = command.trim().replaceAll("\\s\\s+", " ");

        if (cleanedCommand.isEmpty()) {
            return false;
        }
        
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

        Client owner = networkSystem.getOwner(callingClient);
        if (networkSystem.getMode() == NetworkMode.NONE || (owner != null && owner.isLocal())) {
            localCommandHistory.add(command);
        }

        return execute(commandName, params, callingClient);
    }        

    @Override
    public boolean execute(String commandName, List<String> params, EntityRef callingClient) {
        
        if (commandName.isEmpty()) {
            return false;
        }
        
        //get the command
        CommandInfo cmd = commandLookup.get(commandName, params.size());

        //check if the command is loaded
        if (cmd == null) {
            if (commandLookup.containsRow(commandName)) {
                addErrorMessage("Incorrect number of parameters. Try:");
                for (CommandInfo ci : commandLookup.row(commandName).values()) {
                    addMessage(ci.getUsageMessage());
                }
            } else {
                addErrorMessage("Unknown command '" + commandName + "'");
            }

            return false;
        }

        if (cmd.isRunOnServer() && !networkSystem.getMode().isAuthority()) {
            callingClient.send(new CommandEvent(commandName, params));
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
            } catch (IllegalArgumentException e) {
                String msgText = e.getLocalizedMessage();
                if (msgText != null && !msgText.isEmpty()) {
                    addErrorMessage(e.getLocalizedMessage());
                }
                return false;

            } catch (Exception e) {
                addErrorMessage("Error executing command '" + commandName + "': " + e.getLocalizedMessage());

                logger.error("Failed to execute command", e);
                return false;
            }
        }
    }

    private List<String> splitParameters(String paramStr) {
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
