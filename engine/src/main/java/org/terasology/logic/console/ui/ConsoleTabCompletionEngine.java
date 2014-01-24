/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.console.ui;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.internal.CommandInfo;
import org.terasology.utilities.CamelCaseMatcher;

import java.util.Collection;
import java.util.List;

/**
 * @author Immortius
 */
public class ConsoleTabCompletionEngine implements TabCompletionEngine {
    private Console console;

    public ConsoleTabCompletionEngine(Console console) {
        this.console = console;
    }

    @Override
    public String complete(String text) {
        String cmdQuery = text.trim();

        List<CommandInfo> commands = console.getCommandList();

        // Explicitly create a map String->CommandInfo if the CommandInfo is required later
        Collection<String> commandNames = Collections2.transform(commands, new Function<CommandInfo, String>() {

            @Override
            public String apply(CommandInfo input) {
                return input.getName();
            }
        });
        Collection<String> matches = CamelCaseMatcher.getMatches(cmdQuery, commandNames);

        //one match found
        if (matches.size() == 1) {
            return matches.iterator().next();
        } else if (matches.size() > 1) {
            //multiple matches found
            //add list of available commands
            String commandMatches = "";
            for (String cmd : matches) {
                if (!commandMatches.isEmpty()) {
                    commandMatches += " ";
                }

                commandMatches += cmd;
            }
            console.addMessage(commandMatches);
        }
        return text;
    }
}
