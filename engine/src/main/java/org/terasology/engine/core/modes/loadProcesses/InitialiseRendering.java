// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.core.module.rendering.RenderingModuleRegistry;

/**
 * Add {@link RenderingModuleRegistry} to the game {@link Context}.
 * 
 * The rendering system is required whenever a client starts or joins a game. As rendering may fail to re-initialise
 * correctly when it has previously been constructed, this loading process will populate the {@link Context} with a
 * freshly created rendering system.
 * 
 * When switching the game state, the rendering system can just be disposed with the old state.
 */
public class InitialiseRendering extends SingleStepLoadProcess {
    private final Context context;

    public InitialiseRendering(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Initialising Rendering System...";
    }

    @Override
    public boolean step() {
        // NOTE: a {@link RenderingModuleRegistry} is also required during game setup to configure rendering.
        // Thus, the rendering module registry might already be added to the context beforehand.
        if (context.get(RenderingModuleRegistry.class) != null) {
            context.put(RenderingModuleRegistry.class, new RenderingModuleRegistry());
        }
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
