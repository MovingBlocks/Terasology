// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.logic.console.commandSystem.adapter.ParameterAdapterManager;

/**
 *
 */
public class CommandSubsystem implements EngineSubsystem {
    @Override
    public String getName() {
        return "Command";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        rootContext.put(ParameterAdapterManager.class, ParameterAdapterManager.createCore());
    }
}
