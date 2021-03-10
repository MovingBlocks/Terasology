// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.chat;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.console.CoreMessageType;
import org.terasology.engine.logic.console.Message;
import org.terasology.engine.logic.console.MessageEvent;
import org.terasology.engine.logic.players.PlayerUtil;
import org.terasology.engine.network.OwnerEvent;

/**
 * Indicate that a message should be printed to the chat console.
 * <p>
 * Needs to be sent against a client entity, e.g., the targeted entity should have a {@link org.terasology.engine.network.ClientComponent}.
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
     * This entity may often be a player, but it can be any entity with a {@link DisplayNameComponent}.
     * If the entity also has a {@link org.terasology.engine.network.ColorComponent}, the sender's name will be colorized.
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
