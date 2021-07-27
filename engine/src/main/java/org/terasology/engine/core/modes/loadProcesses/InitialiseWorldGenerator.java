// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.world.generator.WorldGenerator;

/**
 * Initialize the world generator.
 * <br><br>
 * This is done after the world entity has been created/loaded so that
 * world generation config. is available at the time of initialization.
 */
public class InitialiseWorldGenerator extends SingleStepLoadProcess {

    private final Context context;

    public InitialiseWorldGenerator(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Initialize world generator ...";
    }

    @Override
    public boolean step() {

        WorldGenerator worldGenerator = context.get(WorldGenerator.class);
        worldGenerator.initialize();

        WorldRenderer worldRenderer = context.get(WorldRenderer.class);
        worldRenderer.getActiveCamera().setReflectionHeight(worldGenerator.getWorld().getSeaLevel() + 0.5f);

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 5;
    }
}
