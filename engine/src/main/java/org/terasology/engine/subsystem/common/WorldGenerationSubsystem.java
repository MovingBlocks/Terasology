// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.common;

import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;
import org.terasology.world.generator.internal.WorldGeneratorManager;

/**
 *
 */
public class WorldGenerationSubsystem implements EngineSubsystem {

    @In
    private ContextAwareClassFactory classFactory;

    @Override
    public String getName() {
        return "World Generation";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        classFactory.createInjectableInstance(WorldGeneratorManager.class);
    }
}
