// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.common;

import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.logic.console.commandSystem.adapter.ParameterAdapterManager;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

/**
 *
 */
public class CommandSubsystem implements EngineSubsystem {

    @In
    private ContextAwareClassFactory classFactory;

    @Override
    public String getName() {
        return "Command";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        // TODO make ParameterAdapterManager injects to adapters
        classFactory.createToContext(ParameterAdapterManager.class, ParameterAdapterManager::createCore);
    }
}
