package org.terasology.events.messaging;

import org.terasology.components.DisplayInformationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.network.NetworkEvent;
import org.terasology.network.OwnerEvent;
import org.terasology.network.Replicate;

/**
 * @author Immortius
 */
@OwnerEvent
public class ChatMessageEvent extends NetworkEvent {
    @Replicate
    private String message;
    @Replicate
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
