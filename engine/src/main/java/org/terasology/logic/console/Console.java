// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.console.commandSystem.ConsoleCommand;
import org.terasology.naming.Name;

import java.util.Collection;
import java.util.List;

/**
 */
public interface Console {

    String NEW_LINE = "\n";

    /**
     * Registers a {@link ConsoleCommand}.
     *
     * @param command   The command to be registered
     */
    void registerCommand(ConsoleCommand command);

    void dispose();

    /**
     * Adds a message to the console (as a CoreMessageType.CONSOLE message)
     *
     * @param message   The message to be added, as a string.
     */
    void addMessage(String message);

    /**
     * Adds a message to the console
     *
     * @param message   The message to be added, as a string.
     * @param type      The type of the message
     */
    void addMessage(String message, MessageType type);

    /**
     * Adds a message to the console
     *
     * @param message   The message to be added
     */
    void addMessage(Message message);

    /**
     * Adds a message to the console (as a CoreMessageType.CONSOLE message)
     *
     * @param message    The message to be added, as a string.
     * @param newLine    A boolean: True causes a newline character to be appended at the end of the message. False doesn't.
     */
    void addMessage(String message, boolean newLine);

    /**
     * Adds a message to the console (as a CoreMessageType.CONSOLE message)
     *
     * @param message    The message to be added, as a string.
     * @param type       The type of the message
     * @param newLine    A boolean: True causes a newline character to be appended at the end of the message. False doesn't.
     */
    void addMessage(String message, MessageType type, boolean newLine);
    /**
     * @return An iterator over all messages in the console
     */
    Iterable<Message> getMessages();

    /**
     * @param types a set of allowed message types
     * @return All messages in the console, filtered by message type (OR)
     */
    Iterable<Message> getMessages(MessageType... types);

    List<String> getPreviousCommands();

    /**
     * Subscribe for notification of all messages added to the console
     *
     * @param subscriber
     */
    void subscribe(ConsoleSubscriber subscriber);

    /**
     * Unsubscribe from receiving notification of messages being added to the console
     *
     * @param subscriber
     */
    void unsubscribe(ConsoleSubscriber subscriber);

    /**
     * Execute a command and log to local command history.
     *
     * @param command The whole string of the command including the command name and the optional parameters.
     * @return Returns true if the command was executed successfully.
     */
    boolean execute(String command, EntityRef callingClient);

    /**
     * Execute a command
     *
     * @param commandName the command name
     * @param params a list of parameters (no quotes!)
     * @param callingClient the resonsible client entity
     * @return true if successful
     */
    boolean execute(Name commandName, List<String> params, EntityRef callingClient);

    /**
     * @param rawCommand Command entered in the UI
     * @return Command name
     */
    String processCommandName(String rawCommand);

    /**
     * @param rawCommand Command entered in the UI
     * @return String command arguments
     */
    List<String> processParameters(String rawCommand);

    /**
     * Get a group of commands by their name. These will vary by the number of parameters they accept
     *
     * @param name The name of the command.
     * @return An iterator over the commands.
     */
    ConsoleCommand getCommand(Name name);

    /**
     * Get the collection of all loaded commands.
     *
     * @return Returns the commands.
     */
    Collection<ConsoleCommand> getCommands();

    /**
     * If <code>oldMsg</code> does not exist, the method does nothing.
     * @param oldMsg the old message
     * @param newMsg the new message
     */
    void replaceMessage(Message oldMsg, Message newMsg);

    /**
     * If the message does not exist, the method does nothing.
     * @param message the message to remove
     */
    void removeMessage(Message message);

    /**
     * Clears the console of all previous messages.
     */
    void clear();
}
