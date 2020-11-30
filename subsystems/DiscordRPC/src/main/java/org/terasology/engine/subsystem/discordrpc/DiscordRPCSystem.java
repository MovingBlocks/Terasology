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

import java.time.OffsetDateTime;

/**
 * It's a system that runs when a single player or multi player game has been started to process some stuff throw the
 * {@link DiscordRPCSubSystem}.
 *
 * @see DiscordRPCSubSystem
 */
@RegisterSystem(RegisterMode.CLIENT)
public final class DiscordRPCSystem extends BaseComponentSystem {

    @In
    private Game game;

    @In
    private LocalPlayer player;

    @In
    private NetworkSystem networkSystem;

    @Override
    public void initialise() {
        DiscordRPCSubSystem.discover();
    }

    @Override
    public void preBegin() {
        DiscordRPCSubSystem.setState(getGame());
        DiscordRPCSubSystem.setStartTimestamp(null);
    }

    @Override
    public void postBegin() {
        DiscordRPCSubSystem.setStartTimestamp(OffsetDateTime.now());
    }

    @Override
    public void shutdown() {
        DiscordRPCSubSystem.setState("In Main Menu");
        DiscordRPCSubSystem.setStartTimestamp(null);
    }

    @ReceiveEvent
    public void onAfk(AfkEvent event, EntityRef entityRef) {
        if (isServer() && player.getClientEntity().equals(entityRef)) {
            return;
        }

        if (event.isAfk()) {
            DiscordRPCSubSystem.setState("Idle");
            DiscordRPCSubSystem.setStartTimestamp(null);
        } else {
            DiscordRPCSubSystem.setState(getGame());
            DiscordRPCSubSystem.setStartTimestamp(OffsetDateTime.now());
        }
    }

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

    private boolean isServer() {
        NetworkMode networkMode = networkSystem.getMode();
        return networkMode != NetworkMode.CLIENT && networkMode != NetworkMode.DEDICATED_SERVER;
    }
}
