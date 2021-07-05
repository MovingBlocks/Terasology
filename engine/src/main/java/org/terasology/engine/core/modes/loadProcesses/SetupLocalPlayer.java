// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.config.PlayerConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.Client;
import org.terasology.engine.network.NetworkSystem;

public class SetupLocalPlayer extends SingleStepLoadProcess {

    private final Context context;

    public SetupLocalPlayer(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Setting up local player";
    }

    @Override
    public boolean step() {
        PlayerConfig playerConfig = context.get(PlayerConfig.class);
        Client localClient = context.get(NetworkSystem.class).joinLocal(playerConfig.playerName.get(),
                playerConfig.color.get());
        context.get(LocalPlayer.class).setClientEntity(localClient.getEntity());
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

}
