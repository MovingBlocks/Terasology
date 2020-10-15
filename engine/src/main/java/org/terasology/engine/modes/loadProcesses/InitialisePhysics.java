// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.modes.loadProcesses;

import org.terasology.context.Context;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.physics.Physics;
import org.terasology.physics.engine.PhysicsEngine;
import org.terasology.physics.engine.PhysicsEngineManager;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

/**
 *
 */
@ExpectedCost(1)
public class InitialisePhysics extends SingleStepLoadProcess {

    @In
    private ContextAwareClassFactory classFactory;
    @In
    private Context context;

    @Override
    public String getMessage() {
        return "Turning on gravity";
    }

    @Override
    public boolean step() {
        PhysicsEngine physicsEngine = classFactory.createToContext(PhysicsEngine.class,
                () -> PhysicsEngineManager.getNewPhysicsEngine(context));
        context.put(Physics.class, physicsEngine);
        return true;
    }
}
