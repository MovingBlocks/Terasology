/*
 * Copyright 2017 MovingBlocks
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
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.StandardModuleExtension;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.rendering.nui.widgets.UIDropdown;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.world.generator.internal.WorldGeneratorInfo;
import org.terasology.world.generator.internal.WorldGeneratorManager;

import java.util.Collections;
import java.util.List;

public class NewGameScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:newGameScreen");
    private static final String DEFAULT_GAME_NAME_PREFIX = "Game ";
    private static final Logger logger = LoggerFactory.getLogger(CreateGameScreen.class);
    private static final String DEFAULT_GAME_TEMPLATE_NAME = "JoshariasSurvival";
    private static final String DEFAULT_WORLD_GENERATOR = "Core:FacetedPerlin";


    @In
    private ModuleManager moduleManager;

    @In
    private Config config;

    @In
    private WorldGeneratorManager worldGeneratorManager;

    @In
    private GameEngine gameEngine;

    @Override
    public void initialise(){

        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());
        final UIText gameName = find("gameName",UIText.class);
        setGameName(gameName);

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

        AdvancedGameSetupScreen advancedSetupGameScreen = getManager().createScreen(AdvancedGameSetupScreen.ASSET_URI, AdvancedGameSetupScreen.class);
        WidgetUtil.trySubscribe(this, "advancedSetup", button ->
                triggerForwardAnimation(advancedSetupGameScreen)
        );

        WidgetUtil.trySubscribe(this, "close", button ->
                triggerBackAnimation()
        );
    }

    private void setGameName(UIText gameName) {
        if (gameName != null) {
            int gameNumber = 1;
            for (GameInfo info : GameProvider.getSavedGames()) {
                if (info.getManifest().getTitle().startsWith(DEFAULT_GAME_NAME_PREFIX)) {
                    String remainder = info.getManifest().getTitle().substring(DEFAULT_GAME_NAME_PREFIX.length());
                    try {
                        gameNumber = Math.max(gameNumber, Integer.parseInt(remainder) + 1);
                    } catch (NumberFormatException e) {
                        logger.trace("Could not parse {} as integer (not an error)", remainder, e);
                    }
                }
            }

            gameName.setText(DEFAULT_GAME_NAME_PREFIX + gameNumber);
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
        Collections.sort(gameplayModules, (o1, o2) ->
                o1.getMetadata().getDisplayName().value().compareTo(o2.getMetadata().getDisplayName().value()));

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



    @Override
    public void onOpened() {
        final UIText gameName = find("gameName",UIText.class);
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
}

