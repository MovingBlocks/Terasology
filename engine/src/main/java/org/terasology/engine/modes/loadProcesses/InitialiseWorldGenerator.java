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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.TerasologyConstants;
import org.terasology.game.GameManifest;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.internal.WorldInfo;

/**
 * @author Martin Steiger
 */
public class InitialiseWorldGenerator extends SingleStepLoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(InitialiseWorldGenerator.class);

    private GameManifest gameManifest;

    public InitialiseWorldGenerator(GameManifest gameManifest) {
        this.gameManifest = gameManifest;
    }

    @Override
    public String getMessage() {
        return "Generating world ...";
    }

    @Override
    public boolean step() {

        WorldGenerator worldGenerator;
        WorldInfo worldInfo = gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD);
        worldGenerator = CoreRegistry.get(WorldGenerator.class);
        worldGenerator.initialize();
        worldGenerator.setWorldSeed(worldInfo.getSeed());

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 5;
    }
}
