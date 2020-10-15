// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.registry.In;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.generator.WorldGenerator;

/**
 * Initialize the world generator.
 * <br><br>
 * This is done after the world entity has been created/loaded so that world generation config. is available at the time
 * of initialization.
 */
@ExpectedCost(5)
public class InitialiseWorldGenerator extends SingleStepLoadProcess {

    @In
    private WorldGenerator worldGenerator;
    @In
    private WorldRenderer worldRenderer;

    @Override
    public String getMessage() {
        return "Initialize world generator ...";
    }

    @Override
    public boolean step() {
        worldGenerator.initialize();
        worldRenderer.getActiveCamera().setReflectionHeight(worldGenerator.getWorld().getSeaLevel() + 0.5f);
        return true;
    }
}
