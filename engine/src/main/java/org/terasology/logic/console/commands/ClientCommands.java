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
package org.terasology.logic.console.commands;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.ClientComponent;
import org.terasology.network.PingSubscriberComponent;
import org.terasology.network.events.DeactivatePingServerEvent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;

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
    public String setWorldTime(@CommandParam("day") float day) {
        worldProvider.getTime().setDays(day);
        return "World time changed";
    }

    /**
     * Subscribes the ping from server function
     * @param sender Client who sends the command
     * @return       The subscription state
     */
    @Command(runOnServer = true,
            shortDescription = "Once activated the ping wil be shown in the top right corner of the debug overlay",
            requiredPermission = PermissionManager.NO_PERMISSION)
    public String togglePingInDebugScreen(@Sender EntityRef sender) {
        if (sender.getComponent(ClientComponent.class).local) {
            return "You are on server or single player mode, don't need the ping information";
        }

        if (!sender.hasComponent(PingSubscriberComponent.class)) {
            PingSubscriberComponent pingSubscriberComp = new PingSubscriberComponent();
            sender.addComponent(pingSubscriberComp);
            return "Ping from server function activated, see in the debug overlay";
        }
        else {
            sender.removeComponent(PingSubscriberComponent.class);
            sender.send(new DeactivatePingServerEvent());
            return "Ping from server function deactivated";
        }
    }
}
