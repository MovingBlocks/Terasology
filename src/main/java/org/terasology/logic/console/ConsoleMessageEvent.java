package org.terasology.logic.console;

import org.terasology.network.OwnerEvent;

/**
 * Use to send console messages to a client
 * @author Immortius
 */
@OwnerEvent
public class ConsoleMessageEvent extends MessageEvent {

    private String message;

    protected ConsoleMessageEvent() {
    }

    public ConsoleMessageEvent(String message) {
        this.message = message;
    }

    @Override
    public Message getFormattedMessage() {
        return new Message(message, CoreMessageType.CONSOLE);
    }
}
