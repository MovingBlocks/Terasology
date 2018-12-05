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

@RegisterSystem(RegisterMode.CLIENT)
public class AfkClientSystem extends BaseComponentSystem {

    @In
    private Console console;

    @In
    private LocalPlayer localPlayer;

    @In
    private NetworkSystem networkSystem;

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
        EntityRef entity = localPlayer.getClientEntity();
        AfkComponent component = entity.getComponent(AfkComponent.class);
        component.afk = !component.afk;
        if (component.afk) {
            console.addMessage("[AFK] You are AFK now!");
        } else {
            console.addMessage("[AFK] You are no longer AFK!");
        }
        entity.addOrSaveComponent(component);
        AfkRequest request = new AfkRequest(entity, component.afk);
        entity.send(request);
    }

    @ReceiveEvent(netFilter = RegisterMode.CLIENT)
    public void onMove(MovedEvent movedEvent, EntityRef entity) {
        EntityRef clientEntity = localPlayer.getClientEntity();
        AfkComponent component = entity.getComponent(AfkComponent.class);
        if (component.afk) {
            component.afk = false;
            clientEntity.addOrSaveComponent(component);
            AfkRequest request = new AfkRequest(clientEntity, false);
            clientEntity.send(request);
        }
    }

}
