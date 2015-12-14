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

import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.input.InputSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.players.LocalPlayerSystem;

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
