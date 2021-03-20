// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console;

/**
 */
public class Message {

    private final MessageType type;
    private final String message;
    private final boolean newLine;

    public Message(String message) {
        this.message = message;
        this.type = CoreMessageType.CONSOLE;
        this.newLine = true;
    }

    public Message(String message, MessageType type) {
        this.message = message;
        this.type = type;
        this.newLine = true;
    }

    public Message(String message, MessageType type, boolean newLine) {
        this.message = message;
        this.type = type;
        this.newLine = newLine;
    }

    public Message(String message, boolean newLine) {
        this.message = message;
        this.type = CoreMessageType.CONSOLE;
        this.newLine = newLine;
    }

    public String getMessage() {
        return message;
    }

    public MessageType getType() {
        return type;
    }

    public boolean hasNewLine()
    {
        return newLine;
    }

    @Override
    public String toString() {
        return String.format("[%s] '%s'", type, message); 
    }
}
