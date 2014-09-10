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

package org.terasology.logic.chat;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.console.CoreMessageType;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.MessageEvent;
import org.terasology.network.ColorComponent;
import org.terasology.network.OwnerEvent;
import org.terasology.rendering.FontColor;

/**
 * A chat message
 *
 * @author Immortius
 */
@OwnerEvent
public class ChatMessageEvent implements MessageEvent {
    private String message;
    private EntityRef from;

    protected ChatMessageEvent() {
    }

    public ChatMessageEvent(String message, EntityRef from) {
        this.message = message;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public Message getFormattedMessage() {
        DisplayNameComponent displayInfo = from.getComponent(DisplayNameComponent.class);
        ColorComponent colorInfo = from.getComponent(ColorComponent.class);
        String playerName = (displayInfo != null) ? displayInfo.name : "Unknown";
        
        if (colorInfo != null) {
            playerName = FontColor.getColored(playerName, colorInfo.color);
        }
        
        return new Message(String.format("%s: %s", playerName, message), CoreMessageType.CHAT);
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "{from = " + from + ", message = '" + message + "'}";
    }

    public EntityRef getFrom() {
        return from;
    }
}
