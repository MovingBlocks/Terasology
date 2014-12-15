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
package org.terasology.logic.console.internal.commands.core;

import org.terasology.engine.GameEngine;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.internal.Command;
import org.terasology.logic.console.internal.CommandParameter;
import org.terasology.logic.console.internal.CoreCommand;
import org.terasology.registry.CoreRegistry;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
@CoreCommand
public class ExitCommand extends Command {
    public ExitCommand() {
        super("exit", false, "Exits the game", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[0];
    }

    public void execute(EntityRef sender)
    {
        CoreRegistry.get(GameEngine.class).shutdown();
    }
}
