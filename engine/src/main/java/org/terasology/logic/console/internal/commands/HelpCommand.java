/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.console.internal.commands;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleColors;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.dynamic.Command;
import org.terasology.logic.console.dynamic.CommandParameter;
import org.terasology.logic.console.dynamic.ICommand;
import org.terasology.registry.In;
import org.terasology.rendering.FontColor;

import java.util.Collection;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class HelpCommand extends Command {
    @In
    private Console console;

    public HelpCommand() {
        super("help", false, "Command help", "Prints out short descriptions for all available commands," +
                " or a longer help text if a command is provided.");
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[]{
                CommandParameter.single("command", String.class, false)
        };
    }

    public String execute(EntityRef sender, String command) {
        if (command == null) {
            StringBuilder msg = new StringBuilder();
            Collection<ICommand> commands = console.getCommands();

            for (ICommand cmd : commands) {
                if (!msg.toString().isEmpty()) {
                    msg.append(Message.NEW_LINE);
                }

                msg.append(FontColor.getColored(cmd.getUsage(), ConsoleColors.COMMAND));
                msg.append(" - ");
                msg.append(cmd.getDescription());
            }

            return msg.toString();
        } else {
            ICommand cmd = console.getCommand(command);
            if (cmd == null) {
                return "No help available for command '" + command + "'. Unknown command.";
            } else {
                StringBuilder msg = new StringBuilder();

                msg.append("=====================================================================================================================");
                msg.append(Message.NEW_LINE);
                msg.append(cmd.getUsage());
                msg.append(Message.NEW_LINE);
                msg.append("=====================================================================================================================");
                msg.append(Message.NEW_LINE);
                if (!cmd.getHelpText().isEmpty()) {
                    msg.append(cmd.getHelpText());
                    msg.append(Message.NEW_LINE);
                    msg.append("=====================================================================================================================");
                    msg.append(Message.NEW_LINE);
                } else if (!cmd.getDescription().isEmpty()) {
                    msg.append(cmd.getDescription());
                    msg.append(Message.NEW_LINE);
                    msg.append("=====================================================================================================================");
                    msg.append(Message.NEW_LINE);
                }

                return msg.toString();
            }
        }
    }

    public String[] suggest(EntityRef sender, String commandName) {
        if (commandName != null) {
            return null;
        }

        Collection<ICommand> commands = console.getCommands();
        String[] suggestions = new String[commands.size()];
        int i = 0;

        for (ICommand command : commands) {
            suggestions[i] = command.getName();
            i++;
        }

        return suggestions;
    }
}
