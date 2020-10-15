// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.rendering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.module.rendering.RenderingModuleRegistry;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

public class ModuleRenderingSubsystem implements EngineSubsystem {
    private static final Logger logger = LoggerFactory.getLogger(ModuleRenderingSubsystem.class);

    @In
    private ContextAwareClassFactory classFactory;

    @Override
    public String getName() {
        return "ModuleRendering";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        classFactory.createToContext(RenderingModuleRegistry.class);
    }

}
