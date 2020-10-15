// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyEngine;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.network.NetworkMode;
import org.terasology.registry.In;

@ExpectedCost(1)
public class RegisterSystems extends SingleStepLoadProcess {

    @In
    private NetworkMode netMode;
    @In
    private ComponentSystemManager componentSystemManager;
    @In
    private ModuleManager moduleManager;
    @In
    private GameEngine gameEngine;

    @Override
    public String getMessage() {
        return "Registering systems...";
    }

    @Override
    public boolean step() {
        TerasologyEngine terasologyEngine = (TerasologyEngine) gameEngine;
        for (EngineSubsystem subsystem : terasologyEngine.getSubsystems()) {
            subsystem.registerSystems(componentSystemManager);
        }
        componentSystemManager.loadSystems(moduleManager.getEnvironment(), netMode);

        return true;
    }
}
