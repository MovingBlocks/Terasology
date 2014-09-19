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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.MovementMode;
import org.terasology.logic.characters.events.SetMovementModeEvent;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;
import org.terasology.logic.health.HealthComponent;
import org.terasology.network.ClientComponent;

/**
 * @author Immortius
 */
@RegisterSystem
public class MovementDebugCommands extends BaseComponentSystem {

    @Command(shortDescription = "Grants flight and movement through walls", runOnServer = true)
    public String ghost(EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        clientComp.character.send(new SetMovementModeEvent(MovementMode.GHOSTING));

        return "Ghost mode toggled";
    }

    @Command(shortDescription = "Set ground friction", runOnServer = true)
    public String setGroundFriction(@CommandParam("amount") float amount, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            float oldFric = move.groundFriction;

            move.groundFriction = amount;
            clientComp.character.saveComponent(move);
            
            return "Ground friction set to " + amount + " (was " + oldFric + ")"; 
        }
        
        return "";
    }

    @Command(shortDescription = "Set max ground speed", helpText = "Set maxGroundSpeed", runOnServer = true)
    public String setMaxGroundSpeed(@CommandParam("amount") float amount, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            float oldSpeed = move.maxGroundSpeed;
            move.maxGroundSpeed = amount;
            clientComp.character.saveComponent(move);

            return "Max ground speed set to " + amount + " (was " + oldSpeed + ")"; 
        }
        
        return "";
    }

    @Command(shortDescription = "Set max ghost speed", runOnServer = true)
    public String setMaxGhostSpeed(@CommandParam("amount") float amount, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            float oldSpeed = move.maxGhostSpeed;
            move.maxGhostSpeed = amount;
            clientComp.character.saveComponent(move);
            
            return "Max ghost speed set to " + amount + " (was " + oldSpeed + ")"; 
        }
        
        return "";
    }

    @Command(shortDescription = "Set jump speed", runOnServer = true)
    public String setJumpSpeed(@CommandParam("amount") float amount, EntityRef client) {
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
    public String hspeed(EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move != null) {
            move.maxGhostSpeed = 50f;
            move.jumpSpeed = 24f;
            move.maxGroundSpeed = 20f;
            move.maxWaterSpeed = 12f;
            clientComp.character.saveComponent(move);
            
            return "High-speed mode activated";
        }
        
        return "";
    }

    @Command(shortDescription = "Jump really high", runOnServer = true)
    public String hjump(EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null && move != null) {
            move.jumpSpeed = 75f;
            health.fallingDamageSpeedThreshold = 85f;
            health.excessSpeedDamageMultiplier = 2f;
            clientComp.character.saveComponent(health);
            clientComp.character.saveComponent(move);
            
            return "High-jump mode activated";
        }
        
        return "";
    }

    @Command(shortDescription = "Restore normal speed values", runOnServer = true)
    public String restoreSpeed(EntityRef client) {
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
            
            return "Normal speed values restored";
        }
        
        return "";
    }

    @Command(shortDescription = "Toggles the maximum slope the player can walk up", runOnServer = true)
    public String sleigh(EntityRef client) {
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

    @Command(shortDescription = "Sets the height the player can step up", runOnServer = true)
    public String stepHeight(@CommandParam("height") float amount, EntityRef client) {
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
