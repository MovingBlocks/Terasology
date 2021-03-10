/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.core.subsystem.common;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.internal.NetworkSystemImpl;
import org.terasology.engine.network.internal.ServerConnectListManager;

/**
 *
 */
public class NetworkSubsystem implements EngineSubsystem {

    private NetworkSystem networkSystem;

    @Override
    public String getName() {
        return "Network";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        networkSystem = new NetworkSystemImpl(rootContext.get(Time.class), rootContext);
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
