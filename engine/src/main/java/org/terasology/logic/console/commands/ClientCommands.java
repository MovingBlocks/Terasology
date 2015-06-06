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
import org.terasology.logic.permission.PermissionManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.scheduling.TaskManager;
import org.terasology.world.WorldProvider;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class ClientCommands extends BaseComponentSystem {
    @In
    private TaskManager taskManager;

    @In
    private CameraTargetSystem cameraTargetSystem;

    @Command(shortDescription = "Reports size of client work thread pool",
            requiredPermission = PermissionManager.NO_PERMISSION)
    public String getClientThreads() {
        return String.format("Client is running %d work threads", taskManager.getPoolSize());
    }

    // TODO: Permission?  Particularly for combined (singleplayer) client/server execution....
    @Command(shortDescription = "Sets size of client work thread pool",
            requiredPermission = PermissionManager.NO_PERMISSION)
    public String setClientThreads(@CommandParam("numThreads") int numThreads) {
        taskManager.setPoolSize(numThreads);
        return String.format("Client is running %d work threads", taskManager.getPoolSize());
    }

    @Command(shortDescription = "Displays debug information on the target entity")
    public String debugTarget() {
        EntityRef cameraTarget = cameraTargetSystem.getTarget();
        return cameraTarget.toFullDescription();
    }

    @Command(shortDescription = "Sets the current world time of the in days for the local player",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setWorldTime(@CommandParam("day") float day) {
        WorldProvider world = CoreRegistry.get(WorldProvider.class);
        world.getTime().setDays(day);

        return "World time changed";
    }
}
