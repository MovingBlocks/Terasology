// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.context.Context;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleImpl;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

public class InitialiseCommandSystem extends SingleStepLoadProcess {

    @In //TODO FIXME - StateLoading not have di yet
    private ContextAwareClassFactory classFactory;

    public InitialiseCommandSystem(Context context) {

    }

    @Override
    public String getMessage() {
        return "Initialising Command System...";
    }

    @Override
    public boolean step() {
        classFactory.createInjectableInstance(Console.class, ConsoleImpl.class);
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
