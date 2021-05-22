// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.physics.CollisionGroupManager;


public class PhysicsSubsystem implements EngineSubsystem {
    @Override
    public String getName() {
        return "Physics";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        rootContext.put(CollisionGroupManager.class, new CollisionGroupManager());
    }
}
