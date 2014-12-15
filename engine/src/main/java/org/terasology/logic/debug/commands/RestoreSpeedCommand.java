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

import org.terasology.asset.Asset;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.console.internal.Command;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.internal.CommandParameter;
import org.terasology.logic.health.HealthComponent;
import org.terasology.network.ClientComponent;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class RestoreSpeedCommand extends Command {
    public RestoreSpeedCommand() {
        super("restoreSpeed", true, "Restore normal speed values", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[0];
    }

    public String execute(EntityRef sender) {
        ClientComponent clientComp = sender.getComponent(ClientComponent.class);

        Asset<?> asset = Assets.get(new AssetUri("prefab:engine:player"));
        CharacterMovementComponent moveDefault = ((PojoPrefab) asset).getComponent(CharacterMovementComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null && moveDefault != null) {
            move.jumpSpeed = moveDefault.jumpSpeed;
            move.speedMultiplier = moveDefault.speedMultiplier;
            move.runFactor = moveDefault.runFactor;
            move.stepHeight = moveDefault.stepHeight;
            move.slopeFactor = moveDefault.slopeFactor;
            move.distanceBetweenFootsteps = moveDefault.distanceBetweenFootsteps;
            clientComp.character.saveComponent(move);
        }

        HealthComponent healthDefault = ((PojoPrefab) asset).getComponent(HealthComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if(health != null && healthDefault != null){
            health.fallingDamageSpeedThreshold = healthDefault.fallingDamageSpeedThreshold;
            health.horizontalDamageSpeedThreshold = healthDefault.horizontalDamageSpeedThreshold;
            health.excessSpeedDamageMultiplier = healthDefault.excessSpeedDamageMultiplier;
            clientComp.character.saveComponent(health);
        }

        return "Normal speed values restored";
    }
}
