// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.headless;

import org.terasology.context.Context;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.input.InputSystem;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

public class HeadlessInput implements EngineSubsystem {

    @In
    ContextAwareClassFactory classFactory;

    @Override
    public String getName() {
        return "Input";
    }

    @Override
    public void postInitialise(Context context) {
        classFactory.createToContext(InputSystem.class);
    }

}
