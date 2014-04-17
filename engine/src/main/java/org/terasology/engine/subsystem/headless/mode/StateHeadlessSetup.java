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
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.modes.StateSetup;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkMode;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.time.WorldTime;

/**
 * The class implements the main game menu.
 * <p/>
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * @version 0.3
 */
public class StateHeadlessSetup extends StateSetup {

    public StateHeadlessSetup() {
    }

    @Override
    public void init(GameEngine gameEngine) {
        super.init(gameEngine);

        GameManifest gameManifest = new GameManifest();

        Config config = CoreRegistry.get(Config.class);
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        for (String moduleName : config.getDefaultModSelection().listModules()) {
            Module module = moduleManager.getLatestModuleVersion(moduleName);
            if (module != null) {
                gameManifest.addModule(module.getId(), module.getVersion());
            }
        }

        WorldGenerationConfig worldGenConfig = config.getWorldGeneration();
        SimpleUri worldGeneratorUri = worldGenConfig.getDefaultGenerator();

        gameManifest.setTitle(worldGenConfig.getWorldTitle());
        gameManifest.setSeed(worldGenConfig.getDefaultSeed());

        WorldInfo worldInfo = new WorldInfo(TerasologyConstants.MAIN_WORLD, gameManifest.getSeed(),
                (long) (WorldTime.DAY_LENGTH * 0.025f), worldGeneratorUri);
        gameManifest.addWorld(worldInfo);
        gameEngine.changeState(new StateLoading(gameManifest, NetworkMode.SERVER));
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
}
