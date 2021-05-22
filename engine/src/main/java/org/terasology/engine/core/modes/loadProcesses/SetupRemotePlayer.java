// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.internal.NetworkSystemImpl;

/**
 */
public class SetupRemotePlayer extends SingleStepLoadProcess {

    private final Context context;

    public SetupRemotePlayer(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Awaiting player data";
    }

    @Override
    public boolean step() {
        NetworkSystemImpl networkSystem = (NetworkSystemImpl) context.get(NetworkSystem.class);
        EntityRef client = networkSystem.getServer().getClientEntity();
        if (client.exists()) {
            context.get(LocalPlayer.class).setClientEntity(client);
            return true;
        }
        return false;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
