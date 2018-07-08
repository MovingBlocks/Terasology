/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.ModuleConfig;
import org.terasology.engine.GameEngine;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.StandardModuleExtension;
import org.terasology.game.GameManifest;
import org.terasology.i18n.TranslationSystem;
import org.terasology.input.Keyboard;
import org.terasology.module.DependencyInfo;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.network.NetworkMode;
import org.terasology.registry.In;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.rendering.nui.layers.mainMenu.selectModulesScreen.SelectModulesScreen;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIDropdown;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.generator.internal.WorldGeneratorInfo;
import org.terasology.world.generator.internal.WorldGeneratorManager;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.time.WorldTime;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CreateGameScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:createGameScreen");

    private static final Logger logger = LoggerFactory.getLogger(CreateGameScreen.class);

    private static final String DEFAULT_GAME_TEMPLATE_NAME = "JoshariasSurvival";

    @In
    private WorldGeneratorManager worldGeneratorManager;

    @In
    private ModuleManager moduleManager;

    @In
    private GameEngine gameEngine;

    @In
    private TranslationSystem translationSystem;

    @In
    private Config config;

    /** A UniverseWrapper object used here to determine if the game is single-player or multi-player.*/
    private UniverseWrapper universeWrapper;

    @Override
    @SuppressWarnings("unchecked")
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

        final UIText seed = find("seed", UIText.class);
        if (seed != null) {
            seed.setText(new FastRandom().nextString(32));
        }

        final UIDropdownScrollable<Module> gameplay = find("gameplay", UIDropdownScrollable.class);
        gameplay.setOptions(getGameplayModules());
        gameplay.setVisibleOptions(3);
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

        final UIDropdownScrollable<WorldGeneratorInfo> worldGenerator = find("worldGenerator", UIDropdownScrollable.class);
        if (worldGenerator != null) {
            worldGenerator.bindOptions(new ReadOnlyBinding<List<WorldGeneratorInfo>>() {
                @Override
                public List<WorldGeneratorInfo> get() {
                    // grab all the module names and their dependencies
                    // This grabs modules from `config.getDefaultModSelection()` which is updated in SelectModulesScreen
                    Set<Name> enabledModuleNames = getAllEnabledModuleNames().stream().collect(Collectors.toSet());
                    List<WorldGeneratorInfo> result = Lists.newArrayList();
                    for (WorldGeneratorInfo option : worldGeneratorManager.getWorldGenerators()) {
                        if (enabledModuleNames.contains(option.getUri().getModuleName())) {
                            result.add(option);
                        }
                    }

                    return result;
                }
            });
            worldGenerator.setVisibleOptions(3);
            worldGenerator.bindSelection(new Binding<WorldGeneratorInfo>() {
                @Override
                public WorldGeneratorInfo get() {
                    // get the default generator from the config. This is likely to have a user triggered selection.
                    WorldGeneratorInfo info = worldGeneratorManager.getWorldGeneratorInfo(config.getWorldGeneration().getDefaultGenerator());
                    if (info != null && getAllEnabledModuleNames().contains(info.getUri().getModuleName())) {
                        return info;
                    }

                    // just use the first available generator
                    for (WorldGeneratorInfo worldGenInfo : worldGeneratorManager.getWorldGenerators()) {
                        if (getAllEnabledModuleNames().contains(worldGenInfo.getUri().getModuleName())) {
                            set(worldGenInfo);
                            return worldGenInfo;
                        }
                    }

                    return null;
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
                    if (value != null) {
                        return value.getDisplayName();
                    }
                    return "";
                }
            });

            final UIButton playButton = find("play", UIButton.class);
            playButton.bindEnabled(new Binding<Boolean>() {
                @Override
                public Boolean get() {
                    return validateModuleDependencies(gameplay.getSelection().getId());
                }

                @Override
                public void set(Boolean value) {
                    playButton.setEnabled(value);
                }
            });
        }

        WidgetUtil.trySubscribe(this, "close", button -> {
            triggerBackAnimation();
            // get back to main screen if no saved games
            if (!isSavedGamesExist()) {
                triggerBackAnimation();
            }
        });

        WidgetUtil.trySubscribe(this, "play", button -> {
            if (worldGenerator.getSelection() == null) {
                MessagePopup errorMessagePopup = getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
                if (errorMessagePopup != null) {
                    errorMessagePopup.setMessage("No World Generator Selected", "Select a world generator (you may need to activate a mod with a generator first).");
                }
            } else {
                GameManifest gameManifest = new GameManifest();

                gameManifest.setTitle(gameName.getText());
                gameManifest.setSeed(seed.getText());
                DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
                ResolutionResult result = resolver.resolve(config.getDefaultModSelection().listModules());
                if (!result.isSuccess()) {
                    MessagePopup errorMessagePopup = getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
                    if (errorMessagePopup != null) {
                        errorMessagePopup.setMessage("Invalid Module Selection", "Please review your module seleciton and try again");
                    }
                    return;
                }
                for (Module module : result.getModules()) {
                    gameManifest.addModule(module.getId(), module.getVersion());
                }

                float timeOffset = 0.25f + 0.025f; // Time at dawn + little offset to spawn in a brighter env.
                WorldInfo worldInfo = new WorldInfo(TerasologyConstants.MAIN_WORLD, gameManifest.getSeed(),
                        (long) (WorldTime.DAY_LENGTH * timeOffset), worldGenerator.getSelection().getUri());
                gameManifest.addWorld(worldInfo);

                gameEngine.changeState(new StateLoading(gameManifest, (isLoadingAsServer()) ? NetworkMode.DEDICATED_SERVER : NetworkMode.NONE));
            }
        });

        UIButton previewSeed = find("previewSeed", UIButton.class);
        ReadOnlyBinding<Boolean> worldGeneratorSelected = new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return worldGenerator != null && worldGenerator.getSelection() != null;
            }
        };
        previewSeed.bindEnabled(worldGeneratorSelected);
        PreviewWorldScreen screen = getManager().createScreen(PreviewWorldScreen.ASSET_URI, PreviewWorldScreen.class);
        WidgetUtil.trySubscribe(this, "previewSeed", button -> {
            if (screen != null) {
                screen.bindSeed(BindHelper.bindBeanProperty("text", seed, String.class));
                try {
                    screen.setEnvironment();
                    triggerForwardAnimation(screen);
                } catch (Exception e) {
                    String msg = "Unable to load world for a 2D preview:\n" + e.toString();
                    getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error", msg);
                    logger.error("Unable to load world for a 2D preview", e);
                }
            }
        });

        WidgetUtil.trySubscribe(this, "mods", w -> triggerForwardAnimation(SelectModulesScreen.ASSET_URI));
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

            if (configDefaultModuleName.equalsIgnoreCase(DEFAULT_GAME_TEMPLATE_NAME)) {
                setDefaultGeneratorOfGameplayModule(defaultGameplayModule);
            }
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

    private void setGameName(UIText gameName) {
        if (gameName != null) {
            gameName.setText(GameProvider.getNextGameName());
        }
    }

    private Set<Name> getAllEnabledModuleNames() {
        Set<Name> enabledModules = Sets.newHashSet();
        for (Name moduleName : config.getDefaultModSelection().listModules()) {
            enabledModules.add(moduleName);
            recursivelyAddModuleDependencies(enabledModules, moduleName);
        }

        return enabledModules;
    }

    private void recursivelyAddModuleDependencies(Set<Name> modules, Name moduleName) {
        Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleName);
        if (module != null) {
            for (DependencyInfo dependencyInfo : module.getMetadata().getDependencies()) {
                modules.add(dependencyInfo.getId());
                recursivelyAddModuleDependencies(modules, dependencyInfo.getId());
            }
        }
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
        ModuleConfig moduleConfig = config.getDefaultModSelection();

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
        Collections.sort(gameplayModules, (o1, o2) ->
                o1.getMetadata().getDisplayName().value().compareTo(o2.getMetadata().getDisplayName().value()));

        return gameplayModules;
    }

    public boolean isLoadingAsServer() {
        return universeWrapper.getLoadingAsServer();
    }

    public void setUniverseWrapper(UniverseWrapper wrapper) {
        this.universeWrapper = wrapper;
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    private boolean isSavedGamesExist() {
        return !GameProvider.getSavedGames().isEmpty();
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown() && event.getKey() == Keyboard.Key.ESCAPE && isEscapeToCloseAllowed()) {
            triggerBackAnimation();
            if (!isSavedGamesExist()) {
                // get back to main screen
                triggerBackAnimation();
            }
            return true;
        }
        return false;
    }
}
