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
package org.terasology.logic.console.internal;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.registry.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleColors;
import org.terasology.logic.console.MessageEvent;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.rendering.FontColor;

import java.util.Collection;
import java.util.List;

/**
 * @author Immortius
 */
@RegisterSystem
public class ConsoleSystem implements ComponentSystem {

    @In
    private Console console;

    @In
    private NetworkSystem networkSystem;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Command(shortDescription = "General help", helpText = "Prints out short descriptions for all available commands.")
    public String help() {
        StringBuilder msg = new StringBuilder();
        List<CommandInfo> commands = console.getCommandList();
        for (CommandInfo cmd : commands) {
            if (!msg.toString().isEmpty()) {
                msg.append("\n");
            }
            msg.append(FontColor.toChar(ConsoleColors.COMMAND));
            msg.append(cmd.getUsageMessage());
            msg.append(FontColor.getReset());
            msg.append(" - ");
            msg.append(cmd.getShortDescription());
        }
        return msg.toString();
    }

    @Command(shortDescription = "Detailed help on a command")
    public String help(@CommandParam("command") String command) {
        Collection<CommandInfo> cmdCollection = console.getCommand(command);
        if (cmdCollection.isEmpty()) {
            return "No help available for command '" + command + "'. Unknown command.";
        } else {
            StringBuilder msg = new StringBuilder();

            for (CommandInfo cmd : cmdCollection) {
                msg.append("=====================================================================================================================");
                msg.append(System.lineSeparator());
                msg.append(cmd.getUsageMessage());
                msg.append(System.lineSeparator());
                msg.append("=====================================================================================================================");
                msg.append(System.lineSeparator());
                if (!cmd.getHelpText().isEmpty()) {
                    msg.append(cmd.getHelpText());
                    msg.append(System.lineSeparator());
                    msg.append("=====================================================================================================================");
                    msg.append(System.lineSeparator());
                } else if (!cmd.getShortDescription().isEmpty()) {
                    msg.append(cmd.getShortDescription());
                    msg.append(System.lineSeparator());
                    msg.append("=====================================================================================================================");
                    msg.append(System.lineSeparator());
                }
                msg.append(System.lineSeparator());
            }
            return msg.toString();
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onMessage(MessageEvent event, EntityRef entity) {
        ClientComponent client = entity.getComponent(ClientComponent.class);
        if (client.local) {
            console.addMessage(event.getFormattedMessage());
        }
    }

    @ReceiveEvent(components = ClientComponent.class, netFilter = RegisterMode.AUTHORITY)
    public void onCommand(CommandEvent event, EntityRef entity) {
        List<String> params = console.splitParameters(event.getParams());
        for (CommandInfo cmd : console.getCommand(event.getCommand())) {
            if (cmd.getParameterCount() == params.size() && cmd.isRunOnServer()) {
                console.execute(event.getCommand() + " " + event.getParams(), entity);
                break;
            }
        }
    }
}
