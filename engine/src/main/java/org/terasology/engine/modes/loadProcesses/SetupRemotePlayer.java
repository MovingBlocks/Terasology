// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.NetworkSystem;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.registry.In;

@ExpectedCost(1)
public class SetupRemotePlayer extends SingleStepLoadProcess {

    @In
    private NetworkSystem networkSystem;
    @In
    private LocalPlayer localPlayer;


    @Override
    public String getMessage() {
        return "Awaiting player data";
    }

    @Override
    public boolean step() {
        NetworkSystemImpl networkSystemImpl = (NetworkSystemImpl) networkSystem;
        EntityRef client = networkSystemImpl.getServer().getClientEntity();
        if (client.exists()) {
            localPlayer.setClientEntity(client);
            return true;
        }
        return false;
    }
}
