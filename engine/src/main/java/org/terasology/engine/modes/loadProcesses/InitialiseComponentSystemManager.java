// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

@ExpectedCost(1)
public class InitialiseComponentSystemManager extends SingleStepLoadProcess {

    @In
    private ContextAwareClassFactory classFactory;

    @Override
    public String getMessage() {
        return "Initialising component system...";
    }

    @Override
    public boolean step() {
        classFactory.createInjectableInstance(ComponentSystemManager.class);
        return true;
    }
}
