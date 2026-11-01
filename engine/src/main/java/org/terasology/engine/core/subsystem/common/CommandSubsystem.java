// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import org.terasology.context.Lifetime;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.logic.console.commandSystem.adapter.ParameterAdapterManager;
import org.terasology.gestalt.di.ServiceRegistry;

import javax.inject.Inject;


public class CommandSubsystem implements EngineSubsystem {

    @Inject
    public CommandSubsystem() {
    }

    @Override
    public String getName() {
        return "Command";
    }

    @Override
    public void initialise(GameEngine engine, ServiceRegistry serviceRegistry) {
        ParameterAdapterManager parameterAdapterManager = ParameterAdapterManager.createCore();
        serviceRegistry.with(ParameterAdapterManager.class).lifetime(Lifetime.Singleton).use(() -> parameterAdapterManager);
    }
}
