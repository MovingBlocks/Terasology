/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.logic.debug;

import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.CharacterTeleportEvent;
import org.terasology.logic.characters.MovementMode;
import org.terasology.logic.characters.events.SetMovementModeEvent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;

import java.util.Optional;

@RegisterSystem
public class MovementDebugCommands extends BaseComponentSystem {

    @Command(shortDescription = "Grants flight and movement through walls", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String ghost(@Sender EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        clientComp.character.send(new SetMovementModeEvent(MovementMode.GHOSTING));

        return "Ghost mode toggled";
    }

    @Command(shortDescription = "Grants flight", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String flight(@Sender EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        clientComp.character.send(new SetMovementModeEvent(MovementMode.FLYING));

        return "Flight mode toggled";
    }


    @Command(shortDescription = "Set speed multiplier", helpText = "Set speedMultiplier", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setSpeedMultiplier(@Sender EntityRef client, @CommandParam("amount") float amount) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            float oldSpeedMultipler = move.speedMultiplier;
            move.speedMultiplier = amount;
            clientComp.character.saveComponent(move);

            return "Speed multiplier set to " + amount + " (was " + oldSpeedMultipler + ")";
        }

        return "";
    }

    @Command(value = "teleport", shortDescription = "Teleports you to a location", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String teleportCommand(@Sender EntityRef sender, @CommandParam("x") float x, @CommandParam("y") float y, @CommandParam("z") float z) {
        ClientComponent clientComp = sender.getComponent(ClientComponent.class);
        clientComp.character.send(new CharacterTeleportEvent(new Vector3f(x, y, z)));
        return "Teleporting  to " + x + " " + y + " " + z;
    }

    @Command(shortDescription = "Set jump speed", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setJumpSpeed(@Sender EntityRef client, @CommandParam("amount") float amount) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            float oldSpeed = move.jumpSpeed;
            move.jumpSpeed = amount;
            clientComp.character.saveComponent(move);

            return "Jump speed set to " + amount + " (was " + oldSpeed + ")";
        }

        return "";
    }

    @Command(shortDescription = "Show your Movement stats",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String showMovement(@Sender EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            return "Your SpeedMultiplier:" + move.speedMultiplier + " JumpSpeed:"
                    + move.jumpSpeed + " SlopeFactor:"
                    + move.slopeFactor + " RunFactor:" + move.runFactor;
        }
        return "You're dead I guess.";
    }

    @Command(shortDescription = "Go really fast", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String hspeed(@Sender EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            move.speedMultiplier = 10f;
            move.jumpSpeed = 24f;
            clientComp.character.saveComponent(move);

            return "High-speed mode activated";
        }

        return "";
    }

    @Command(shortDescription = "Jump really high", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String hjump(@Sender EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            move.jumpSpeed = 75f;
            clientComp.character.saveComponent(move);

            return "High-jump mode activated";
        }

        return "";
    }

    @Command(shortDescription = "Restore normal speed values", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String restoreSpeed(@Sender EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);

        Optional<Prefab> prefab = Assets.get(new ResourceUrn("engine:player"), Prefab.class);
        CharacterMovementComponent moveDefault = prefab.get().getComponent(CharacterMovementComponent.class);
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

        return "Normal speed values restored";
    }

    @Command(shortDescription = "Toggles the maximum slope the player can walk up", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String sleigh(@Sender EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            float oldFactor = move.slopeFactor;
            if (move.slopeFactor > 0.7f) {
                move.slopeFactor = 0.6f;
            } else {
                move.slopeFactor = 0.9f;
            }
            clientComp.character.saveComponent(move);
            return "Slope factor is now " + move.slopeFactor + " (was " + oldFactor + ")";
        }
        return "";
    }

    @Command(shortDescription = "Sets the height the player can step up", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String stepHeight(@Sender EntityRef client, @CommandParam("height") float amount) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            float prevStepHeight = move.stepHeight;
            move.stepHeight = amount;
            clientComp.character.saveComponent(move);

            return "Ground friction set to " + amount + " (was " + prevStepHeight + ")";
        }

        return "";
    }
}
