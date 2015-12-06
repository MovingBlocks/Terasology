/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.engine.modes.loadProcesses;

import org.terasology.context.Context;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.generator.WorldGenerator;

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
        worldRenderer.getActiveCamera().setReflectionHeight(worldGenerator.getWorld().getSeaLevel());

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 5;
    }
}
