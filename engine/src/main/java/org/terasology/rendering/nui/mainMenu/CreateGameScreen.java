/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.mainMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.systems.In;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkMode;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIScreen;
import org.terasology.rendering.nui.UIScreenUtil;
import org.terasology.rendering.nui.baseWidgets.ButtonEventListener;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.baseWidgets.UIDropdown;
import org.terasology.rendering.nui.baseWidgets.UIText;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.mainMenu.savedGames.GameProvider;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.generator.internal.WorldGeneratorInfo;
import org.terasology.world.generator.internal.WorldGeneratorManager;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.time.WorldTime;

/**
 * @author Immortius
 */
public class CreateGameScreen extends UIScreen {

    private static final String DEFAULT_GAME_NAME_PREFIX = "Game ";
    private static final Logger logger = LoggerFactory.getLogger(CreateGameScreen.class);

    @In
    private NUIManager nuiManager;

    @In
    private WorldGeneratorManager worldGeneratorManager;

    @In
    private ModuleManager moduleManager;

    @In
    private GameEngine gameEngine;

    @In
    private Config config;

    private boolean loadingAsServer;

    @Override
    @SuppressWarnings("unchecked")
    public void initialise() {
        final UIText worldName = find("worldName", UIText.class);
        if (worldName != null) {
            int gameNum = 1;
            for (GameInfo info : GameProvider.getSavedGames()) {
                if (info.getManifest().getTitle().startsWith(DEFAULT_GAME_NAME_PREFIX)) {
                    String remainder = info.getManifest().getTitle().substring(DEFAULT_GAME_NAME_PREFIX.length());
                    try {
                        gameNum = Math.max(gameNum, Integer.parseInt(remainder) + 1);
                    } catch (NumberFormatException e) {
                        logger.trace("Could not parse {} as integer (not an error)", remainder, e);
                    }
                }
            }

            worldName.setText(DEFAULT_GAME_NAME_PREFIX + gameNum);
        }

        final UIText seed = find("seed", UIText.class);
        if (seed != null) {
            seed.setText(new FastRandom().nextString(32));
        }

        final UIDropdown<WorldGeneratorInfo> worldGenerator = find("worldGenerator", UIDropdown.class);
        if (worldGenerator != null) {
            worldGenerator.setOptions(worldGeneratorManager.getWorldGenerators());
            worldGenerator.bindSelection(new Binding<WorldGeneratorInfo>() {
                @Override
                public WorldGeneratorInfo get() {
                    return worldGeneratorManager.getWorldGeneratorInfo(config.getWorldGeneration().getDefaultGenerator());
                }

                @Override
                public void set(WorldGeneratorInfo value) {
                    if (value != null) {
                        config.getWorldGeneration().setDefaultGenerator(value.getUri());
                    }
                }
            });
            worldGenerator.setOptionRenderer(new StringTextRenderer<WorldGeneratorInfo>() {
                @Override
                public String getString(WorldGeneratorInfo value) {
                    return value.getDisplayName();
                }
            });
        }


        UIScreenUtil.trySubscribe(this, "close", new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                nuiManager.popScreen();
            }
        });

        UIScreenUtil.trySubscribe(this, "play", new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                GameManifest gameManifest = new GameManifest();

                gameManifest.setTitle(worldName.getText());
                gameManifest.setSeed(seed.getText());
                for (String moduleName : config.getDefaultModSelection().listModules()) {
                    Module module = moduleManager.getLatestModuleVersion(moduleName);
                    if (module != null) {
                        gameManifest.addModule(module.getId(), module.getVersion());
                    }
                }

                WorldInfo worldInfo = new WorldInfo(TerasologyConstants.MAIN_WORLD, gameManifest.getSeed(),
                        (long) (WorldTime.DAY_LENGTH * 0.025f), worldGenerator.getSelection().getUri());
                gameManifest.addWorld(worldInfo);

                gameEngine.changeState(new StateLoading(gameManifest, (loadingAsServer) ? NetworkMode.SERVER : NetworkMode.NONE));
            }
        });

        UIScreenUtil.trySubscribe(this, "previewSeed", new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                PreviewWorldScreen screen = nuiManager.pushScreen("engine:previewWorldScreen", PreviewWorldScreen.class);
                if (screen != null) {
                    screen.bindSeed(BindHelper.bindBeanProperty("text", seed, String.class));
                }
            }
        });
        UIScreenUtil.trySubscribe(this, "mods", new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                nuiManager.pushScreen("engine:selectModsScreen");
            }
        });
    }

    public boolean isLoadingAsServer() {
        return loadingAsServer;
    }

    public void setLoadingAsServer(boolean loadingAsServer) {
        this.loadingAsServer = loadingAsServer;
    }
}
