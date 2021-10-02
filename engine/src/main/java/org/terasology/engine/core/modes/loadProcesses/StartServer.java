// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.core.modes.StateLoading;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.exceptions.HostingFailedException;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.layers.mainMenu.MessagePopup;

public class StartServer extends SingleStepLoadProcess {
    private static final Logger logger = LoggerFactory.getLogger(StartServer.class);

    private final Context context;
    private final boolean dedicated;

    /**
     * @param dedicated true, if server should be dedicated (i.e. with local client)
     */
    public StartServer(Context context, boolean dedicated) {
        this.context = context;
        this.dedicated = dedicated;
    }

    @Override
    public String getMessage() {
        return "Starting Server";
    }

    @Override
    public boolean step() {
        try {
            Config config = context.get(Config.class);
            int port = config.getNetwork().getServerPort();
            context.get(NetworkSystem.class).host(port, dedicated);
        } catch (HostingFailedException e) {
            NUIManager nui = context.get(NUIManager.class);
            if (nui != null) {
                nui.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Failed to Host",
                        e.getMessage() + " - Reverting to single player");
            } else {
                logger.error("Failed to Host. NUI was also unavailable for warning popup (headless?)", e);
                throw new RuntimeException("Cannot host game successfully from likely headless start, terminating");
            }
        }
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
