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

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.ModuleConfig;
import org.terasology.engine.GameEngine;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.StandardModuleExtension;
import org.terasology.game.GameManifest;
import org.terasology.i18n.TranslationSystem;
import org.terasology.input.Keyboard;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.naming.Name;
import org.terasology.network.NetworkMode;
import org.terasology.registry.In;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.layers.mainMenu.advancedGameSetupScreen.AdvancedGameSetupScreen;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.rendering.nui.widgets.UIDropdown;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.world.generator.internal.WorldGeneratorInfo;
import org.terasology.world.generator.internal.WorldGeneratorManager;

import java.util.Comparator;
import java.util.List;

public class NewGameScreen extends CoreScreenLayer {

    private static final Logger logger = LoggerFactory.getLogger(NewGameScreen.class);

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:newGameScreen");
    private static final String DEFAULT_GAME_TEMPLATE_NAME = "JoshariasSurvival";

    @In
    private ModuleManager moduleManager;

    @In
    private Config config;

    @In
    private WorldGeneratorManager worldGeneratorManager;

    @In
    private GameEngine gameEngine;

    @In
    private TranslationSystem translationSystem;

    private UniverseWrapper universeWrapper;

    @Override
    public void initialise() {

        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        UILabel gameTypeTitle = find("gameTypeTitle", UILabel.class);
        if (gameTypeTitle != null) {
            gameTypeTitle.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    if (isLoadingAsServer()) {
                        return translationSystem.translate("${engine:menu#select-multiplayer-game-sub-title}");
                    } else {
                        return translationSystem.translate("${engine:menu#select-singleplayer-game-sub-title}");
                    }
                }
            });
        }
        final UIText gameName = find("gameName", UIText.class);
        setGameName(gameName);

        final UIDropdownScrollable<Module> gameplay = find("gameplay", UIDropdownScrollable.class);
        gameplay.setOptions(getGameplayModules());
        gameplay.setVisibleOptions(5);
        gameplay.bindSelection(new Binding<Module>() {
            Module selected;

            @Override
            public Module get() {
                return selected;
            }

            @Override
            public void set(Module value) {
                setSelectedGameplayModule(value);
                selected = value;
            }
        });
        gameplay.setOptionRenderer(new StringTextRenderer<Module>() {
            @Override
            public String getString(Module value) {
                return value.getMetadata().getDisplayName().value();
            }

            @Override
            public void draw(Module value, Canvas canvas) {
                canvas.getCurrentStyle().setTextColor(validateModuleDependencies(value.getId()) ? Color.WHITE : Color.RED);
                super.draw(value, canvas);
                canvas.getCurrentStyle().setTextColor(Color.WHITE);
            }
        });

        UILabel gameplayDescription = find("gameplayDescription", UILabel.class);
        gameplayDescription.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                Module selectedModule = gameplay.getSelection();
                if (selectedModule != null) {
                    return selectedModule.getMetadata().getDescription().value();
                } else {
                    return "";
                }
            }
        });

        AdvancedGameSetupScreen advancedSetupGameScreen = getManager().createScreen(AdvancedGameSetupScreen.ASSET_URI, AdvancedGameSetupScreen.class);
        WidgetUtil.trySubscribe(this, "advancedSetup", button -> {
            universeWrapper.setGameName(gameName.getText());
            advancedSetupGameScreen.setUniverseWrapper(universeWrapper);
            triggerForwardAnimation(advancedSetupGameScreen);
        });

        WidgetUtil.trySubscribe(this, "play", button -> {
            if (gameName.getText().isEmpty()) {
                universeWrapper.setGameName(GameProvider.getNextGameName());
            }
            universeWrapper.setGameName(gameName.getText());
            GameManifest gameManifest = GameManifestProvider.createGameManifest(universeWrapper, moduleManager, config);
            if (gameManifest != null) {
                gameEngine.changeState(new StateLoading(gameManifest, (isLoadingAsServer()) ? NetworkMode.DEDICATED_SERVER : NetworkMode.NONE));
            } else {
                MessagePopup errorPopup = getManager().createScreen(MessagePopup.ASSET_URI, MessagePopup.class);
                errorPopup.setMessage("Error", "Can't create new game!");
            }
        });

        WidgetUtil.trySubscribe(this, "close", button -> {
            if (GameProvider.isSavesFolderEmpty()) {
                // skip selectGameScreen and get back directly to main screen
                getManager().pushScreen("engine:mainMenuScreen");
            } else {
                triggerBackAnimation();
            }
        });

        WidgetUtil.trySubscribe(this, "mainMenu", button -> {
            getManager().pushScreen("engine:mainMenuScreen");
        });
    }

    /**
     * Sets the game names based on the game number of the last saved game
     * @param gameName The {@link UIText} in which the name will be displayed.
     */
    private void setGameName(UIText gameName) {
        if (gameName != null) {
            gameName.setText(GameProvider.getNextGameName());
        }
    }

    private List<Module> getGameplayModules() {
        List<Module> gameplayModules = Lists.newArrayList();
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            Module latestVersion = moduleManager.getRegistry().getLatestModuleVersion(moduleId);
            if (!latestVersion.isOnClasspath()) {
                if (StandardModuleExtension.isGameplayModule(latestVersion)) {
                    gameplayModules.add(latestVersion);
                }
            }
        }
        gameplayModules.sort(Comparator.comparing(o -> o.getMetadata().getDisplayName().value()));

        return gameplayModules;
    }

    private boolean validateModuleDependencies(Name moduleName) {
        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        return resolver.resolve(moduleName).isSuccess();
    }

    private void setSelectedGameplayModule(Module module) {
        ModuleConfig moduleConfig = config.getDefaultModSelection();
        if (moduleConfig.getDefaultGameplayModuleName().equals(module.getId().toString())) {
            // same as before -> we're done
            return;
        }

        moduleConfig.setDefaultGameplayModuleName(module.getId().toString());
        moduleConfig.clear();
        moduleConfig.addModule(module.getId());

        // Set the default generator of the selected gameplay module
        setDefaultGeneratorOfGameplayModule(module);

        config.save();
    }

    // Sets the default generator of the passed in gameplay module. Make sure it's already selected.
    private void setDefaultGeneratorOfGameplayModule(Module module) {
        // Set the default generator of the selected gameplay module
        SimpleUri defaultWorldGenerator = StandardModuleExtension.getDefaultWorldGenerator(module);
        if (defaultWorldGenerator != null) {
            for (WorldGeneratorInfo worldGenInfo : worldGeneratorManager.getWorldGenerators()) {
                if (worldGenInfo.getUri().equals(defaultWorldGenerator)) {
                    config.getWorldGeneration().setDefaultGenerator(worldGenInfo.getUri());
                }
            }
        }

        config.save();
    }


    @Override
    public void onOpened() {
        super.onOpened();

        final UIText gameName = find("gameName", UIText.class);
        setGameName(gameName);

        final UIDropdown<Module> gameplay = find("gameplay", UIDropdown.class);

        String configDefaultModuleName = config.getDefaultModSelection().getDefaultGameplayModuleName();
        String useThisModuleName = "";

        // Get the default gameplay module from the config if it exists. This is likely to have a user triggered selection.
        // Otherwise, default to DEFAULT_GAME_TEMPLATE_NAME.
        if ("".equalsIgnoreCase(configDefaultModuleName) || DEFAULT_GAME_TEMPLATE_NAME.equalsIgnoreCase(configDefaultModuleName)) {
            useThisModuleName = DEFAULT_GAME_TEMPLATE_NAME;
        } else {
            useThisModuleName = configDefaultModuleName;
        }

        Name defaultGameplayModuleName = new Name(useThisModuleName);
        Module defaultGameplayModule = moduleManager.getRegistry().getLatestModuleVersion(defaultGameplayModuleName);

        if (defaultGameplayModule != null) {
            gameplay.setSelection(defaultGameplayModule);

            setDefaultGeneratorOfGameplayModule(defaultGameplayModule);
        } else {
            // Find the first gameplay module that is available.
            for (Module module : moduleManager.getRegistry()) {
                // Module is null if it is no longer present.
                if (module != null && StandardModuleExtension.isGameplayModule(module)) {
                    gameplay.setSelection(module);
                    break;
                }
            }
        }
    }

    public boolean isLoadingAsServer() {
        return universeWrapper.getLoadingAsServer();
    }

    public void setUniverseWrapper(UniverseWrapper wrapper) {
        this.universeWrapper = wrapper;
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown() && event.getKey() == Keyboard.Key.ESCAPE) {
            if (GameProvider.isSavesFolderEmpty()) {
                // skip selectGameScreen and get back directly to main screen
                getManager().pushScreen("engine:mainMenuScreen");
                return true;
            }
        }
        return super.onKeyEvent(event);
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}

