// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.input.cameraTarget.CameraTargetSystem;
import org.terasology.engine.logic.players.LocalPlayerSystem;

/**
 */
public class RegisterInputSystem extends SingleStepLoadProcess {

    private final Context context;

    public RegisterInputSystem(Context context) {
        this.context = context;
    }
    @Override
    public String getMessage() {
        return "Setting up Input Systems...";
    }

    @Override
    public boolean step() {
        ComponentSystemManager componentSystemManager = context.get(ComponentSystemManager.class);

        LocalPlayerSystem localPlayerSystem = new LocalPlayerSystem();
        componentSystemManager.register(localPlayerSystem, "engine:localPlayerSystem");
        context.put(LocalPlayerSystem.class, localPlayerSystem);

        CameraTargetSystem cameraTargetSystem = new CameraTargetSystem();
        context.put(CameraTargetSystem.class, cameraTargetSystem);
        componentSystemManager.register(cameraTargetSystem, "engine:CameraTargetSystem");

        InputSystem inputSystem = context.get(InputSystem.class);
        componentSystemManager.register(inputSystem, "engine:InputSystem");

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
