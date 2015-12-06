/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.logic.notifications;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.CoreMessageType;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.MessageEvent;
import org.terasology.logic.players.PlayerUtil;
import org.terasology.network.OwnerEvent;

/**
 * A notification message
 */
@OwnerEvent
public class NotificationMessageEvent implements MessageEvent {
    private String message;
    private EntityRef from;

    protected NotificationMessageEvent() {
    }

    public NotificationMessageEvent(String message, EntityRef from) {
        this.message = message;
        this.from = from;
    }
    
    public static NotificationMessageEvent newJoinEvent(EntityRef client) {
        String playerName = PlayerUtil.getColoredPlayerName(client);
        
        return new NotificationMessageEvent("Player \"" + playerName + "\" has joined the game", client);
    }
    
    public static NotificationMessageEvent newLeaveEvent(EntityRef client) {
        String playerName = PlayerUtil.getColoredPlayerName(client);
        
        return new NotificationMessageEvent("Player \"" + playerName + "\" has left the game", client);
    }

    public String getMessage() {
        return message;
    }

    public EntityRef getFrom() {
        return from;
    }
    
    @Override
    public Message getFormattedMessage() {
        return new Message(message, CoreMessageType.NOTIFICATION);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{from = " + from + ", message = '" + message + "'}";
    }


}
