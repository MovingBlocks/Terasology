/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

import org.terasology.components.LocalPlayerComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.RespawnEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.modes.LoadProcess;
import org.terasology.logic.LocalPlayer;

/**
 * @author Immortius
 */
public class RespawnPlayer implements LoadProcess {
    @Override
    public String getMessage() {
        return "Spawning Player...";
    }

    @Override
    public boolean step() {
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
        LocalPlayerComponent localPlayerComponent = playerEntity.getComponent(LocalPlayerComponent.class);
        if (localPlayerComponent.isDead) {
            playerEntity.send(new RespawnEvent());
        }
        return true;
    }

    @Override
    public int begin() {
        return 1;
    }

}
