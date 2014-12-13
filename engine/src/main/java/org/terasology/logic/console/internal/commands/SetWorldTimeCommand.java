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
import org.terasology.logic.console.dynamic.Command;
import org.terasology.logic.console.dynamic.CommandParameter;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.WorldProvider;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class SetWorldTimeCommand extends Command {
    public SetWorldTimeCommand() {
        super("setWorldTime", false, "Sets the current world time in days", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
            CommandParameter.single("day", Float.class, true)
        };
    }

    public String execute(EntityRef sender, Float day)
    {
        WorldProvider world = CoreRegistry.get(WorldProvider.class);
        world.getTime().setDays(day);

        return "World time changed";
    }

    //TODO Add the suggestion method
}
