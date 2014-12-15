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
package org.terasology.logic.debug.commands;

import org.terasology.logic.characters.MovementMode;
import org.terasology.logic.characters.events.SetMovementModeEvent;
import org.terasology.logic.console.internal.Command;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.internal.CommandParameter;
import org.terasology.network.ClientComponent;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class GhostCommand extends Command {
    public GhostCommand() {
        super("ghost", true, "Grants flight and movement through walls", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[0];
    }

    public String execute(EntityRef sender) {
        ClientComponent clientComp = sender.getComponent(ClientComponent.class);
        clientComp.character.send(new SetMovementModeEvent(MovementMode.GHOSTING));

        return "Ghost mode toggled";
    }
}
