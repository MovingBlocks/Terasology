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
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.terasology.logic.console.Console;
import org.terasology.logic.console.internal.CommandInfo;
import org.terasology.utilities.CamelCaseMatcher;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A stateless completion engine that returns the list of all matching commands
 * @author Martin Steiger
 * @author Immortius
 */
public class StatelessTabCompletionEngine implements TabCompletionEngine {
    private final Console console;

    public StatelessTabCompletionEngine(Console console) {
        this.console = console;
    }

    @Override
    public String complete(String text) {
        String cmdQuery = text.trim();

        List<CommandInfo> commands = console.getCommandList();

        // TODO: (Java8) replace with lamba expression
        // Explicitly create a map String->CommandInfo if the CommandInfo is required later
        Collection<String> commandNames = Collections2.transform(commands, new Function<CommandInfo, String>() {

            @Override
            public String apply(CommandInfo input) {
                return input.getName();
            }
        });
        
        List<String> matches = Lists.newArrayList(CamelCaseMatcher.getMatches(cmdQuery, commandNames));
        Collections.sort(matches);
        
        //one match found
        if (matches.size() == 1) {
            return matches.iterator().next();
        } else if (matches.size() > 1) {
            //multiple matches found
            //add list of available commands
            String commandMatches = Joiner.on(' ').join(matches);
            console.addMessage(commandMatches.toString());
        }
        return text;
    }
    
    @Override
    public void reset() {
        // nothing to do
    }
}
