// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.context.Lifetime;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.core.module.rendering.RenderingModuleRegistry;
import org.terasology.gestalt.di.ServiceRegistry;

/**
 * Add {@link RenderingModuleRegistry} to the game {@link ServiceRegistry}.
 * 
 * The rendering system is required whenever a client starts or joins a game. As rendering may fail to re-initialise
 * correctly when it has previously been constructed, this loading process will populate the {@link ServiceRegistry} with a
 * freshly created rendering system.
 * 
 * When switching the game state, the rendering system can just be disposed with the old state.
 */
public class InitialiseRendering extends SingleStepLoadProcess {
    private final ServiceRegistry serviceRegistry;

    public InitialiseRendering(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }


    @Override
    public String getMessage() {
        return "Initialising Rendering System...";
    }

    @Override
    public boolean step() {
        RenderingModuleRegistry renderingModuleRegistry = new RenderingModuleRegistry();
        serviceRegistry.with(RenderingModuleRegistry.class).lifetime(Lifetime.Singleton).use(() -> renderingModuleRegistry);
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
