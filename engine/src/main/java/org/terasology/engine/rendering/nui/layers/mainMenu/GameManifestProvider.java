/*
 * Copyright 2019 MovingBlocks
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
import org.terasology.registry.In;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.time.WorldTime;


/**
 * Generates new games manifest according to input data.
 */
public class GameManifestProvider {

    private static final Logger logger = LoggerFactory.getLogger(GameManifestProvider.class);

    @In
    private Config config;

    private GameManifestProvider() {
    }

    /**
     * Generates game manifest with default settings (title, seed) if not specified.
     * Uses default world generator, and modules selection.
     * @TODO: rewrite/fix it when code will be more stable
     *
     * @param universeWrapper  contains the universe level properties
     * @param moduleManager    resolves modules
     * @param config           provides default module selection, world generator
     * @return                 game manifest with default settings
     */
    public static GameManifest createGameManifest(final UniverseWrapper universeWrapper, final ModuleManager moduleManager, final Config config) {
        GameManifest gameManifest = new GameManifest();
        if (StringUtils.isNotBlank(universeWrapper.getGameName())) {
            gameManifest.setTitle(GameProvider.getNextGameName(universeWrapper.getGameName()));
        } else {
            gameManifest.setTitle(GameProvider.getNextGameName());
        }


        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        ResolutionResult result = resolver.resolve(config.getDefaultModSelection().listModules());
        if (!result.isSuccess()) {
            logger.error("Can't resolve dependencies");
            return null;
        }
        for (Module module : result.getModules()) {
            gameManifest.addModule(module.getId(), module.getVersion());
        }

        SimpleUri uri;
        String seed;
        if (universeWrapper.getTargetWorld() != null) {
            uri = universeWrapper.getTargetWorld().getWorldGenerator().getUri();
            seed = universeWrapper.getTargetWorld().getWorldGenerator().getWorldSeed();
        } else {
            uri = config.getWorldGeneration().getDefaultGenerator();
            seed = universeWrapper.getSeed();
        }
        gameManifest.setSeed(seed);

        String targetWorldName = "";
        if (universeWrapper.getTargetWorld() != null) {
            targetWorldName = universeWrapper.getTargetWorld().getWorldName().toString();
        }
        // This is multiplied by the number of seconds in a day (86400000) to determine the exact  millisecond at which the game will start.
        WorldInfo worldInfo = new WorldInfo(TerasologyConstants.MAIN_WORLD, targetWorldName, seed,
                (long) (WorldTime.DAY_LENGTH * WorldTime.SUNRISE_OFFSET), uri);

        gameManifest.addWorld(worldInfo);
        config.getUniverseConfig().addWorldManager(worldInfo);
        config.getUniverseConfig().setSpawnWorldTitle(worldInfo.getTitle());
        config.getUniverseConfig().setUniverseSeed(universeWrapper.getSeed());
        return gameManifest;
    }
}
