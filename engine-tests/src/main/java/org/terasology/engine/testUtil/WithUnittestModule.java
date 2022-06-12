// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.testUtil;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.gestalt.module.Module;

/**
 * This subsystem registers the {@code unittest} module containing stub component classes with the {@link ModuleManager} before the engine
 * is initialized.
 * <p>
 * It is indended to be used on the MTE side by adding it to the list of subsystems given to the builder.
 * <p>
 * NOTE: The usage of this subsystem for MTE is potentially fragile as it depends on specific assumptions on when subsystems are initialized.
 * This makes things work because {@code Subsystem.initialize} is called at a useful place in the engine's boot
 * sequence, but it's very possible that something else will call {@code ModuleManager.loadEnvironment} later — without including "unittest"
 * in the environment — and then things might break with an unclear error again.
 */
public class WithUnittestModule implements EngineSubsystem {
    @Override
    public String getName() {
        return "Unittest";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        EngineSubsystem.super.initialise(engine, rootContext);
        ModuleManager manager = rootContext.get(ModuleManager.class);
        registerUnittestModule(manager);
    }

    public static void registerUnittestModule(ModuleManager manager) {
        Module unittestModule = manager.registerPackageModule("org.terasology.unittest");
        manager.resolveAndLoadEnvironment(unittestModule.getId());
    }
}
