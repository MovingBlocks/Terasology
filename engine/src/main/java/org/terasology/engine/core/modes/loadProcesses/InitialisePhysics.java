// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.physics.engine.PhysicsEngineManager;
import org.terasology.gestalt.di.ServiceRegistry;

public class InitialisePhysics extends SingleStepLoadProcess {
    private final Context context;
    private final ServiceRegistry serviceRegistry;

    public InitialisePhysics(Context context, ServiceRegistry serviceRegistry) {
        this.context = context;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public String getMessage() {
        return "Turning on gravity";
    }

    @Override
    public boolean step() {
        PhysicsEngineManager.registerPhysicsEngine(serviceRegistry);
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
