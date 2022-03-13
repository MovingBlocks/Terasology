// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.modes.VariableStepLoadProcess;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.world.chunks.ChunkProvider;

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
