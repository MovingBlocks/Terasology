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
import org.terasology.engine.module.StandardModuleExtension;
import org.terasology.game.GameManifest;
import org.terasology.i18n.TranslationSystem;
import org.terasology.module.DependencyInfo;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.network.NetworkMode;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
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

/**
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
    private TranslationSystem translationSystem;

    @In
    private Config config;

    private boolean loadingAsServer;

    @Override
    @SuppressWarnings("unchecked")
    public void initialise() {


        UILabel gameTypeTitle = find("gameTypeTitle", UILabel.class);
        if (gameTypeTitle != null) {
            gameTypeTitle.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    if (loadingAsServer) {
                        return translationSystem.translate("${engine:menu#select-multiplayer-game-sub-title}");
                    } else {
                        return translationSystem.translate("${engine:menu#select-singleplayer-game-sub-title}");
                    }
                }
            });
        }


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
                    // get the default generator from the config.  This is likely to have  a user triggered selection.
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
        }


        WidgetUtil.trySubscribe(this, "close", button -> getManager().popScreen());

        WidgetUtil.trySubscribe(this, "play", button -> {
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

                float timeOffset = 0.25f + 0.025f;  // Time at dawn + little offset to spawn in a brighter env.
                WorldInfo worldInfo = new WorldInfo(TerasologyConstants.MAIN_WORLD, gameManifest.getSeed(),
                        (long) (WorldTime.DAY_LENGTH * timeOffset), worldGenerator.getSelection().getUri());
                gameManifest.addWorld(worldInfo);

                gameEngine.changeState(new StateLoading(gameManifest, (loadingAsServer) ? NetworkMode.DEDICATED_SERVER : NetworkMode.NONE));
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
        WidgetUtil.trySubscribe(this, "previewSeed", button -> {
            PreviewWorldScreen screen = getManager().pushScreen(PreviewWorldScreen.ASSET_URI, PreviewWorldScreen.class);
            if (screen != null) {
                screen.bindSeed(BindHelper.bindBeanProperty("text", seed, String.class));
            }
        });

        WidgetUtil.trySubscribe(this, "mods", button -> getManager().pushScreen("engine:selectModsScreen"));
    }

    @Override
    public void onOpened() {
        super.onOpened();

        final UIDropdown<Module> gameplay = find("gameplay", UIDropdown.class);

        // get the default gameplay module from the config.  This is likely to have a user triggered selection.
        Name defaultGameplayModuleName = new Name(config.getDefaultModSelection().getDefaultGameplayModuleName());
        Module defaultGameplayModule = moduleManager.getRegistry().getLatestModuleVersion(defaultGameplayModuleName);
        if (defaultGameplayModule != null) {
            gameplay.setSelection(defaultGameplayModule);
        } else {
            // find the first gameplay module that is available
            for (Module module : moduleManager.getRegistry()) {
                // module is null if it is no longer present
                if (module != null && StandardModuleExtension.isGameplayModule(module)) {
                    gameplay.setSelection(module);
                }
            }
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
