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
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.Client;
import org.terasology.network.FieldReplicateType;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.network.Replicate;
import org.terasology.network.Server;
import org.terasology.registry.In;

import java.util.HashMap;
import java.util.Map;

@RegisterSystem(RegisterMode.ALWAYS)
public class AFKSystem extends BaseComponentSystem {

    private final Logger logger = LoggerFactory.getLogger(AFKSystem.class);

    @In
    private Context context;

    @In
    private NetworkSystem networkSystem;

    @In
    private LocalPlayer localPlayer;

    @In
    private Console console;

    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    private Map<Long, Boolean> afkMap = new HashMap<Long, Boolean>();

    private boolean afk;

    @Override
    public void initialise() {
        context.put(AFKSystem.class, this);
        logger.info("Initialised the AFK system");
    }

    @Override
    public void shutdown() {
        logger.info("Success! Shut down the afk system.");
    }

    @Command(
            value = "afk",
            shortDescription = "Tell the players that you are away from the keyboard",
            helpText = "[on:off]",
            requiredPermission = PermissionManager.NO_PERMISSION
    )
    public void command() {
        afk = !afk;
        if (networkSystem.getServer() == null && !networkSystem.getMode().isServer()) {
            console.addMessage("[AFK] Make sure you are connected to an online server ( singleplayer doesn't count )");
            return;
        }
        NetworkMode networkMode = networkSystem.getMode();
        if (networkMode == NetworkMode.DEDICATED_SERVER) {
            afkMap.put(localPlayer.getClientEntity().getId(), afk);
            onAFKRequest(new AFKRequest(afk), localPlayer.getClientEntity());
            if (afk) {
                console.addMessage("[AFK] You are AFK!");
            } else {
                console.addMessage("[AFK] You are no longer AFK!");
            }
        } else if (networkMode == NetworkMode.CLIENT) {
            networkSystem.getServer().send(new AFKRequest(afk), localPlayer.getClientEntity());
        }
    }

    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void onAFKRequest(AFKRequest event, EntityRef entityRef) {
        afkMap.put(entityRef.getId(), event.isAfk());
        for (Client client : networkSystem.getPlayers()) {
            client.send(new AFKEvent(event.isAfk()), entityRef);
        }
    }

    @ReceiveEvent
    public void onAFKEvent(AFKEvent event, EntityRef entityRef) {
        afkMap.put(entityRef.getId(), event.isAfk());
        if (event.isAfk()) {
            console.addMessage("[AFK] You are AFK!");
        } else {
            console.addMessage("[AFK] You are no longer AFK!");
        }
    }

    public boolean isAFK(long id) {
        if (afkMap.containsKey(id)) {
            return afkMap.get(id);
        }
        return false;
    }

}
