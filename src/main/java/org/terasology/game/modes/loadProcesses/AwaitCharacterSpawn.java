/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.game.modes.loadProcesses;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.modes.LoadProcess;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.rendering.world.WorldRenderer;

/**
 * @author Immortius
 */
public class AwaitCharacterSpawn implements LoadProcess {

    private WorldRenderer worldRenderer;

    @Override
    public String getMessage() {
        return "Awaiting Character Spawn...";
    }

    @Override
    public boolean step() {
        ComponentSystemManager componentSystemManager = CoreRegistry.get(ComponentSystemManager.class);
        for (UpdateSubscriberSystem updater : componentSystemManager.iterateUpdateSubscribers()) {
            updater.update(0.0f);
        }
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        ClientComponent client = localPlayer.getClientEntity().getComponent(ClientComponent.class);
        if (client != null && client.character.exists()) {
            worldRenderer.setPlayer(CoreRegistry.get(LocalPlayer.class));
            return true;
        }
        return false;
    }

    @Override
    public int begin() {
        worldRenderer = CoreRegistry.get(WorldRenderer.class);
        return UNKNOWN_STEPS;
    }

}
