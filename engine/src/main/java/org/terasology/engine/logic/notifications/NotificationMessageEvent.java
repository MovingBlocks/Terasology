// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.notifications;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.console.CoreMessageType;
import org.terasology.engine.logic.console.Message;
import org.terasology.engine.logic.console.MessageEvent;
import org.terasology.engine.logic.players.PlayerUtil;
import org.terasology.context.annotation.API;
import org.terasology.engine.network.OwnerEvent;

/**
 * A notification message
 */

@API
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
