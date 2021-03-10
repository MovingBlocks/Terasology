/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
