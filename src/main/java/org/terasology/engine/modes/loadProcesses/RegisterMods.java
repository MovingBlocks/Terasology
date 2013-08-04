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

package org.terasology.engine.modes.loadProcesses;

import org.terasology.asset.AssetManager;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.LoadProcess;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.module.ModuleManager;
import org.terasology.game.GameManifest;
import org.terasology.engine.module.Module;

/**
 * @author Immortius
 */
public class RegisterMods implements LoadProcess {

    private GameManifest gameManifest;

    public RegisterMods(GameManifest gameManifest) {
        this.gameManifest = gameManifest;
    }

    @Override
    public String getMessage() {
        return "Registering Mods...";
    }

    @Override
    public boolean step() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        for (Module module : moduleManager.getMods()) {
            module.setEnabled(false);
        }

        for (String modName : gameManifest.getModConfiguration().listMods()) {
            Module module = moduleManager.getMod(modName);
            if (module != null) {
                module.setEnabled(true);
            } else {
                CoreRegistry.get(GameEngine.class).changeState(new StateMainMenu("Missing required module: " + modName));
            }
        }

        moduleManager.applyActiveMods();
        AssetManager.getInstance().clear();
        AssetManager.getInstance().applyOverrides();
        return true;
    }

    @Override
    public int begin() {
        return 1;
    }

}
