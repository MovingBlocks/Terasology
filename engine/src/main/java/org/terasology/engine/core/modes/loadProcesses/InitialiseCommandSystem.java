// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.context.Lifetime;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.ConsoleImpl;
import org.terasology.gestalt.di.ServiceRegistry;

public class InitialiseCommandSystem extends SingleStepLoadProcess {

    private ServiceRegistry serviceRegistry;

    public InitialiseCommandSystem(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public String getMessage() {
        return "Initialising Command System...";
    }

    @Override
    public boolean step() {
        serviceRegistry.with(Console.class).lifetime(Lifetime.Singleton).use(ConsoleImpl.class);
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
