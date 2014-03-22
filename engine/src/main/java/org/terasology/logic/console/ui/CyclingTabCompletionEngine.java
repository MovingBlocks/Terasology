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

import java.util.Collection;
import java.util.List;

import org.terasology.logic.console.Console;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.internal.CommandInfo;
import org.terasology.rendering.FontColor;
import org.terasology.rendering.nui.Color;
import org.terasology.utilities.CamelCaseMatcher;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * A text completion engine with cycle-through functionality 
 * @author Martin Steiger
 */
public class CyclingTabCompletionEngine implements TabCompletionEngine {

    private static final int MAX_CYCLES = 10;

    private final Console console;
    
    private int index;
    private final List<String> matchList = Lists.newArrayList();
    private String query;

    public CyclingTabCompletionEngine(Console console) {
        this.console = console;
    }

    @Override
    public String complete(String text) {
        
        if (!matchList.contains(text)) {
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
            Collection<String> matches = CamelCaseMatcher.getMatches(cmdQuery, commandNames);
    
            if (matches.isEmpty()) {
                return text;
            }
            
            if (matches.size() == 1) {
                return matches.iterator().next();
            } 
    
            if (matches.size() > MAX_CYCLES) {
                console.addMessage(new Message("Too many hits, please refine your search"));
                return text;
            }

            matchList.clear();
            matchList.addAll(matches);
            index = 0;
        }
        
        StringBuilder commandMatches = new StringBuilder();
        for (int i = 0; i < matchList.size(); i++) {
            if (commandMatches.length() != 0) {
                commandMatches.append(" ");
            }
            
            String name = matchList.get(i);
            
            if (index == i) {
                name = FontColor.getColored(name, Color.BLACK);
            }

            commandMatches.append(name);
        }
        console.addMessage(commandMatches.toString());
        
        index = (index + 1) % matchList.size(); 
        
        return matchList.get(index);
    }
}
