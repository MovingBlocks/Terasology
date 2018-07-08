/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.module.ModuleManager;
import org.terasology.game.GameManifest;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ResolutionResult;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.time.WorldTime;


/**
 * Generates new games manifest according to input data.
 */
public class GameManifestProvider {

    private static final Logger logger = LoggerFactory.getLogger(GameManifestProvider.class);

    private GameManifestProvider() {
    }

    /**
     * Generates game manifest with default settings (world generator, list of modules).
     *
     * @param universeWrapper  contains the universe level properties
     * @param moduleManager    resolves modules
     * @param config           provides default module selection, world generator
     * @return                 game manifest with default settings
     */
    public static GameManifest createDefaultGameManifest(final UniverseWrapper universeWrapper, final ModuleManager moduleManager, final Config config) {
        GameManifest gameManifest = new GameManifest();
        if (StringUtils.isNotBlank(universeWrapper.getGameName())) {
            gameManifest.setTitle(universeWrapper.getGameName());
        } else {
            gameManifest.setTitle(GameProvider.getNextGameName());
        }

        String seed;
        if (StringUtils.isNotBlank(universeWrapper.getSeed())) {
            seed = universeWrapper.getSeed();
        } else {
            seed = new FastRandom().nextString(32);
        }
        gameManifest.setSeed(seed);

        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        ResolutionResult result = resolver.resolve(config.getDefaultModSelection().listModules());
        if (!result.isSuccess()) {
            logger.error("Can't resolve dependencies");
            return null;
        }
        for (Module module : result.getModules()) {
            gameManifest.addModule(module.getId(), module.getVersion());
        }

        SimpleUri uri = config.getWorldGeneration().getDefaultGenerator();
        // This is multiplied by the number of seconds in a day (86400000) to determine the exact  millisecond at which the game will start.
        final float timeOffset = 0.50f;
        WorldInfo worldInfo = new WorldInfo(TerasologyConstants.MAIN_WORLD, seed,
                (long) (WorldTime.DAY_LENGTH * timeOffset), uri);

        gameManifest.addWorld(worldInfo);
        return gameManifest;
    }
}
