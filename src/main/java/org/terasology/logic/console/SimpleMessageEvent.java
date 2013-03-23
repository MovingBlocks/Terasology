package org.terasology.logic.console;

import org.terasology.network.OwnerEvent;

/**
 * Simple message event. Not ideal in general, but useful for quickly getting things working.
 *
 * @author Immortius
 */
@OwnerEvent
public class SimpleMessageEvent extends MessageEvent {
    private String message;

    protected SimpleMessageEvent() {
    }

    public SimpleMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String getFormattedMessage() {
        return message;
    }
}
