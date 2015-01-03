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

import org.terasology.network.OwnerEvent;

/**
 * Use to send console messages to a client
 *
 * @author Immortius
 */
@OwnerEvent
public class ConsoleMessageEvent implements MessageEvent {

    private String message;
    private String messageType;

    protected ConsoleMessageEvent() {
    }

    public ConsoleMessageEvent(String message, String messageType) {
        this.message = message;
        this.messageType = messageType;
    }

    public ConsoleMessageEvent(Message message) {
        this(message.getMessage(), message.getType());
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getMessageType() {
        return messageType;
    }
}
