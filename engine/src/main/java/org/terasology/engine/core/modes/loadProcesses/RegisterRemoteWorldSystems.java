// Copyright 2024 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.entitySystem.systems.ComponentSystem;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.chunks.remoteChunkProvider.RemoteChunkProvider;
import org.terasology.engine.world.sun.CelestialSystem;

public class RegisterRemoteWorldSystems extends SingleStepLoadProcess {
    private final Context context;

    public RegisterRemoteWorldSystems(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Registering World Systems...";
    }

    @Override
    public boolean step() {
        context.get(NetworkSystem.class).setRemoteWorldProvider(context.get(RemoteChunkProvider.class));

        ComponentSystemManager componentSystemManager = context.get(ComponentSystemManager.class);
        componentSystemManager.register((ComponentSystem) context.get(BlockEntityRegistry.class), "engine:BlockEntityRegistry");
        componentSystemManager.register((ComponentSystem) context.get(CelestialSystem.class));

        WorldRenderer worldRenderer = context.get(WorldRenderer.class);
        float reflectionHeight = context.get(NetworkSystem.class).getServer().getInfo().getReflectionHeight();
        worldRenderer.getActiveCamera().setReflectionHeight(reflectionHeight);
        worldRenderer.init();
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
