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
package org.terasology.logic.console.suggesters;

import com.google.common.collect.Sets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.CommandParameterSuggester;
import org.terasology.logic.console.commandSystem.ConsoleCommand;
import org.terasology.naming.Name;

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
