// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.context.Lifetime;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.gestalt.di.ServiceRegistry;

public class InitialiseComponentSystemManager extends SingleStepLoadProcess {

    private final ServiceRegistry serviceRegistry;

    public InitialiseComponentSystemManager(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public String getMessage() {
        return "Initialising component system...";
    }

    @Override
    public boolean step() {
        serviceRegistry.with(ComponentSystemManager.class).lifetime(Lifetime.Singleton).use(ComponentSystemManager.class);
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

}
