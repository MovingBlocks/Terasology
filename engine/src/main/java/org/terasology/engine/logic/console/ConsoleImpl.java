// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.console;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.console.commandSystem.ConsoleCommand;
import org.terasology.engine.logic.console.commandSystem.exceptions.CommandExecutionException;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.gestalt.naming.Name;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.nui.FontColor;
import org.terasology.nui.FontUnderline;
import org.terasology.engine.utilities.collection.CircularBuffer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The console handles commands and messages.
 *
 */
public class ConsoleImpl implements Console {
    private static final String PARAM_SPLIT_REGEX = " (?=([^\"]*\"[^\"]*\")*[^\"]*$)";
    private static final int MAX_MESSAGE_HISTORY = 255;
    private static final int MAX_COMMAND_HISTORY = 30;
    private static final Logger logger = LoggerFactory.getLogger(ConsoleImpl.class);

    private final CircularBuffer<Message> messageHistory = CircularBuffer.create(MAX_MESSAGE_HISTORY);
    private final CircularBuffer<String> localCommandHistory = CircularBuffer.create(MAX_COMMAND_HISTORY);
    private final Map<Name, ConsoleCommand> commandRegistry = Maps.newHashMap();
    private final Set<ConsoleSubscriber> messageSubscribers = Sets.newHashSet();

    private final NetworkSystem networkSystem;
    private final Context context;

    public ConsoleImpl(Context context) {
        this.networkSystem = context.get(NetworkSystem.class);
        this.context = context;
    }

    /**
     * Registers a {@link ConsoleCommand}.
     *
     * @param command The command to be registered
     */
    @Override
    @SuppressWarnings("PMD.GuardLogStatement")
    public void registerCommand(ConsoleCommand command) {
        Name commandName = command.getName();

        if (commandRegistry.containsKey(commandName)) {
            logger.warn("Command with name '{}' already registered by class '{}', skipping '{}'", commandName,
                    commandRegistry.get(commandName).getSource().getClass().getCanonicalName(),
                    command.getSource().getClass().getCanonicalName());
        } else {
            commandRegistry.put(commandName, command);
            logger.debug("Command '{}' successfully registered for class '{}'.", commandName,
                    command.getSource().getClass().getCanonicalName());
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
     * @param message    The message to be added, as a string.
     * @param newLine    A boolean: True causes a newline character to be appended at the end of the message. False doesn't.
     */
    @Override
    public void addMessage(String message, boolean newLine) {
        addMessage(new Message(message, newLine));
    }

    /**
     * Adds a message to the console
     *
     * @param message    The message to be added, as a string.
     * @param type       The type of the message
     * @param newLine    A boolean: True causes a newline character to be appended at the end of the message. False doesn't.
     */
    @Override
    public void addMessage(String message, MessageType type, boolean newLine) {
        addMessage(new Message(message, type, newLine));
    }

    /**
     * Adds a message to the console
     *
     * @param message The message to be added
     */
    @Override
    public void addMessage(Message message) {
        String uncoloredText = FontUnderline.strip(FontColor.stripColor(message.getMessage()));
        logger.info("[{}] {}", message.getType(), uncoloredText); //NOPMD
        messageHistory.add(message);
        for (ConsoleSubscriber subscriber : messageSubscribers) {
            subscriber.onNewConsoleMessage(message);
        }
    }

    @Override
    public void removeMessage(Message message) {
        messageHistory.remove(message);
    }

    /**
     * Clears the console of all previous messages.
     */
    @Override
    public void clear() {
        messageHistory.clear();
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

        return Collections2.filter(messageHistory, input -> allowedTypes.contains(input.getType()));
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

        if (cc.local && !rawCommand.isEmpty() && (localCommandHistory.isEmpty() || !localCommandHistory.getLast().equals(rawCommand))) {
            localCommandHistory.add(rawCommand);
        }

        return execute(new Name(commandName), processedParameters, callingClient);
    }

    @Override
    public boolean execute(Name commandName, List<String> params, EntityRef callingClient) {
        if (commandName.isEmpty()) {
            return false;
        }

        //get the command
        ConsoleCommand cmd = getCommand(commandName);

        //check if the command is loaded
        if (cmd == null) {
            addErrorMessage("Unknown command '" + commandName + "'");
            return false;
        }

        String requiredPermission = cmd.getRequiredPermission();

        if (!clientHasPermission(callingClient, requiredPermission)) {
            callingClient.send(
                    new ErrorMessageEvent("You do not have enough permissions to execute this command (" + requiredPermission + ")."));
            return false;
        }

        if (params.size() < cmd.getRequiredParameterCount()) {
            callingClient.send(new ErrorMessageEvent("Please, provide required arguments marked by <>."));
            callingClient.send(new ConsoleMessageEvent(cmd.getUsage()));
            return false;
        }

        if (cmd.isRunOnServer() && !networkSystem.getMode().isAuthority()) {
            callingClient.send(new CommandEvent(commandName, params));
            return true;
        } else {
            try {
                String result = cmd.execute(params, callingClient);
                if (!Strings.isNullOrEmpty(result)) {
                    callingClient.send(new ConsoleMessageEvent(result));
                }

                return true;
            } catch (CommandExecutionException e) {
                Throwable cause = e.getCause();
                String causeMessage;
                if (cause != null) {
                    causeMessage = cause.getLocalizedMessage();
                    if (Strings.isNullOrEmpty(causeMessage)) {
                        causeMessage = cause.toString();
                    }
                } else {
                    causeMessage = e.getLocalizedMessage();
                }

                logger.error("An error occurred while executing a command", e);

                if (!Strings.isNullOrEmpty(causeMessage)) {
                    callingClient.send(new ErrorMessageEvent("An error occurred while executing command '"
                            + cmd.getName() + "': " + causeMessage));
                }
                return false;
            }
        }
    }

    private boolean clientHasPermission(EntityRef callingClient, String requiredPermission) {
        Preconditions.checkNotNull(callingClient, "The calling client must not be null!");

        PermissionManager permissionManager = context.get(PermissionManager.class);
        boolean hasPermission = true;

        if (permissionManager != null && requiredPermission != null
                && !requiredPermission.equals(PermissionManager.NO_PERMISSION)) {
            hasPermission = false;
            ClientComponent clientComponent = callingClient.getComponent(ClientComponent.class);

            if (permissionManager.hasPermission(clientComponent.clientInfo, requiredPermission)) {
                hasPermission = true;
            }
        }

        return hasPermission;
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
        return splitParameters(parameterPart);
    }

    private static List<String> splitParameters(String paramStr) {
        String[] rawParams = paramStr.split(PARAM_SPLIT_REGEX);
        List<String> params = Lists.newArrayList();
        for (String s : rawParams) {
            String param = s;

            if (param.trim().isEmpty()) {
                continue;
            }
            if (param.length() > 1 && param.startsWith("\"") && param.endsWith("\"")) {
                param = param.substring(1, param.length() - 1);
            }
            params.add(param);
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
    public ConsoleCommand getCommand(Name name) {
        return commandRegistry.get(name);
    }

    /**
     * Get the list of all loaded commands.
     *
     * @return Returns the command list.
     */
    @Override
    public Collection<ConsoleCommand> getCommands() {
        return commandRegistry.values();
    }
}
