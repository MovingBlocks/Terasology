// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.systems;

import org.terasology.engine.core.subsystem.rpc.DiscordRPCSubSystem;
import org.terasology.engine.game.Game;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;

/**
 * It's a system that runs when a single player or multi player game has been started to process some stuff
 * throw the {@link DiscordRPCSubSystem}.
 *
 * @see DiscordRPCSubSystem
 */
@RegisterSystem(RegisterMode.CLIENT)
public class DiscordRPCSystem extends BaseComponentSystem {

    @In
    private Game game;

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
