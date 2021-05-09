// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.suggesters;

import com.google.common.collect.Sets;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.console.commandSystem.CommandParameterSuggester;
import org.terasology.engine.logic.console.commandSystem.ConsoleCommand;
import org.terasology.engine.logic.console.Console;
import org.terasology.gestalt.naming.Name;

import java.util.Collection;
import java.util.Set;

/**
 */
public final class CommandNameSuggester implements CommandParameterSuggester<Name> {
    private final Console console;

    public CommandNameSuggester(Console console) {
        this.console = console;
    }

    @Override
    public Set<Name> suggest(EntityRef sender, Object... resolvedParameters) {
        Collection<ConsoleCommand> commands = console.getCommands();
        Set<Name> suggestions = Sets.newHashSetWithExpectedSize(commands.size());

        for (ConsoleCommand command : commands) {
            suggestions.add(command.getName());
        }

        return suggestions;
    }
}
