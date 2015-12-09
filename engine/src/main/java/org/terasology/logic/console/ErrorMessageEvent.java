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
