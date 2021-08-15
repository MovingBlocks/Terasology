// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.exceptions.HostingFailedException;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.layers.mainMenu.MessagePopup;

public class StartServer extends SingleStepLoadProcess {

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
            context.get(NUIManager.class).pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Failed to Host",
                    e.getMessage() + " - Reverting to single player");
        }
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
