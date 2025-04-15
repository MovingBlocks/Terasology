// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import org.terasology.context.Lifetime;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.world.generator.internal.WorldGeneratorManager;
import org.terasology.gestalt.di.ServiceRegistry;

import javax.inject.Inject;


public class WorldGenerationSubsystem implements EngineSubsystem {
    @Inject
    protected ModuleManager moduleManager;

    @Inject
    public WorldGenerationSubsystem() {
    }

    @Override
    public String getName() {
        return "World Generation";
    }

    @Override
    public void initialise(GameEngine engine, ServiceRegistry serviceRegistry) {
        WorldGeneratorManager worldGeneratorManager = new WorldGeneratorManager(moduleManager);
        serviceRegistry.with(WorldGeneratorManager.class).lifetime(Lifetime.Singleton).use(() -> worldGeneratorManager);
    }
}
