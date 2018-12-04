/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.logic.afk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.events.MovedEvent;
import org.terasology.registry.In;
import org.terasology.registry.Share;

@Share(AFK.class)
@RegisterSystem(RegisterMode.ALWAYS)
public class AFKSystem extends BaseComponentSystem implements AFK {

    private static final Logger logger = LoggerFactory.getLogger(AFKSystem.class);

    @In
    private Console console;

    @In
    private LocalPlayer localPlayer;

    @In
    private NetworkSystem networkSystem;

    private AFKComponent component;

    @Override
    public void initialise() {
        component = new AFKComponent();
        localPlayer.getClientEntity().addComponent(component);
        logger.info("Successfully! Initialised the AFK system");
    }

    @Override
    @Command(
            value = "afk",
            shortDescription = "Say that you are AFK",
            requiredPermission = PermissionManager.NO_PERMISSION
    )
    public void onCommand() {
        NetworkMode networkMode = networkSystem.getMode();
        if (networkMode != NetworkMode.CLIENT && networkMode != NetworkMode.DEDICATED_SERVER) {
            console.addMessage("Failed! You need to be connected to use this command.");
            return;
        }
        component.afk = !component.afk;
        if (component.afk) {
            console.addMessage("[AFK} You are AFK now!");
        } else {
            console.addMessage("[AFK] You are no longer AFK!");
        }
        localPlayer.getClientEntity().addOrSaveComponent(component);
    }

    @ReceiveEvent(netFilter = RegisterMode.CLIENT)
    public void onMove(MovedEvent movedEvent, EntityRef entity) {
        if (component.afk) {
            component.afk = false;
            console.addMessage("[AFK] Welcome back, You are no longer AFK!");
            localPlayer.getClientEntity().addOrSaveComponent(component);
        }
    }

}
