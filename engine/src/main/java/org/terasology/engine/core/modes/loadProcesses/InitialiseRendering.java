// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.core.module.rendering.RenderingModuleRegistry;

/**
 * Add {@link RenderingModuleRegistry} to the game {@link Context}.
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
        context.put(RenderingModuleRegistry.class, new RenderingModuleRegistry());
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
