// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console;

/**
 * Interface for subscribers to messages being added to the console
 */
@FunctionalInterface
public interface ConsoleSubscriber {

    /**
     * Called each time a message is added to the console
     *
     * @param message
     */
    void onNewConsoleMessage(Message message);
}
