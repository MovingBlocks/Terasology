// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.EngineTime;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.VariableStepLoadProcess;
import org.terasology.registry.In;
import org.terasology.rendering.world.WorldRenderer;

/**
 * Loops until world is pre-generated or 5 seconds elapsed.
 */
@ExpectedCost(5)
public class PrepareWorld extends VariableStepLoadProcess {

    @In
    private WorldRenderer worldRenderer;
    @In
    private EngineTime time;

    private long startTime;
    private long timeElapsed;

    @Override
    public String getMessage() {
        return "${engine:menu#catching-world}";
    }

    @Override
    public boolean step() {
        if (worldRenderer.pregenerateChunks()) {
            return true;
        }
        timeElapsed = time.getRealTimeInMs() - startTime;
        return timeElapsed > 5000;
    }

    @Override
    public void begin() {
        startTime = time.getRealTimeInMs();
    }

    @Override
    public float getProgress() {
        return (1 / Math.max(1f, 5000f / (float) timeElapsed));
    }
}
