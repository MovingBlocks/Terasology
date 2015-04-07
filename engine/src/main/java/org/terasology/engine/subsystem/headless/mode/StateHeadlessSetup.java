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
package org.terasology.engine.subsystem.headless.mode;

import org.terasology.config.Config;
import org.terasology.config.WorldGenerationConfig;
import org.terasology.engine.GameEngine;
import org.terasology.engine.LoggingContext;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.modes.StateSetup;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.StandardModuleExtension;
import org.terasology.game.GameManifest;
import org.terasology.module.Module;
import org.terasology.naming.Name;
import org.terasology.network.NetworkMode;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.time.WorldTime;

import java.util.List;

/**
 * The class is game selection menu replacement for the headless server.
 *
 * @author Benjamin Glatzel
 * @author Anton Kireev
 * @author Marcel Lehwald
 * @author Florian
 */
public class StateHeadlessSetup extends StateSetup {

    public StateHeadlessSetup() {
    }

    @Override
    public void init(GameEngine gameEngine) {
        super.init(gameEngine);

        GameManifest gameManifest = null;
        List<GameInfo> savedGames = GameProvider.getSavedGames();
        if (savedGames.size() > 0) {
            gameManifest = savedGames.get(0).getManifest();
        } else {
            gameManifest = createGameManifest();
        }
        gameEngine.changeState(new StateLoading(gameManifest, NetworkMode.LISTEN_SERVER));
    }

    private GameManifest createGameManifest() {
        GameManifest gameManifest = new GameManifest();

        Config config = CoreRegistry.get(Config.class);
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        for (Name moduleName : config.getDefaultModSelection().listModules()) {
            Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleName);
            if (module != null) {
                gameManifest.addModule(module.getId(), module.getVersion());
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
                (long) (WorldTime.DAY_LENGTH * 0.025f), worldGeneratorUri);
        gameManifest.addWorld(worldInfo);
        return gameManifest;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void handleInput(float delta) {
    }

    @Override
    public void update(float delta) {
        super.update(delta);
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
