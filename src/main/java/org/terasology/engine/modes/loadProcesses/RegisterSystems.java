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

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.modes.LoadProcess;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.network.NetworkMode;

import java.util.Locale;
import java.util.Set;

/**
 * @author Immortius
 */
public class RegisterSystems implements LoadProcess {
    private static final Logger logger = LoggerFactory.getLogger(RegisterSystems.class);
    private NetworkMode netMode;
    private Set<String> registeredMods = Sets.newHashSet();
    private ModManager modManager;
    private ComponentSystemManager componentSystemManager;

    public RegisterSystems(NetworkMode netMode) {
        this.netMode = netMode;
    }

    @Override
    public String getMessage() {
        return "Registering systems...";
    }

    @Override
    public boolean step() {
        componentSystemManager = CoreRegistry.get(ComponentSystemManager.class);
        modManager = CoreRegistry.get(ModManager.class);

        componentSystemManager.loadSystems(ModManager.ENGINE_PACKAGE, modManager.getEngineReflections(), netMode);
        for (Mod mod : modManager.getActiveMods()) {
            if (!registeredMods.contains(mod.getModInfo().getId().toLowerCase(Locale.ENGLISH))) {
                loadMod(mod);
            }
        }
        return true;
    }

    private void loadMod(Mod mod) {
        logger.debug("Loading {}", mod.getModInfo().getId());
        for (String dependency : mod.getModInfo().getDependencies()) {
            if (!registeredMods.contains(dependency.toLowerCase(Locale.ENGLISH))) {
                logger.debug("Requesting {} due to dependency", dependency);
                loadMod(modManager.getMod(dependency));
            }
        }
        if (mod.isCodeMod()) {
            componentSystemManager.loadSystems(mod.getModInfo().getId(), mod.getReflections(), netMode);
        }
        registeredMods.add(mod.getModInfo().getId().toLowerCase(Locale.ENGLISH));
    }

    @Override
    public int begin() {
        return 1;
    }
}
