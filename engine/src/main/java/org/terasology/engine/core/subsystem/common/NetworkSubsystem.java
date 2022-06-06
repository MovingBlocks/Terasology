// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.internal.NetworkSystemImpl;
import org.terasology.engine.network.internal.ServerConnectListManager;


public class NetworkSubsystem implements EngineSubsystem {

    private NetworkSystem networkSystem;

    @Override
    public String getName() {
        return "Network";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        networkSystem = new NetworkSystemImpl((EngineTime) rootContext.get(Time.class), rootContext);
        rootContext.put(NetworkSystem.class, networkSystem);
    }

    @Override
    public void postInitialise(Context rootContext) {
        rootContext.put(ServerConnectListManager.class, new ServerConnectListManager(rootContext));
    }

    @Override
    public void preUpdate(GameState currentState, float delta) {
        networkSystem.update();
    }
}
