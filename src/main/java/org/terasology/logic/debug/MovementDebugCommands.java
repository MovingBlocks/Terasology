/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.events.ToggleNoClipEvent;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;
import org.terasology.logic.health.HealthComponent;
import org.terasology.network.ClientComponent;

/**
 * @author Immortius
 */
@RegisterSystem
public class MovementDebugCommands implements ComponentSystem {

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Command(shortDescription = "Grants flight and movement through walls", runOnServer = true)
    public void ghost(EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        clientComp.character.send(new ToggleNoClipEvent());
    }

    @Command(shortDescription = "Set ground friction", runOnServer = true)
    public void setGroundFriction(@CommandParam("amount") float amount, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            move.groundFriction = amount;
            clientComp.character.saveComponent(move);
        }
    }

    @Command(shortDescription = "Set max ground speed", helpText = "Set maxGroundSpeed", runOnServer = true)
    public void setMaxGroundSpeed(@CommandParam("amount") float amount, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            move.maxGroundSpeed = amount;
            clientComp.character.saveComponent(move);
        }
    }

    @Command(shortDescription = "Set max ghost speed", runOnServer = true)
    public void setMaxGhostSpeed(@CommandParam("amount") float amount, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            move.maxGhostSpeed = amount;
            clientComp.character.saveComponent(move);
        }
    }

    @Command(shortDescription = "Set jump speed", runOnServer = true)
    public void setJumpSpeed(@CommandParam("amount") float amount, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            move.jumpSpeed = amount;
            clientComp.character.saveComponent(move);
        }
    }

    @Command(shortDescription = "Show your Movement stats")
    public String showMovement(EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            return "Your groundFriction:" + move.groundFriction + " maxGroudspeed:" + move.maxGroundSpeed + " JumpSpeed:"
                    + move.jumpSpeed + " maxWaterSpeed:" + move.maxWaterSpeed + " maxGhostSpeed:" + move.maxGhostSpeed + " SlopeFactor:"
                    + move.slopeFactor + " runFactor:" + move.runFactor;
        }
        return "You're dead I guess.";
    }

    @Command(shortDescription = "Go really fast", runOnServer = true)
    public void hspeed(EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            move.maxGhostSpeed = 50f;
            move.jumpSpeed = 24f;
            move.maxGroundSpeed = 20f;
            move.maxWaterSpeed = 12f;
            clientComp.character.saveComponent(move);
        }
    }

    @Command(shortDescription = "Jump really high", runOnServer = true)
    public void hjump(EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null && move != null) {
            move.jumpSpeed = 75f;
            health.fallingDamageSpeedThreshold = 85f;
            health.excessSpeedDamageMultiplier = 2f;
            clientComp.character.saveComponent(health);
            clientComp.character.saveComponent(move);
        }
    }

    @Command(shortDescription = "Restore normal speed values", runOnServer = true)
    public void restoreSpeed(EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            move.maxGhostSpeed = 3f;
            move.jumpSpeed = 12f;
            move.maxGroundSpeed = 5f;
            move.maxWaterSpeed = 2f;
            move.runFactor = 1.5f;
            move.stepHeight = 0.35f;
            move.slopeFactor = 0.6f;
            move.groundFriction = 8.0f;
            move.distanceBetweenFootsteps = 1f;
            clientComp.character.saveComponent(move);
        }
    }

    @Command(shortDescription = "Toggles the maximum slope the player can walk up", runOnServer = true)
    public String sleigh(EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            if (move.slopeFactor > 0.7f) {
                move.slopeFactor = 0.6f;
            } else {
                move.slopeFactor = 0.9f;
            }
            clientComp.character.saveComponent(move);
            return "Slope factor is now " + move.slopeFactor;
        }
        return "";
    }

    @Command(shortDescription = "Sets the height the player can step up", runOnServer = true)
    public void stepHeight(@CommandParam("height") float amount, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            move.stepHeight = amount;
            clientComp.character.saveComponent(move);
        }
    }
}
