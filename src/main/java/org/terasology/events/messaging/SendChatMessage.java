package org.terasology.events.messaging;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.network.NetworkEvent;
import org.terasology.network.Replicate;
import org.terasology.network.ServerEvent;

/**
 * @author Immortius
 */
@ServerEvent
public class SendChatMessage extends NetworkEvent {
    @Replicate
    private String message;

    private SendChatMessage() {}

    public SendChatMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{message = '" + message + "'}";
    }
}
