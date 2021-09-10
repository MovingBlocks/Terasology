// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console;

import org.terasology.engine.network.OwnerEvent;

/**
 * Use to send error messages to a client
 *
 */
@OwnerEvent
public class ErrorMessageEvent implements MessageEvent {

    private String message = "";

    // Default constructor for serialization
    ErrorMessageEvent() {
    }

    public ErrorMessageEvent(String message) {
        this.message = message;
    }

    @Override
    public Message getFormattedMessage() {
        return new Message(message, CoreMessageType.ERROR);
    }
}
