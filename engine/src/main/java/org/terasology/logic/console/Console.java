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
package org.terasology.logic.console;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.internal.CommandInfo;

import java.util.Collection;
import java.util.List;

/**
 * @author Immortius
 */
public interface Console {
    /**
     * Registers an object as a command provider - all methods annotated with @Command will be made available on the console.
     *
     * @param provider
     */
    void registerCommandProvider(Object provider);

    void dispose();

    /**
     * Adds a message to the console (as a CoreMessageType.CONSOLE message)
     *
     * @param message
     */
    void addMessage(String message);

    /**
     * Adds a message to the console
     *
     * @param message
     * @param type
     */
    void addMessage(String message, MessageType type);

    /**
     * Adds a message to the console
     *
     * @param message
     */
    void addMessage(Message message);

    /**
     * @return An iterator over all messages in the console
     */
    Iterable<Message> getMessages();

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
    boolean execute(String commandName, List<String> params, EntityRef callingClient);

    List<String> splitParameters(String paramStr);

    /**
     * Get a group of commands by their name. These will vary by the number of parameters they accept
     *
     * @param name The name of the command.
     * @return An iterator over the commands.
     */
    Collection<CommandInfo> getCommand(String name);

    /**
     * Get the list of all loaded commands.
     *
     * @return Returns the command list.
     */
    List<CommandInfo> getCommandList();

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
}
