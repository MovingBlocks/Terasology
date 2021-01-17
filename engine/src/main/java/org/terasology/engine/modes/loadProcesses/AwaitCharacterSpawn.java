/*
 * Copyright 2016 MovingBlocks
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

package org.terasology.engine.modes.loadProcesses;

import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.modes.VariableStepLoadProcess;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.world.chunks.ChunkProvider;

/**
 * Loops until player is loaded into the world.
 *
 * Takes variable amount of steps.
 */
public class AwaitCharacterSpawn extends VariableStepLoadProcess {

    private final Context context;
    private ChunkProvider chunkProvider;

    public AwaitCharacterSpawn(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return  "${engine:menu#awaiting-character-spawn}";
    }

    @Override
    public boolean step() {
        ComponentSystemManager componentSystemManager = context.get(ComponentSystemManager.class);
        for (UpdateSubscriberSystem updater : componentSystemManager.iterateUpdateSubscribers()) {
            updater.update(0.0f);
        }
        LocalPlayer localPlayer = context.get(LocalPlayer.class);
        ClientComponent client = localPlayer.getClientEntity().getComponent(ClientComponent.class);
        if (client != null && client.character.exists()) {
            client.character.send(new AwaitedLocalCharacterSpawnEvent());
            return true;
        } else {
            chunkProvider.update();
        }
        return false;
    }

    @Override
    public void begin() {
        chunkProvider = context.get(ChunkProvider.class);
    }

    @Override
    public int getExpectedCost() {
        return 10;
    }
}
