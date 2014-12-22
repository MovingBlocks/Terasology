/*
 * Copyright 2014 MovingBlocks
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
import org.terasology.config.Config;
import org.terasology.config.ModuleConfig;
import org.terasology.engine.GameEngine;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.module.ModuleManager;
import org.terasology.game.GameManifest;
import org.terasology.module.DependencyInfo;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.network.NetworkMode;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIDropdown;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.generator.internal.WorldGeneratorInfo;
import org.terasology.world.generator.internal.WorldGeneratorManager;
import org.terasology.world.internal.WorldInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @author Immortius
 */
public class CreateGameScreen extends CoreScreenLayer {

    private static final String DEFAULT_GAME_NAME_PREFIX = "Game ";
    private static final Logger logger = LoggerFactory.getLogger(CreateGameScreen.class);

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

        final UIDropdown<Module> gameplay = find("gameplay", UIDropdown.class);
        gameplay.setOptions(getGameplayModules());
        gameplay.bindSelection(new Binding<Module>() {
            Module selected;

            @Override
            public Module get() {
                // try and be smart about auto selecting a gameplay
                if (selected == null) {
                    // get the default gameplay module from the config.  This is likely to have  a user triggered selection.
                    Module defaultGameplayModule = moduleManager.getRegistry().getLatestModuleVersion(
                            new Name(config.getDefaultModSelection().getDefaultGameplayModuleName()));
                    if (defaultGameplayModule != null) {
                        set(defaultGameplayModule);
                        return selected;
                    }

                    // find the first gameplay module that is available
                    for (Name moduleName : config.getDefaultModSelection().listModules()) {
                        Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleName);

                        // module is null if it is no longer present
                        if (module != null && moduleManager.isGameplayModule(module)) {
                            set(module);
                            return selected;
                        }
                    }

                }

                return selected;
            }

            @Override
            public void set(Module value) {
                setSelectedGameplayModule(selected, value);
                selected = value;
            }
        });
        gameplay.setOptionRenderer(new StringTextRenderer<Module>() {
            @Override
            public String getString(Module value) {
                return value.getMetadata().getDisplayName().value();
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

        final UIDropdown<WorldGeneratorInfo> worldGenerator = find("worldGenerator", UIDropdown.class);
        if (worldGenerator != null) {
            worldGenerator.bindOptions(new ReadOnlyBinding<List<WorldGeneratorInfo>>() {
                @Override
                public List<WorldGeneratorInfo> get() {
                    // grab all the module names and their dependencies
                    Set<Name> enabledModuleNames = Sets.newHashSet();
                    for (Name moduleName : getAllEnabledModuleNames()) {
                        enabledModuleNames.add(moduleName);
                    }

                    List<WorldGeneratorInfo> result = Lists.newArrayList();
                    for (WorldGeneratorInfo option : worldGeneratorManager.getWorldGenerators()) {
                        if (enabledModuleNames.contains(option.getUri().getModuleName())) {
                            result.add(option);
                        }
                    }

                    return result;
                }
            });
            worldGenerator.bindSelection(new Binding<WorldGeneratorInfo>() {
                @Override
                public WorldGeneratorInfo get() {
                    // get the default generator from the config.  This is likely to have  a user triggered selection.
                    WorldGeneratorInfo info = worldGeneratorManager.getWorldGeneratorInfo(config.getWorldGeneration().getDefaultGenerator());
                    if (info != null && getAllEnabledModuleNames().contains(info.getUri().getModuleName())) {
                        return info;
                    }

                    // get the default generator from the selected gameplay module
                    Module selectedGameplayModule = gameplay.getSelection();
                    if (selectedGameplayModule != null) {
                        String defaultWorldGenerator = selectedGameplayModule.getMetadata().getExtension(ModuleManager.DEFAULT_WORLD_GENERATOR_EXT, String.class);
                        if (defaultWorldGenerator != null) {
                            for (WorldGeneratorInfo worldGenInfo : worldGeneratorManager.getWorldGenerators()) {
                                if (worldGenInfo.getUri().equals(new SimpleUri(defaultWorldGenerator))) {
                                    set(worldGenInfo);
                                    return worldGenInfo;
                                }
                            }
                        }
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
        }


        WidgetUtil.trySubscribe(this, "close", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().popScreen();
            }
        });

        WidgetUtil.trySubscribe(this, "play", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                if (worldGenerator.getSelection() == null) {
                    MessagePopup errorMessagePopup = getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
                    if (errorMessagePopup != null) {
                        errorMessagePopup.setMessage("No World Generator Selected", "Select a world generator (you may need to activate a mod with a generator first).");
                    }
                } else {
                    GameManifest gameManifest = new GameManifest();

                    gameManifest.setTitle(worldName.getText());
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

                    WorldInfo worldInfo = new WorldInfo(TerasologyConstants.MAIN_WORLD, gameManifest.getSeed(),
                            worldGenerator.getSelection().getUri());
                    gameManifest.addWorld(worldInfo);

                    gameEngine.changeState(new StateLoading(gameManifest, (loadingAsServer) ? NetworkMode.DEDICATED_SERVER : NetworkMode.NONE));
                }
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
        WidgetUtil.trySubscribe(this, "previewSeed", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                PreviewWorldScreen screen = getManager().pushScreen("engine:previewWorldScreen", PreviewWorldScreen.class);
                if (screen != null) {
                    screen.bindSeed(BindHelper.bindBeanProperty("text", seed, String.class));
                }
            }
        });

        UIButton configButton = find("config", UIButton.class);
        if (configButton != null) {
            configButton.subscribe(new ActivateEventListener() {
                @Override
                public void onActivated(UIWidget button) {
                    getManager().pushScreen("engine:configWorldGen");
                }
            });
            configButton.bindEnabled(worldGeneratorSelected);
        }

        WidgetUtil.trySubscribe(this, "mods", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().pushScreen("engine:selectModsScreen");
            }
        });
    }

    private Set<Name> getAllEnabledModuleNames() {
        Set<Name> enabledModules = Sets.newHashSet();
        for (Name moduleName : config.getDefaultModSelection().listModules()) {
            Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleName);
            if (module != null) {
                enabledModules.add(moduleName);
                if (module != null) {
                    for (DependencyInfo dependencyInfo : module.getMetadata().getDependencies()) {
                        enabledModules.add(dependencyInfo.getId());
                    }
                }
            }
        }

        return enabledModules;
    }

    private void setSelectedGameplayModule(Module previousModule, Module module) {
        ModuleConfig moduleConfig = config.getDefaultModSelection();
        moduleConfig.setDefaultGameplayModuleName(module.getId().toString());
        if (previousModule != null) {
            moduleConfig.removeModule(previousModule.getId());
        }
        moduleConfig.addModule(module.getId());

        if (!moduleConfig.hasModule(config.getWorldGeneration().getDefaultGenerator().getModuleName())) {
            config.getWorldGeneration().setDefaultGenerator(new SimpleUri());
        }
        config.save();
    }

    private List<Module> getGameplayModules() {
        List<Module> gameplayModules = Lists.newArrayList();
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            Module latestVersion = moduleManager.getRegistry().getLatestModuleVersion(moduleId);
            if (!latestVersion.isOnClasspath()) {
                if (moduleManager.isGameplayModule(latestVersion)) {
                    gameplayModules.add(latestVersion);
                }
            }
        }
        Collections.sort(gameplayModules, new Comparator<Module>() {
            @Override
            public int compare(Module o1, Module o2) {
                return o1.getMetadata().getDisplayName().value().compareTo(o2.getMetadata().getDisplayName().value());
            }
        });

        return gameplayModules;
    }

    public boolean isLoadingAsServer() {
        return loadingAsServer;
    }

    public void setLoadingAsServer(boolean loadingAsServer) {
        this.loadingAsServer = loadingAsServer;
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
