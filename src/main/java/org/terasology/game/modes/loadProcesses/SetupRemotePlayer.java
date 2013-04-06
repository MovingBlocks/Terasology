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

import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.game.modes.LoadProcess;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.internal.NetEntityRef;
import org.terasology.network.NetworkSystem;
import org.terasology.network.internal.NetworkSystemImpl;

/**
 * @author Immortius
 */
public class SetupRemotePlayer implements LoadProcess {
    @Override
    public String getMessage() {
        return "Awaiting player data";
    }

    @Override
    public boolean step() {
        NetworkSystemImpl networkSystem = (NetworkSystemImpl) CoreRegistry.get(NetworkSystem.class);
        EntityRef client = new NetEntityRef(networkSystem.getServer().getInfo().getClientId(), networkSystem);
        if (client.exists()) {
            CoreRegistry.get(LocalPlayer.class).setClientEntity(client);
            return true;
        }
        return false;
    }

    @Override
    public int begin() {
        return 1;
    }
}
