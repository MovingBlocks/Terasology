// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.testUtil;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.gestalt.module.Module;

public class WithUnittestModule implements EngineSubsystem {
    @Override
    public String getName() {
        return "Unittest";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        EngineSubsystem.super.initialise(engine, rootContext);
        ModuleManager manager = rootContext.get(ModuleManager.class);
        Module unittestModule = manager.registerPackageModule("org.terasology.unittest");
        manager.resolveAndLoadEnvironment(unittestModule.getId());
    }
}
