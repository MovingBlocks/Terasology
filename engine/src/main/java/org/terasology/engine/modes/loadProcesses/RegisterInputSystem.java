// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.input.InputSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.players.LocalPlayerSystem;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

/**
 *
 */
@ExpectedCost(1)
public class RegisterInputSystem extends SingleStepLoadProcess {

    @In
    private ComponentSystemManager componentSystemManager;
    @In
    private ContextAwareClassFactory classFactory;
    @In
    private InputSystem inputSystem;

    @Override
    public String getMessage() {
        return "Setting up Input Systems...";
    }

    @Override
    public boolean step() {
        componentSystemManager.register(classFactory.createInjectableInstance(LocalPlayerSystem.class), "engine" +
                ":localPlayerSystem");
        componentSystemManager.register(classFactory.createInjectableInstance(CameraTargetSystem.class), "engine" +
                ":CameraTargetSystem");
        componentSystemManager.register(inputSystem, "engine:InputSystem");
        return true;
    }
}
