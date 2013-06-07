package org.terasology.logic.console;

import org.terasology.entitySystem.event.AbstractEvent;
import org.terasology.network.ServerEvent;

/**
 * This event is used to convey commands marked as runOnServer to the server.
 * @author Immortius
 */
@ServerEvent
final class CommandEvent extends AbstractEvent {

    private String command;
    private String params;

    private CommandEvent() {
    }

    CommandEvent(String command, String params) {
        this.command = command;
        this.params = params;
    }

    public String getCommand() {
        return command;
    }

    public String getParams() {
        return params;
    }
}
