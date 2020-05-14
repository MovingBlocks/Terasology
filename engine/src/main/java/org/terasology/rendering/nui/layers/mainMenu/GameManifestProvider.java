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
import org.terasology.engine.module.DependencyResolutionFailedException;
import org.terasology.engine.module.ModuleManager;
import org.terasology.game.GameManifest;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ResolutionResult;
import org.terasology.registry.In;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.world.generator.WorldConfigurator;
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
     *
     * @param universeWrapper contains the universe level properties
     * @param moduleManager   resolves modules
     * @param config          provides default module selection, world generator
     * @return game manifest with default settings
     * @TODO: rewrite/fix it when code will be more stable
     */
    public static GameManifest createGameManifest(final UniverseWrapper universeWrapper,
                                                  final ModuleManager moduleManager,
                                                  final Config config) {

        GameManifest gameManifest = new GameManifest();

        try {
            addModulesTo(gameManifest, new DependencyResolver(moduleManager.getRegistry()), config);
        } catch (DependencyResolutionFailedException e) {
            logger.error(e.getMessage());
            return null;
        }

        gameManifest.setTitle(getGameTitle(universeWrapper.getGameName()));
        gameManifest.setSeed(getSeed(universeWrapper));

        // This is multiplied by the number of seconds in a day (86400000) to determine the exact  millisecond at which the game will start.
        long startTime = (long) (WorldTime.DAY_LENGTH * WorldTime.NOON_OFFSET);
        SimpleUri uri = getGeneratorUri(universeWrapper, config);
        String targetWorldName = getTargetWorldName(universeWrapper);

        WorldInfo worldInfo = new WorldInfo(TerasologyConstants.MAIN_WORLD, targetWorldName, gameManifest.getSeed(), startTime, uri);
        final WorldConfigurator worldConfigurator = universeWrapper.getTargetWorld().getWorldConfigurator();
        worldInfo.setConfigurator(worldConfigurator);

        gameManifest.addWorld(worldInfo);
        config.getUniverseConfig().addWorldManager(worldInfo);
        config.getUniverseConfig().setSpawnWorldTitle(worldInfo.getTitle());
        config.getUniverseConfig().setUniverseSeed(universeWrapper.getSeed());
        return gameManifest;
    }

    /**
     * Determine the game title from given string.
     * <p>
     * If the given {@code name} is null or empty a random name is chosen.
     *
     * @param name     the title, may be null or empty to choose a random name
     */
    private static String getGameTitle(String name) {
        return StringUtils.isNotBlank(name) ? name : GameProvider.getNextGameName();
    }

    private static void addModulesTo(GameManifest manifest, DependencyResolver resolver, Config config) throws DependencyResolutionFailedException {
        ResolutionResult result = resolver.resolve(config.getDefaultModSelection().listModules());
        if (!result.isSuccess()) {
            throw new DependencyResolutionFailedException("Can't resolve dependencies");
        }
        for (Module module : result.getModules()) {
            manifest.addModule(module.getId(), module.getVersion());
        }
    }

    private static String getSeed(UniverseWrapper universeWrapper) {
        String seed;
        if (universeWrapper.getTargetWorld() != null) {
            seed = universeWrapper.getTargetWorld().getWorldGenerator().getWorldSeed();
        } else {
            seed = universeWrapper.getSeed();
        }
        return seed;
    }

    private static SimpleUri getGeneratorUri(UniverseWrapper universeWrapper, Config config) {
        SimpleUri uri;
        if (universeWrapper.getTargetWorld() != null) {
            uri = universeWrapper.getTargetWorld().getWorldGenerator().getUri();
        } else {
            uri = config.getWorldGeneration().getDefaultGenerator();
        }
        return uri;
    }

    private static String getTargetWorldName(UniverseWrapper universeWrapper) {
        String targetWorldName = "";
        if (universeWrapper.getTargetWorld() != null) {
            targetWorldName = universeWrapper.getTargetWorld().getWorldName().toString();
        }
        return targetWorldName;
    }
}
