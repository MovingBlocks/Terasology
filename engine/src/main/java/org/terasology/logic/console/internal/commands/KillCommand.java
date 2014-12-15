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
import org.terasology.logic.console.internal.Command;
import org.terasology.logic.console.internal.CommandParameter;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.health.HealthComponent;
import org.terasology.network.ClientComponent;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class KillCommand extends Command {
    public KillCommand() {
        super("kill", true, "Reduce the player's health to zero", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
            /*CommandParameter.single("target", String.class, false)*/
        };
    }

    public String execute(EntityRef sender/*, String target*/)
    {
        ClientComponent clientComp = sender.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);

        if (health != null) {
            clientComp.character.send(new DestroyEvent(clientComp.character, EntityRef.NULL, EngineDamageTypes.DIRECT.get()));
        }

        return "Success";
    }

    //TODO Add the suggestion method
}
