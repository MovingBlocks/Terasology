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
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultiset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleMessageEvent;
import org.terasology.logic.console.ConsoleSubscriber;
import org.terasology.logic.console.CoreMessageType;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.MessageType;
import org.terasology.logic.console.dynamic.Command;
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
    private final TreeMultiset<ICommand> commands = TreeMultiset.create();
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

        if (isCommandRegistered(command)) {
            logger.warn("Command already registered the same name ({}), parameter count ({}) and varargs policy ({})," +
                    " skipping", commandName, command.getRequiredParameterCount(), command.endsWithVarargs());
        } else {
            commands.add(command);
            TreeMultiset<ICommand> cmdList = commandRegistry.get(commandName);

            if (cmdList == null) {
                cmdList = TreeMultiset.create();
                commandRegistry.put(commandName, cmdList);
            } else {
                logger.warn("Command {} already registered, conflicting {} and {}.",
                        commandName, cmdList.iterator().next().getClass().getCanonicalName(),
                        command.getClass().getCanonicalName());
            }

            cmdList.add(command);
        }
    }

    @Override
    public void dispose() {
        commands.clear();
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

        // remove quotation marks
        for (int i = 0; i < params.size(); i++) {
            String value = params.get(i);
            if (value.startsWith("\"") && value.endsWith("\"")) {
                params.set(i, value.substring(1, value.length() - 1));
            }
        }

        ClientComponent cc = callingClient.getComponent(ClientComponent.class);
        if (cc.local) {
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
        ICommand cmd = findBestCommand(commandName, params.size());

        //check if the command is loaded
        if (cmd == null) {
            if (commandRegistry.containsKey(commandName)) {
                addErrorMessage("Incorrect number of parameters. Try:");
                for (ICommand ci : commandRegistry.get(commandName)) {
                    addMessage(ci.getUsage());
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
                String msgText = e.getLocalizedMessage();
                if (msgText != null && !msgText.isEmpty()) {
                    if (callingClient.exists()) {
                        callingClient.send(new ConsoleMessageEvent(e.getLocalizedMessage()));
                    } else {
                        addErrorMessage(e.getLocalizedMessage());
                    }
                }
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
     * @return An array of commands with given name
     */
    @Override
    public ICommand[] getCommand(String name) {
        TreeMultiset<ICommand> correspondingCommands = commandRegistry.get(name);

        if (correspondingCommands == null) {
            return new Command[0];
        }

        return correspondingCommands.toArray(new ICommand[correspondingCommands.size()]);
    }

    /**
     * Get the list of all loaded commands.
     *
     * @return Returns the command list.
     */
    @Override
    public Collection<ICommand> getCommands() {
        return commands;
    }

    public ICommand findBestCommand(String name, int args) {
        TreeMultiset<ICommand> correspondingCommands = commandRegistry.get(name);

        if (correspondingCommands == null) {
            return null;
        }

        for (ICommand command : correspondingCommands) {
            if ((command.getRequiredParameterCount() == args && !command.endsWithVarargs())
                    || (command.getRequiredParameterCount() <= args && command.endsWithVarargs())) {
                return command;
            }
        }

        return null;
    }

    public boolean isCommandRegistered(ICommand cmd) {
        TreeMultiset<ICommand> registeredCommands = commandRegistry.get(cmd.getName());

        if (registeredCommands == null || registeredCommands.size() <= 0) {
            return false;
        }

        for (ICommand current : registeredCommands) {
            if (cmd.compareTo(current) == 0) {
                return true;
            }
        }

        return false;
    }
}
