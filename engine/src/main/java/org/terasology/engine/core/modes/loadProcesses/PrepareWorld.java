// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.modes.VariableStepLoadProcess;
import org.terasology.engine.rendering.world.WorldRenderer;

/**
 * Loops until world is pre-generated or 5 seconds elapsed.
 */
public class PrepareWorld extends VariableStepLoadProcess {

    private final Context context;
    private long startTime;
    private WorldRenderer worldRenderer;
    private long timeElapsed;

    public PrepareWorld(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "${engine:menu#catching-world}";
    }

    @Override
    public boolean step() {
        if (worldRenderer.pregenerateChunks()) {
            return true;
        }
        EngineTime time = (EngineTime) context.get(Time.class);
        timeElapsed = time.getRealTimeInMs() - startTime;
        return timeElapsed > 5000;
    }

    @Override
    public void begin() {
        worldRenderer = context.get(WorldRenderer.class);
        EngineTime time = (EngineTime) context.get(Time.class);
        startTime = time.getRealTimeInMs();
    }

    @Override
    public float getProgress() {
        return (1/Math.max(1f, 5000f / (float) timeElapsed));
    }

    @Override
    public int getExpectedCost() {
        return 5;
    }

}
