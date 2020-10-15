// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.config.Config;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.network.exceptions.HostingFailedException;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.mainMenu.MessagePopup;

@ExpectedCost(1)
public class StartServer extends SingleStepLoadProcess {

    @In
    private Config config;
    @In
    private NUIManager nuiManager;
    @In
    private NetworkSystem networkSystem;

    private final boolean dedicated;

    public StartServer(NetworkMode netMode) {
        if (netMode == NetworkMode.DEDICATED_SERVER) {
            dedicated = true;
        } else if (netMode == NetworkMode.LISTEN_SERVER) {
            dedicated = false;
        } else {
            throw new IllegalStateException("Invalid server mode: " + netMode);
        }
    }

    @Override
    public String getMessage() {
        return "Starting Server";
    }

    @Override
    public boolean step() {
        try {
            int port = config.getNetwork().getServerPort();
            networkSystem.host(port, dedicated);
        } catch (HostingFailedException e) {
            nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Failed to Host",
                    e.getMessage() + " - Reverting to single player");
        }
        return true;
    }
}
