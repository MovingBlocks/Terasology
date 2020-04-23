/*
 * Copyright 2017 MovingBlocks
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
import org.terasology.logic.console.CoreMessageType;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.MessageEvent;
import org.terasology.logic.players.PlayerUtil;
import org.terasology.network.OwnerEvent;

/**
 * Indicate that a message should be printed to the chat console.
 * <p>
 * Needs to be sent against a client entity, e.g., the targeted entity should have a {@link org.terasology.network.ClientComponent}.
 */
@OwnerEvent
public class ChatMessageEvent implements MessageEvent {
    private String message;
    private EntityRef from;

    protected ChatMessageEvent() {
    }

    /**
     * A chat message is associated with the entity that sent it.
     * <p>
     * This entity may often be a player, but it can be any entity with a {@link org.terasology.logic.common.DisplayNameComponent}.
     * If the entity also has a {@link org.terasology.network.ColorComponent}, the sender's name will be colorized.
     * <p>
     * Note: The exact representation of sender and message is up to the processing system.
     *
     * @param message the message to display in the chat console
     * @param from    the sender of this message (should have a display name component)
     */
    public ChatMessageEvent(String message, EntityRef from) {
        this.message = message;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public Message getFormattedMessage() {
        String playerName = PlayerUtil.getColoredPlayerName(from);

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
