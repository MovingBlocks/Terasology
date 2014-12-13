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
package org.terasology.logic.health.commands;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.dynamic.Command;
import org.terasology.logic.console.dynamic.CommandParameter;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.health.HealthSystem;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class SetMaxHealthCommand extends Command {
    @In
    private HealthSystem healthSystem;

    public SetMaxHealthCommand() {
        super("setMaxHealth", true, "Set max health", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
                CommandParameter.single("max", Integer.class, true)
        };
    }

    public String execute(EntityRef sender, Integer max) {
        ClientComponent clientComp = sender.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            healthSystem.doHeal(clientComp.character, health.maxHealth, clientComp.character, health);
        }
        return "Max health set to " + max;
    }

    //TODO Implement the suggest method
}
