// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.world.generator.internal.WorldGeneratorManager;

/**
 *
 */
public class WorldGenerationSubsystem implements EngineSubsystem {
    @Override
    public String getName() {
        return "World Generation";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        rootContext.put(WorldGeneratorManager.class, new WorldGeneratorManager(rootContext));
    }
}
