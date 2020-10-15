// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleImpl;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

@ExpectedCost(1)
public class InitialiseCommandSystem extends SingleStepLoadProcess {

    @In
    private ContextAwareClassFactory classFactory;

    @Override
    public String getMessage() {
        return "Initialising Command System...";
    }

    @Override
    public boolean step() {
        classFactory.createToContext(ConsoleImpl.class, Console.class);
        return true;
    }
}
