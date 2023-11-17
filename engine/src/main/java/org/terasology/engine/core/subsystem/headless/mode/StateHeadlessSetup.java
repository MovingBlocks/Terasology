// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.mode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.WorldGenerationConfig;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.LoggingContext;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.modes.AbstractState;
import org.terasology.engine.core.modes.StateLoading;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.module.StandardModuleExtension;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.engine.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.engine.world.time.WorldTime;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.naming.Name;

import java.util.List;

/**
 * The class is game selection menu replacement for the headless server.
 *
 */
public class StateHeadlessSetup extends AbstractState {

    private static final Logger logger = LoggerFactory.getLogger(StateHeadlessSetup.class);

    protected boolean strictModuleRequirements;

    private final NetworkMode networkMode;

    public StateHeadlessSetup() {
        this(NetworkMode.LISTEN_SERVER);
    }

    public StateHeadlessSetup(NetworkMode networkMode) {
        this.networkMode = networkMode;
    }

    @Override
    public void init(GameEngine gameEngine) {
        context = gameEngine.createChildContext();
        initEntityAndComponentManagers(true);
        createLocalPlayer(context);

        GameManifest gameManifest;
        List<GameInfo> savedGames = GameProvider.getSavedGames();
        if (savedGames.size() > 0) {
            gameManifest = savedGames.get(0).getManifest();
        } else {
            gameManifest = createGameManifest();
        }

        Config config = context.get(Config.class);
        WorldInfo worldInfo = gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD);
        config.getUniverseConfig().addWorldManager(worldInfo);
        config.getUniverseConfig().setSpawnWorldTitle(worldInfo.getTitle());
        config.getUniverseConfig().setUniverseSeed(gameManifest.getSeed());

        gameEngine.changeState(new StateLoading(gameManifest, networkMode));
    }

    public GameManifest createGameManifest() {
        GameManifest gameManifest = new GameManifest();

        Config config = context.get(Config.class);
        ModuleManager moduleManager = context.get(ModuleManager.class);
        for (Name moduleName : config.getDefaultModSelection().listModules()) {
            Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleName);
            if (module != null) {
                gameManifest.addModule(module.getId(), module.getVersion());
            } else if (strictModuleRequirements) {
                throw new RuntimeException("ModuleRegistry has no latest version for module " + moduleName);
            } else {
                logger.warn("ModuleRegistry has no latest version for module {}", moduleName);
            }
        }

        WorldGenerationConfig worldGenConfig = config.getWorldGeneration();

        // If no valid default world generator set then try to find one - no option to pick one manually in headless
        if (!worldGenConfig.getDefaultGenerator().isValid()) {

            // find the first gameplay module that is available, it should have a preferred world gen
            for (Name moduleName : config.getDefaultModSelection().listModules()) {
                Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleName);
                if (StandardModuleExtension.isGameplayModule(module)) {
                    SimpleUri defaultWorldGenerator = StandardModuleExtension.getDefaultWorldGenerator(module);
                    worldGenConfig.setDefaultGenerator(defaultWorldGenerator);
                    break;
                }
            }
        }
        SimpleUri worldGeneratorUri = worldGenConfig.getDefaultGenerator();

        gameManifest.setTitle(worldGenConfig.getWorldTitle());
        gameManifest.setSeed(worldGenConfig.getDefaultSeed());
        WorldInfo worldInfo = new WorldInfo(TerasologyConstants.MAIN_WORLD, gameManifest.getSeed(),
                (long) (WorldTime.DAY_LENGTH * WorldTime.NOON_OFFSET), worldGeneratorUri);
        gameManifest.addWorld(worldInfo);
        return gameManifest;
    }

    @Override
    public void handleInput(float delta) {
    }

    @Override
    public void update(float delta) {
        eventSystem.process();
    }

    @Override
    public void render() {
    }

    @Override
    public boolean isHibernationAllowed() {
        return true;
    }

    @Override
    public String getLoggingPhase() {
        return LoggingContext.INIT_PHASE;
    }
}
