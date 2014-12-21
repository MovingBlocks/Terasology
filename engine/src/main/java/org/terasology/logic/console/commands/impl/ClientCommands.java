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
package org.terasology.logic.console.commands.impl;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.console.commands.referenced.Command;
import org.terasology.logic.console.commands.referenced.CommandParameter;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class ClientCommands extends BaseComponentSystem {

    @In
    private CameraTargetSystem cameraTargetSystem;

    @Command(shortDescription = "Reduce the player's health to zero", runOnServer = true)
    public void kill(EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            clientComp.character.send(new DestroyEvent(clientComp.character, EntityRef.NULL, EngineDamageTypes.DIRECT.get()));
        }
    }

    @Command(shortDescription = "Displays debug information on the target entity")
    public String debugTarget(EntityRef sender) {
        EntityRef cameraTarget = cameraTargetSystem.getTarget();
        return cameraTarget.toFullDescription();
    }

    @Command(shortDescription = "Sets the current world time in days")
    public String setWorldTime(EntityRef sender, @CommandParameter("day") float day) {
        WorldProvider world = CoreRegistry.get(WorldProvider.class);
        world.getTime().setDays(day);

        return "World time changed";
    }

    @Command(shortDescription = "Teleports you to a location", runOnServer = true)
    public String teleport(EntityRef sender, @CommandParameter("x") float x, @CommandParameter("y") float y, @CommandParameter("z") float z) {
        ClientComponent clientComp = sender.getComponent(ClientComponent.class);
        LocationComponent location = clientComp.character.getComponent(LocationComponent.class);
        if (location != null) {
            location.setWorldPosition(new Vector3f(x, y, z));
            clientComp.character.saveComponent(location);
        }

        return "Teleported to " + x + " " + y + " " + z;
    }
}
