// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.common;

import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.network.NetworkSystem;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.network.internal.ServerConnectListManager;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

/**
 *
 */
public class NetworkSubsystem implements EngineSubsystem {

    @In
    private ContextAwareClassFactory classFactory;

    private NetworkSystem networkSystem;

    @Override
    public String getName() {
        return "Network";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        networkSystem = classFactory.createToContext(NetworkSystemImpl.class, NetworkSystem.class);
    }

    @Override
    public void postInitialise(Context rootContext) {
        classFactory.createToContext(ServerConnectListManager.class);
    }

    @Override
    public void preUpdate(GameState currentState, float delta) {
        networkSystem.update();
    }
}
