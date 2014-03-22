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
 * @author Immortius
 */
public class Message {
    /**
     * A newline constant for the console
     * TODO: consider moving this to the {@link Console} interface as soon as Java 8 is around
     */
    public static final String NEW_LINE = "\n";

    private final MessageType type;
    private final String message;

    public Message(String message) {
        this.message = message;
        this.type = CoreMessageType.CONSOLE;
    }

    public Message(String message, MessageType type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public MessageType getType() {
        return type;
    }
}
