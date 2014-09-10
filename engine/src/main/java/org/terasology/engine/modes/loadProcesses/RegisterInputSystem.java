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
import org.terasology.config.BindsConfig;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.module.ModuleManager;
import org.terasology.input.InputSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.players.LocalPlayerSystem;
import org.terasology.registry.CoreRegistry;

/**
 * @author Immortius
 */
public class RegisterInputSystem extends SingleStepLoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(RegisterInputSystem.class);

    @Override
    public String getMessage() {
        return "Setting up Input Systems...";
    }

    @Override
    public boolean step() {
        ComponentSystemManager componentSystemManager = CoreRegistry.get(ComponentSystemManager.class);
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);

        LocalPlayerSystem localPlayerSystem = new LocalPlayerSystem();
        componentSystemManager.register(localPlayerSystem, "engine:localPlayerSystem");
        CoreRegistry.put(LocalPlayerSystem.class, localPlayerSystem);

        CameraTargetSystem cameraTargetSystem = new CameraTargetSystem();
        CoreRegistry.put(CameraTargetSystem.class, cameraTargetSystem);
        componentSystemManager.register(cameraTargetSystem, "engine:CameraTargetSystem");

        BindsConfig bindsConfig = CoreRegistry.get(Config.class).getInput().getBinds();
        InputSystem inputSystem = CoreRegistry.get(InputSystem.class);
        componentSystemManager.register(inputSystem, "engine:InputSystem");

        bindsConfig.applyBinds(inputSystem, moduleManager);

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
