// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import org.terasology.context.Lifetime;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.physics.CollisionGroupManager;
import org.terasology.gestalt.di.ServiceRegistry;

import javax.inject.Inject;

public class PhysicsSubsystem implements EngineSubsystem {

    @Inject
    public PhysicsSubsystem() {
    }

    @Override
    public String getName() {
        return "Physics";
    }

    @Override
    public void initialise(GameEngine engine, ServiceRegistry serviceRegistry) {
        CollisionGroupManager collisionGroupManager = new CollisionGroupManager();
        serviceRegistry.with(CollisionGroupManager.class).lifetime(Lifetime.Singleton).use(() -> collisionGroupManager);
    }
}
