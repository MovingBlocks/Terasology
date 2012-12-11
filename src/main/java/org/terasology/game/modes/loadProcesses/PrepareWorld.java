/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.game.modes.loadProcesses;

import org.lwjgl.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.game.CoreRegistry;
import org.terasology.game.modes.LoadProcess;
import org.terasology.rendering.world.WorldRenderer;

/**
 * @author Immortius
 */
public class PrepareWorld implements LoadProcess {
    private static final Logger logger = LoggerFactory.getLogger(PrepareWorld.class);

    private long startTime;
    private WorldRenderer worldRenderer;

    @Override
    public String getMessage() {
        return "Caching World...";
    }

    @Override
    public boolean step() {
        if (worldRenderer.pregenerateChunks()) {
            return true;
        }
        long totalTime = Sys.getTime() * 1000 / Sys.getTimerResolution() - startTime;
        return totalTime > 5000;
    }

    @Override
    public int begin() {
        worldRenderer = CoreRegistry.get(WorldRenderer.class);
        startTime = Sys.getTime() * 1000 / Sys.getTimerResolution();
        return UNKNOWN_STEPS;
    }

}
