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

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.engine.module.DependencyInfo;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.UriUtil;
import org.terasology.network.NetworkMode;

import java.util.Locale;
import java.util.Set;

/**
 * @author Immortius
 */
public class RegisterSystems extends SingleStepLoadProcess {
    private static final Logger logger = LoggerFactory.getLogger(RegisterSystems.class);
    private NetworkMode netMode;
    private Set<String> registeredModules = Sets.newHashSet();
    private ModuleManager moduleManager;
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
        moduleManager = CoreRegistry.get(ModuleManager.class);

        for (Module module : moduleManager.getActiveModules()) {
            if (!registeredModules.contains(module.getId())) {
                loadModule(module);
            }
        }
        return true;
    }

    private void loadModule(Module module) {
        logger.debug("Loading {}", module);
        for (DependencyInfo dependency : module.getModuleInfo().getDependencies()) {
            if (!registeredModules.contains(UriUtil.normalise(dependency.getId()))) {
                logger.debug("Requesting {} due to dependency", dependency);
                loadModule(moduleManager.getLatestModuleVersion(dependency.getId()));
            }
        }
        if (module.isCodeModule()) {
            componentSystemManager.loadSystems(module.getId(), module.getReflections(), netMode);
        }
        registeredModules.add(module.getId().toLowerCase(Locale.ENGLISH));
    }

}
