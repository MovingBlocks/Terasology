// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.common;

import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

/**
 *
 */
public class PhysicsSubsystem implements EngineSubsystem {

    @In
    private ContextAwareClassFactory classFactory;

    @Override
    public String getName() {
        return "Physics";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        classFactory.createInjectableInstance(CollisionGroupManager.class);
    }
}
