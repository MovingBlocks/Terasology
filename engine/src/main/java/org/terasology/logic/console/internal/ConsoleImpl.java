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
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.*;
import org.terasology.logic.console.dynamic.CommandExecutionException;
import org.terasology.logic.console.dynamic.ICommand;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.FontColor;
import org.terasology.utilities.collection.CircularBuffer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
    private final CircularBuffer<Message> messageHistory = CircularBuffer.create(MAX_MESSAGE_HISTORY);
    private final CircularBuffer<String> localCommandHistory = CircularBuffer.create(MAX_COMMAND_HISTORY);
    private final CommandRegistry commandRegistry = new CommandRegistry();
    private final Set<ConsoleSubscriber> messageSubscribers = Sets.newSetFromMap(new MapMaker().weakKeys().<ConsoleSubscriber, Boolean>makeMap());

    private NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);

    /**
     * Registers a {@link ICommand}.
     *
     * @param command The command to be registered
     */
    @Override
    public void registerCommand(ICommand command) {
        String commandName = command.getName();

        logger.debug("Registering {}...", command.getName());

        if (commandRegistry.containsKey(commandName)) {
            logger.warn("Command with name '{}' already registered by class '{}', skipping '{}'",
                    commandName, commandRegistry.get(commandName).getClass().getCanonicalName(),
                    command.getClass().getCanonicalName());
        } else {
            commandRegistry.put(commandName, command);
            logger.info("Command '{}' successfully registered for class '{}'.", commandName,
                    command.getClass().getCanonicalName());
        }
    }

    @Override
    public void dispose() {
        commandRegistry.clear();
        messageHistory.clear();
    }

    /**
     * Adds a message to the console (as a CoreMessageType.CONSOLE message)
     *
     * @param message The message content
     */
    @Override
    public void addMessage(String message) {
        addMessage(new Message(message));
    }

    /**
     * Adds a message to the console
     *
     * @param message The content of the message
     * @param type    The type of the message
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
     * @param message The message to be added
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
    public Iterable<Message> getMessages(MessageType... types) {
        final List<MessageType> allowedTypes = Arrays.asList(types);

        // JAVA8: this can be simplified using Stream.filter()
        return Collections2.filter(messageHistory, new Predicate<Message>() {

            @Override
            public boolean apply(Message input) {
                return allowedTypes.contains(input.getType());
            }
        });
    }

    @Override
    public List<String> getPreviousCommands() {
        return ImmutableList.copyOf(localCommandHistory);
    }

    /**
     * Subscribe for notification of all messages added to the console
     */
    @Override
    public void subscribe(ConsoleSubscriber subscriber) {
        this.messageSubscribers.add(subscriber);
    }

    /**
     * Unsubscribe from receiving notification of messages being added to the console
     */
    @Override
    public void unsubscribe(ConsoleSubscriber subscriber) {
        this.messageSubscribers.remove(subscriber);
    }

    @Override
    public boolean execute(String rawCommand, EntityRef callingClient) {
        String commandName = processCommandName(rawCommand);
        List<String> processedParameters = processParameters(rawCommand);

        ClientComponent cc = callingClient.getComponent(ClientComponent.class);
        if (cc.local) {
            localCommandHistory.add(rawCommand);
        }

        return execute(commandName, processedParameters, callingClient);
    }

    @Override
    public boolean execute(String commandName, List<String> params, EntityRef callingClient) {
        if (commandName.isEmpty()) {
            return false;
        }

        //get the command
        ICommand cmd = getCommand(commandName);

        //check if the command is loaded
        if (cmd == null) {
            addErrorMessage("Unknown command '" + commandName + "'");
            return false;
        }

        if(params.size() < cmd.getRequiredParameterCount()) {
            addErrorMessage("Please, provide required arguments marked by <>.");
            addMessage(cmd.getUsage());
            return false;
        }

        if (cmd.isRunOnServer() && !networkSystem.getMode().isAuthority()) {
            callingClient.send(new CommandEvent(commandName, params));
            return true;
        } else {
            try {
                String result = cmd.executeRaw(params, callingClient);
                if (result != null && !result.isEmpty()) {
                    if (callingClient.exists()) {
                        callingClient.send(new ConsoleMessageEvent(result));
                    } else {
                        addMessage(result);
                    }
                }

                return true;
            } catch (CommandExecutionException e) {
                Throwable cause = e.getCause();
                String causeMessage = cause.getLocalizedMessage();

                e.printStackTrace();

                if (causeMessage == null || causeMessage.isEmpty()) {
                    causeMessage = cause.getMessage();

                    if (causeMessage == null || causeMessage.isEmpty()) {
                        causeMessage = cause.toString();

                        if (causeMessage == null || causeMessage.isEmpty()) {
                            return false;
                        }
                    }
                }

                String errorReport = FontColor.getColored("An error occurred while executing command '"
                                                          + commandName + "': " + causeMessage, ConsoleColors.ERROR);

                if (callingClient.exists()) {
                    callingClient.send(new ConsoleMessageEvent(errorReport));
                } else {
                    addErrorMessage(errorReport);
                }

                return false;
            }
        }
    }

    private static String cleanCommand(String rawCommand) {
        // trim and remove double spaces
        return rawCommand.trim().replaceAll("\\s\\s+", " ");
    }

    @Override
    public String processCommandName(String rawCommand) {
        String cleanedCommand = cleanCommand(rawCommand);
        int commandEndIndex = cleanedCommand.indexOf(" ");

        if (commandEndIndex >= 0) {
            return cleanedCommand.substring(0, commandEndIndex);
        } else {
            return cleanedCommand;
        }
    }

    @Override
    public List<String> processParameters(String rawCommand) {
        String cleanedCommand = cleanCommand(rawCommand);
        //get the command name
        int commandEndIndex = cleanedCommand.indexOf(" ");

        if (commandEndIndex < 0) {
            commandEndIndex = cleanedCommand.length();
        }

        //remove command name from string
        String parameterPart = cleanedCommand.substring(commandEndIndex).trim();

        //get the parameters
        List<String> params = splitParameters(parameterPart);

        // remove quotation marks
        for (int i = 0; i < params.size(); i++) {
            String value = params.get(i);
            if (value.startsWith("\"") && value.endsWith("\"")) {
                params.set(i, value.substring(1, value.length() - 1));
            }
        }

        return params;
    }

    private static List<String> splitParameters(String paramStr) {
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
     * @return An array of commands with given name
     */
    @Override
    public ICommand getCommand(String name) {
        return commandRegistry.get(name);
    }

    /**
     * Get the list of all loaded commands.
     *
     * @return Returns the command list.
     */
    @Override
    public Collection<ICommand> getCommands() {
        return commandRegistry.values();
    }
}
