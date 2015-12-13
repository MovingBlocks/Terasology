/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.logic.console;

import org.terasology.entitySystem.event.Event;
import org.terasology.naming.Name;
import org.terasology.network.ServerEvent;

import java.util.List;

/**
 * This event is used to convey commands marked as runOnServer to the server.
 *
 */
@ServerEvent
final class CommandEvent implements Event {

    private Name commandName;
    private List<String> parameters;

    CommandEvent() {
    }

    public CommandEvent(Name commandName, List<String> parameters) {
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
