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

import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.console.internal.Command;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.internal.CommandParameter;
import org.terasology.network.ClientComponent;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class SetSpeedMultiplierCommand extends Command {
    public SetSpeedMultiplierCommand() {
        super("setSpeedMultiplier", true, "Set speed multiplier", "Set speedMultiplier");
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
                CommandParameter.single("amount", Float.class, true)
        };
    }

    public String execute(EntityRef sender, Float amount) {
        ClientComponent clientComp = sender.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            float oldSpeedMultipler = move.speedMultiplier;
            move.speedMultiplier = amount;
            clientComp.character.saveComponent(move);

            return "Speed multiplier set to " + amount + " (was " + oldSpeedMultipler + ")";
        }

        return "";
    }

    //TODO Implement the suggest method
}
