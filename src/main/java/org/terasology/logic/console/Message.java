package org.terasology.logic.console;

import org.newdawn.slick.Color;

/**
 * @author Immortius
 */
public class Message {
    private MessageType type = CoreMessageType.CONSOLE;
    private String message;

    public Message(String message) {
        this.message = message;
    }

    public Message(String message, MessageType type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public MessageType getType() {
        return type;
    }
}
