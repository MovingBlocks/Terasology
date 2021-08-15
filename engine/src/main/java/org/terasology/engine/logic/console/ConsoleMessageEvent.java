// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console;

import org.terasology.engine.network.OwnerEvent;

/**
 * Use to send console messages to a client
 *
 */
@OwnerEvent
public class ConsoleMessageEvent implements MessageEvent {

    private String message;

    protected ConsoleMessageEvent() {
    }

    public ConsoleMessageEvent(String message) {
        this.message = message;
    }

    @Override
    public Message getFormattedMessage() {
        return new Message(message, CoreMessageType.CONSOLE);
    }
}
