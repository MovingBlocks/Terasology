// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.console.commands;

import org.joml.Vector3f;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.StaticSpawnLocationComponent;
import org.terasology.logic.players.event.WorldtimeResetEvent;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.sun.CelestialSystem;

/**
 * This class contains basic client commands for debugging eg.
 * for displaying debug information for the target at which is camera pointing at
 * and for setting current world time for the local player in days
 */
@RegisterSystem
public class ClientCommands extends BaseComponentSystem {
    @In
    private CameraTargetSystem cameraTargetSystem;

    @In
    private WorldProvider worldProvider;

    @In
    private NetworkSystem networkSystem;

    @In
    private CelestialSystem celestialSystem;

    /**
     * Displays debug information on the target entity for the target the camera is pointing at
     * @return String containing debug information on the entity
     */
    @Command(shortDescription = "Displays debug information on the target entity")
    public String debugTarget() {
        EntityRef cameraTarget = cameraTargetSystem.getTarget();
        return cameraTarget.toFullDescription();
    }

    /**
     * Sets the current world time for the local player in days
     * @param day Float containing day to be set
     * @return String message containing message to notify user
     */
    @Command(shortDescription = "Sets the current world time for the local player in days",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setWorldTime(@CommandParam("day") float day, @Sender EntityRef sender) {
        worldProvider.getTime().setDays(day);
        sender.send(new WorldtimeResetEvent(day));
        return "World time changed";
    }

    /**
     * Permanently halts the sun's position and angle
     * @param day Float containing day to be set
     * @return String message containing message to notify user
     */
    @Command(shortDescription = "Permanently halts the sun's position and angle", requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public  String toggleSunHalting(@CommandParam("day") float day) {
        celestialSystem.toggleSunHalting(day);

        if (celestialSystem.isSunHalted()) {
            return "Permanently set the sun's position.";
        } else {
            return "Disabled the sun's halt.";
        }
    }
    /**
     * Sets the spawn location for the client to the current location
     * @return String containing debug information on the entity
     */
    @Command(shortDescription = "Sets the spawn location for the client to the current location", runOnServer = true, requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setSpawnLocation(@Sender EntityRef sender) {
        EntityRef clientInfo = sender.getComponent(ClientComponent.class).clientInfo;
        StaticSpawnLocationComponent staticSpawnLocationComponent = new StaticSpawnLocationComponent();
        if (clientInfo.hasComponent(StaticSpawnLocationComponent.class)) {
            staticSpawnLocationComponent = clientInfo.getComponent(StaticSpawnLocationComponent.class);
        }
        staticSpawnLocationComponent.position = sender.getComponent(ClientComponent.class).character.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
        clientInfo.addOrSaveComponent(staticSpawnLocationComponent);
        return "Set spawn location to- " + staticSpawnLocationComponent.position;
    }
}
