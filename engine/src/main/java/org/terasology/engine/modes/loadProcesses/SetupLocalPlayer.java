/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.config.Config;
import org.terasology.config.PlayerConfig;
import org.terasology.registry.CoreRegistry;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.Client;
import org.terasology.network.NetworkSystem;

/**
 * @author Immortius
 */
public class SetupLocalPlayer extends SingleStepLoadProcess {
    @Override
    public String getMessage() {
        return "Setting up local player";
    }

    @Override
    public boolean step() {
        PlayerConfig playerConfig = CoreRegistry.get(Config.class).getPlayer();
        Client localClient = CoreRegistry.get(NetworkSystem.class).joinLocal(playerConfig.getName(), playerConfig.getColor());
        CoreRegistry.get(LocalPlayer.class).setClientEntity(localClient.getEntity());
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

}
