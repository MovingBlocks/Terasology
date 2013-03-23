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

import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.modes.LoadProcess;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.network.NetworkMode;

/**
 * @author Immortius
 */
public class RegisterSystems implements LoadProcess {
    private NetworkMode netMode;

    public RegisterSystems(NetworkMode netMode) {
        this.netMode = netMode;
    }

    @Override
    public String getMessage() {
        return "Registering systems...";
    }

    @Override
    public boolean step() {

        ComponentSystemManager componentSystemManager = CoreRegistry.get(ComponentSystemManager.class);
        ModManager modManager = CoreRegistry.get(ModManager.class);

        componentSystemManager.loadSystems(ModManager.ENGINE_PACKAGE, modManager.getEngineReflections(), netMode);
        for (Mod mod : modManager.getActiveMods()) {
            if (mod.isCodeMod()) {
                componentSystemManager.loadSystems(mod.getModInfo().getId(), mod.getReflections(), netMode);
            }
        }
        return true;
    }

    @Override
    public int begin() {
        return 1;
    }
}
