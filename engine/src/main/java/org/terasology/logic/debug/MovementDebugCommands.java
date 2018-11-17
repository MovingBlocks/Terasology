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

import org.terasology.entitySystem.entity.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.characters.CharacterImpulseEvent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.CharacterTeleportEvent;
import org.terasology.logic.characters.GazeMountPointComponent;
import org.terasology.logic.characters.MovementMode;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Quat4f;
import org.terasology.physics.engine.PhysicsEngine;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.SetMovementModeEvent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;

import java.util.Optional;

@RegisterSystem
@Share(MovementDebugCommands.class)
public class MovementDebugCommands extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(MovementDebugCommands.class);

    @In
    private PhysicsEngine physics;

    @In
    private EntityManager entityManager;

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

    @Command(value = "pushCharacter", shortDescription = "Pushes you in the direction (x, y, z)", runOnServer = true)
    public String pushCharacterCommand(@Sender EntityRef sender,
                                       @CommandParam("x") float x, @CommandParam("y") float y, @CommandParam("z") float z) {
        ClientComponent clientComponent = sender.getComponent(ClientComponent.class);
        clientComponent.character.send(new CharacterImpulseEvent(new Vector3f(x, y, z)));
        return "Pushing character with " + x + " " + y + " " + z;
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

    @Command(shortDescription = "Show your Position/Location",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String showPosition(@Sender EntityRef client) {
        LocationComponent loc = client.getComponent(LocationComponent.class);
        return "Your Position: " + loc.getWorldPosition();
    }

    @Command(shortDescription = "Show your Height",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String showHeight(@Sender EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
        float height = move.height;
        GazeMountPointComponent gazeMountPointComponent = clientComp.character.getComponent(GazeMountPointComponent.class);
        float eyeHeight = gazeMountPointComponent.translate.y;
        return "Your height: " + height + " Eye-height: " + eyeHeight;
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

    @Command(shortDescription = "Sets the height of the player", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String playerHeight(@Sender EntityRef client, @CommandParam("height") float amount) {
        try {
            ClientComponent clientComp = client.getComponent(ClientComponent.class);
            CharacterMovementComponent move = clientComp.character.getComponent(CharacterMovementComponent.class);
            if (move != null) {
                float prevHeight = move.height;
                move.height = amount;
                clientComp.character.saveComponent(move);
                LocationComponent loc = client.getComponent(LocationComponent.class);
                Vector3f currentPosition = loc.getWorldPosition();
                clientComp.character
                        .send(new CharacterTeleportEvent(new Vector3f(currentPosition.getX(), currentPosition.getY() + (amount - prevHeight) / 2, currentPosition.getZ())));
                physics.removeCharacterCollider(clientComp.character);
                physics.getCharacterCollider(clientComp.character);
                return "Height of player set to " + amount + " (was " + prevHeight + ")";
            }
            return "";
        } catch (NullPointerException e) {
            logger.error(e.getMessage(), e);
            return "";
        }
    }

    @Command(shortDescription = "Sets the eye-height of the player", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String playerEyeHeight(@Sender EntityRef client, @CommandParam("eye-height") float amount) {
        EntityRef player = client.getComponent(ClientComponent.class).character;
        try {
            GazeMountPointComponent gazeMountPointComponent = player.getComponent(GazeMountPointComponent.class);
            if (gazeMountPointComponent != null) {
                float prevHeight = gazeMountPointComponent.translate.y;
                gazeMountPointComponent.translate.y = amount;
                Location.removeChild(player, gazeMountPointComponent.gazeEntity);
                Location.attachChild(player, gazeMountPointComponent.gazeEntity, gazeMountPointComponent.translate, new Quat4f(Quat4f.IDENTITY));
                player.saveComponent(gazeMountPointComponent);
                return "Eye-height of player set to " + amount + " (was " + prevHeight + ")";
            }
            return "";
        } catch (NullPointerException e) {
            logger.error(e.getMessage(), e);
            return "";
        }
    }

    @Command(value = "teleport", shortDescription = "Teleports you to a location", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String teleportCommand(@Sender EntityRef sender, @CommandParam("x") float x, @CommandParam("y") float y, @CommandParam("z") float z) {
        ClientComponent clientComp = sender.getComponent(ClientComponent.class);
        clientComp.character.send(new CharacterTeleportEvent(new Vector3f(x, y, z)));
        return "Teleporting  to " + x + " " + y + " " + z;
    }

    @Command(shortDescription = "Teleport to player", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String teleportMeToPlayer(@Sender EntityRef sender, @CommandParam("username") String username) {

        for (EntityRef clientEntity : entityManager.getEntitiesWith(ClientComponent.class)) {
            EntityRef clientInfo = clientEntity.getComponent(ClientComponent.class).clientInfo;

            DisplayNameComponent name = clientInfo.getComponent(DisplayNameComponent.class);
            if (username.equalsIgnoreCase(name.name)) {
                LocationComponent locationComponent = clientEntity.getComponent(LocationComponent.class);
                if (locationComponent != null) {
                    Vector3f vLocation = locationComponent.getWorldPosition();
                    ClientComponent clientComp = sender.getComponent(ClientComponent.class);
                    if (clientComp != null) {
                        clientComp.character.send(new CharacterTeleportEvent(vLocation));
                        return "Teleporting you to " + username + " at " + vLocation.x + " " + vLocation.y + " " + vLocation.z;
                    }
                }
            }
        }

        throw new IllegalArgumentException("No such user '" + username + "'");
    }

    @Command(shortDescription = "Teleport player to you", runOnServer = true,
            requiredPermission = PermissionManager.USER_MANAGEMENT_PERMISSION)
    public String teleportPlayerToMe(@Sender EntityRef sender, @CommandParam("username") String username) {

        for (EntityRef clientEntity : entityManager.getEntitiesWith(ClientComponent.class)) {
            EntityRef clientInfo = clientEntity.getComponent(ClientComponent.class).clientInfo;

            DisplayNameComponent name = clientInfo.getComponent(DisplayNameComponent.class);
            if (username.equalsIgnoreCase(name.name)) {
                LocationComponent locationComponent = sender.getComponent(LocationComponent.class);
                if (locationComponent != null) {
                    Vector3f vLocation = locationComponent.getWorldPosition();
                    ClientComponent clientComp = clientEntity.getComponent(ClientComponent.class);
                    if (clientComp != null) {
                        clientComp.character.send(new CharacterTeleportEvent(vLocation));
                        return "Teleporting " + username + " to you at " + vLocation.x + " " + vLocation.y + " " + vLocation.z;
                    }
                }
            }
        }

        throw new IllegalArgumentException("No such user '" + username + "'");
    }

    @Command(shortDescription = "Teleport User1 to User2", runOnServer = true,
            requiredPermission = PermissionManager.USER_MANAGEMENT_PERMISSION)
    public String teleportPlayerToPlayer(@CommandParam("usernameFrom") String usernameFrom, @CommandParam("usernameTo") String usernameTo) {

        if (usernameFrom.equalsIgnoreCase(usernameTo)) {
            throw new IllegalArgumentException("Why teleport to yourself...");
        }

        EntityRef entityFrom = null;
        EntityRef entityTo = null;
        boolean foundEntityFrom = false;
        boolean foundEntityTo = false;

        for (EntityRef clientEntity : entityManager.getEntitiesWith(ClientComponent.class)) {
            EntityRef clientInfo = clientEntity.getComponent(ClientComponent.class).clientInfo;

            DisplayNameComponent name = clientInfo.getComponent(DisplayNameComponent.class);
            if (!foundEntityFrom && usernameFrom.equalsIgnoreCase(name.name)) {
                entityFrom = clientEntity;
                foundEntityFrom = true;
            } else if (!foundEntityTo && usernameTo.equalsIgnoreCase(name.name)) {
                entityTo = clientEntity;
                foundEntityTo = true;
            }

            if (foundEntityFrom && foundEntityTo) {
                break;
            }
        }

        if (!foundEntityFrom) {
            throw new IllegalArgumentException("No such user '" + usernameFrom + "'");
        }
        if (!foundEntityTo) {
            throw new IllegalArgumentException("No such user '" + usernameTo + "'");
        }

        LocationComponent locationComponent = entityTo.getComponent(LocationComponent.class);
        if (locationComponent != null) {
            Vector3f vLocation = locationComponent.getWorldPosition();
            ClientComponent clientComp = entityFrom.getComponent(ClientComponent.class);
            if (clientComp != null) {
                clientComp.character.send(new CharacterTeleportEvent(vLocation));
                return "Teleporting " + usernameFrom + " to " + usernameTo + " at " + vLocation.x + " " + vLocation.y + " " + vLocation.z;
            }
        }

        throw new IllegalArgumentException("User " + usernameTo + " has an invalid location.");
    }

    @Command(shortDescription = "Teleport all users to location", runOnServer = true,
            requiredPermission = PermissionManager.USER_MANAGEMENT_PERMISSION)
    public String teleportAllPlayersToLocation(@CommandParam("x") float x, @CommandParam("y") float y, @CommandParam("z") float z) {

        for (EntityRef clientEntity : entityManager.getEntitiesWith(ClientComponent.class)) {
            ClientComponent clientComp = clientEntity.getComponent(ClientComponent.class);
            if (clientComp != null) {
                clientComp.character.send(new CharacterTeleportEvent(new Vector3f(x, y, z)));
            }
        }

        return "All possible players teleported";
    }

    @Command(shortDescription = "Teleport all users to specified user", runOnServer = true,
            requiredPermission = PermissionManager.USER_MANAGEMENT_PERMISSION)
    public String teleportAllPlayersToPlayer(@CommandParam("username") String username) {

        Vector3f vPlayerLocation = Vector3f.zero();
        boolean bPlayerLocationWasFound = false;
        EntityRef playerEntity = null;

        for (EntityRef clientEntity : entityManager.getEntitiesWith(ClientComponent.class)) {
            EntityRef clientInfo = clientEntity.getComponent(ClientComponent.class).clientInfo;

            DisplayNameComponent name = clientInfo.getComponent(DisplayNameComponent.class);
            if (username.equalsIgnoreCase(name.name)) {
                LocationComponent locationComponent = clientEntity.getComponent(LocationComponent.class);
                if (locationComponent != null) {
                    vPlayerLocation = locationComponent.getWorldPosition();
                    bPlayerLocationWasFound = true;
                    playerEntity = clientEntity;
                }
                break;
            }
        }

        if (!bPlayerLocationWasFound) {
            throw new IllegalArgumentException("No such user '" + username + "'");
        }

        MovementMode playerMovementMode = MovementMode.NONE;
        ClientComponent clientInfo = playerEntity.getComponent(ClientComponent.class);
        if (clientInfo != null) {
            CharacterMovementComponent playerMovementComponent = clientInfo.character.getComponent(CharacterMovementComponent.class);
            if (playerMovementComponent != null) {
                playerMovementMode = playerMovementComponent.mode;
            }
        }

        for (EntityRef clientEntity : entityManager.getEntitiesWith(ClientComponent.class)) {
            ClientComponent clientComp = clientEntity.getComponent(ClientComponent.class);
            if (clientComp != null) {
                clientComp.character.send(new CharacterTeleportEvent(vPlayerLocation));

                CharacterMovementComponent characterMovementComponent = clientComp.character.getComponent(CharacterMovementComponent.class);
                if (characterMovementComponent != null && playerMovementMode != MovementMode.NONE && playerMovementMode != characterMovementComponent.mode) {
                    clientComp.character.send(new SetMovementModeEvent(playerMovementMode));
                }
            }
        }

        return "All possible players teleported to " + username + " and set to " + playerMovementMode;
    }
}
