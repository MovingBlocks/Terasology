// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.config.Config;
import org.terasology.config.PlayerConfig;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.Client;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;

@ExpectedCost(1)
public class SetupLocalPlayer extends SingleStepLoadProcess {

    @In
    private Config config;
    @In
    private NetworkSystem networkSystem;
    @In
    private LocalPlayer localPlayer;

    @Override
    public String getMessage() {
        return "Setting up local player";
    }

    @Override
    public boolean step() {
        PlayerConfig playerConfig = config.getPlayer();
        Client localClient = networkSystem.joinLocal(playerConfig.getName(), playerConfig.getColor());
        localPlayer.setClientEntity(localClient.getEntity());
        return true;
    }
}
