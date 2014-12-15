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
import org.terasology.logic.location.LocationComponent;
import org.terasology.network.ClientComponent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class TeleportCommand extends Command {
    public TeleportCommand() {
        super("teleport", true, "Teleports you to a location", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
                CommandParameter.single("x", Float.class, true),
                CommandParameter.single("y", Float.class, true),
                CommandParameter.single("z", Float.class, true)/*,
                CommandParameter.array("targets", String.class, false)*/ //TODO Add an option to teleport others
        };
    }

    public String execute(EntityRef sender, Float x, Float y, Float z/*, String[] targets*/) {
        ClientComponent clientComp = sender.getComponent(ClientComponent.class);
        LocationComponent location = clientComp.character.getComponent(LocationComponent.class);

        if (location != null) {
            location.setWorldPosition(new Vector3f(x, y, z));
            clientComp.character.saveComponent(location);
        }

        return "Teleported to [" + x + "; " + y + "; " + z + "].";
    }

    //TODO Add the suggestion method
}
