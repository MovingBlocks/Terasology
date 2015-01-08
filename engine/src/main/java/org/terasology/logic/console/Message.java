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
     * JAVA8: consider moving this to the {@link Console} interface as soon as Java 8 is around
     */
    public static final String NEW_LINE = "\n";
    public static final String TYPE_INFO = "info";
    public static final String TYPE_WARNING = "warning";
    public static final String TYPE_ERROR = "error";
    public static final String TYPE_NOTIFICATION = "notification";
    public static final String TYPE_CHAT = "chat";

    private final String message;
    private final String type;

    public Message(String message, String type) {
        this.message = message;
        this.type = type;
    }

    /**
     * Creates a new Message with the 'info' type
     */
    public Message(String message) {
        this(message, TYPE_INFO);
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] '%s'", type, message); 
    }
}
