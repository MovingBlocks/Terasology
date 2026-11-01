// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console;

import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.gestalt.naming.Name;

import java.util.List;

/**
 * This event is used to convey commands marked as runOnServer to the server.
 *
 */
@ServerEvent
public final class CommandEvent implements Event {

    private Name commandName;
    private List<String> parameters;

    CommandEvent() {
    }

    CommandEvent(Name commandName, List<String> parameters) {
        this.commandName = commandName;
        this.parameters = parameters;
    }

    public Name getCommandName() {
        return commandName;
    }

    public List<String> getParameters() {
        return parameters;
    }
}
