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
