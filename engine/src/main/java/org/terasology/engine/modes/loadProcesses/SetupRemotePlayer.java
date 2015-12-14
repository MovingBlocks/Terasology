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

import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.NetworkSystem;
import org.terasology.network.internal.NetworkSystemImpl;

/**
 */
public class SetupRemotePlayer extends SingleStepLoadProcess {

    private final Context context;

    public SetupRemotePlayer(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Awaiting player data";
    }

    @Override
    public boolean step() {
        NetworkSystemImpl networkSystem = (NetworkSystemImpl) context.get(NetworkSystem.class);
        EntityRef client = networkSystem.getServer().getClientEntity();
        if (client.exists()) {
            context.get(LocalPlayer.class).setClientEntity(client);
            return true;
        }
        return false;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
