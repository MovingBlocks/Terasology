// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import org.terasology.context.Lifetime;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.internal.NetworkSystemImpl;
import org.terasology.engine.network.internal.ServerConnectListManager;
import org.terasology.gestalt.di.ServiceRegistry;

import javax.inject.Inject;

public class NetworkSubsystem implements EngineSubsystem {
    private NetworkSystem networkSystem;

    @Inject
    public NetworkSubsystem() {
    }

    @Override
    public String getName() {
        return "Network";
    }

    @Override
    public void initialise(GameEngine engine, ServiceRegistry serviceRegistry) {
        serviceRegistry.with(NetworkSystem.class).lifetime(Lifetime.Singleton).use(NetworkSystemImpl.class);
    }

    @Override
    public void postInitialise(Context rootContext) {
        rootContext.put(ServerConnectListManager.class, new ServerConnectListManager(rootContext));
        networkSystem = rootContext.get(NetworkSystem.class);
    }

    @Override
    public void preUpdate(GameState currentState, float delta) {
        networkSystem.update();
    }
}
