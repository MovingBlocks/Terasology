// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.VariableStepLoadProcess;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.chunks.ChunkProvider;

/**
 * Loops until player is loaded into the world.
 * <p>
 * Takes variable amount of steps.
 */
@ExpectedCost(10)
public class AwaitCharacterSpawn extends VariableStepLoadProcess {

    @In
    private ChunkProvider chunkProvider;
    @In
    private LocalPlayer localPlayer;
    @In
    private ComponentSystemManager componentSystemManager;

    @Override
    public String getMessage() {
        return "${engine:menu#awaiting-character-spawn}";
    }

    @Override
    public boolean step() {

        for (UpdateSubscriberSystem updater : componentSystemManager.iterateUpdateSubscribers()) {
            updater.update(0.0f);
        }
        ClientComponent client = localPlayer.getClientEntity().getComponent(ClientComponent.class);
        if (client != null && client.character.exists()) {
            client.character.send(new AwaitedLocalCharacterSpawnEvent());
            return true;
        } else {
            chunkProvider.completeUpdate();
            chunkProvider.beginUpdate();
        }
        return false;
    }
}
