/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.events.messaging;

import org.terasology.components.DisplayInformationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.console.MessageEvent;
import org.terasology.network.OwnerEvent;

/**
 * @author Immortius
 */
@OwnerEvent
public class ChatMessageEvent extends MessageEvent {
    private String message;
    private EntityRef from;

    private ChatMessageEvent() {
    }

    public ChatMessageEvent(String message, EntityRef from) {
        this.message = message;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public String getFormattedMessage() {
        DisplayInformationComponent displayInfo = from.getComponent(DisplayInformationComponent.class);
        return String.format("%s: %s", (displayInfo != null) ? displayInfo.name : "Unknown", message);
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "{from = " + from + ", message = '" + message + "'}";
    }

    public EntityRef getFrom() {
        return from;
    }
}
