// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.discordrpc;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.game.Game;
import org.terasology.logic.afk.AfkEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;

/**
 * It's a system that runs when a single player or multi player game has been started to process some stuff throw the
 * {@link DiscordRPCSubSystem}.
 *
 * @see DiscordRPCSubSystem
 */
@RegisterSystem(RegisterMode.CLIENT)
public class DiscordRPCSystem extends BaseComponentSystem {

    @In
    private Game game;

    @In
    private LocalPlayer player;

    @In
    private NetworkSystem networkSystem;

    public String getGame() {
        NetworkMode networkMode = networkSystem.getMode();
        String mode = "Playing Online";
        if (networkMode == NetworkMode.DEDICATED_SERVER) {
            mode = "Hosting | " + game.getName();
        } else if (networkMode == NetworkMode.NONE) {
            mode = "Solo | " + game.getName();
        }
        return mode;
    }

    @ReceiveEvent
    public void onAfk(AfkEvent event, EntityRef entityRef) {
        if (requireConnection() && player.getClientEntity().equals(entityRef)) {
            return;
        }
        if (event.isAfk()) {
            disableDiscord();
        } else {
            enableDiscord();
        }
    }

    private boolean requireConnection() {
        NetworkMode networkMode = networkSystem.getMode();
        return networkMode != NetworkMode.CLIENT && networkMode != NetworkMode.DEDICATED_SERVER;
    }

    private void enableDiscord() {
        DiscordRPCSubSystem.tryToDiscover();
        DiscordRPCSubSystem.setState("Idle", true);
    }

    private void disableDiscord() {
        DiscordRPCSubSystem.tryToDiscover();
        DiscordRPCSubSystem.setState(getGame(), true);
    }

    @Override
    public void initialise() {
        DiscordRPCSubSystem.tryToDiscover();
    }

    @Override
    public void preBegin() {
        DiscordRPCSubSystem.setState(getGame(), false);
    }

    @Override
    public void postBegin() {
        DiscordRPCSubSystem.setState(getGame(), true);
    }

    @Override
    public void shutdown() {
        DiscordRPCSubSystem.setState("In Lobby");
    }
}
