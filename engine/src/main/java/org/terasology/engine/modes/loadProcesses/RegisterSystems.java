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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyEngine;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.network.NetworkMode;
import org.terasology.registry.CoreRegistry;

/**
 * @author Immortius
 */
public class RegisterSystems extends SingleStepLoadProcess {
    private static final Logger logger = LoggerFactory.getLogger(RegisterSystems.class);
    private NetworkMode netMode;
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
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);

        TerasologyEngine terasologyEngine = (TerasologyEngine) CoreRegistry.get(GameEngine.class);
        for (EngineSubsystem subsystem : terasologyEngine.getSubsystems()) {
            subsystem.registerSystems(componentSystemManager);
        }
        componentSystemManager.loadSystems(moduleManager.getEnvironment(), netMode);

        return true;
    }


    @Override
    public int getExpectedCost() {
        return 1;
    }

}
