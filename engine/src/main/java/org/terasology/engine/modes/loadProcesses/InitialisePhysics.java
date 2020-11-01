/*
 * Copyright 2014 MovingBlocks
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
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.physics.Physics;
import org.terasology.physics.engine.PhysicsEngine;
import org.terasology.physics.engine.PhysicsEngineManager;

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
