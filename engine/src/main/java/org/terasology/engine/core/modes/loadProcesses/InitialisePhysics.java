// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.physics.Physics;
import org.terasology.engine.physics.engine.PhysicsEngine;
import org.terasology.engine.physics.engine.PhysicsEngineManager;

/**
 */
public class InitialisePhysics extends SingleStepLoadProcess {
    private final Context context;

    public InitialisePhysics(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Turning on gravity";
    }

    @Override
    public boolean step() {
        PhysicsEngine physicsEngine = PhysicsEngineManager.getNewPhysicsEngine(context);
        context.put(Physics.class, physicsEngine);
        context.put(PhysicsEngine.class, physicsEngine);
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
