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
package org.terasology.rendering.nui.layers.mainMenu.gameDetailsScreen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.context.Context;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.module.ModuleManager;
import org.terasology.i18n.TranslationSystem;
import org.terasology.math.geom.Vector2i;
import org.terasology.module.DependencyInfo;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.naming.NameVersion;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.AbstractItemRenderer;
import org.terasology.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.rendering.nui.layers.mainMenu.SelectGameScreen;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layouts.ScrollableArea;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.rendering.nui.widgets.UITabBox;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.rendering.nui.widgets.slideshow.UITimedImageSlideshow;
import org.terasology.utilities.time.DateTimeHelper;
import org.terasology.world.biomes.Biome;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.internal.WorldInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Shows detailed information about saved game.
 */
public class GameDetailsScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:gameDetailsScreen");

    private static final Logger logger = LoggerFactory.getLogger(SelectGameScreen.class);

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @In
    private ModuleManager moduleManager;
    @In
    private TranslationSystem translationSystem;
    @In
    private Context context;

    private GameInfo gameInfo;
    private UIList<ModuleSelectionInfo> gameModules;
    private UIList<WorldInfo> gameWorlds;
    private UIList<Biome> biomes;
    private UIList<String> blocks;

    private UIText description;
    private UILabel descriptionTitle;
    private ScrollableArea worldDescription;
    private ScrollableArea descriptionContainer;
    private Map<String, List<String>> blockFamilyIds;
    private List<String> errors;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        gameModules = find("modules", UIList.class);
        gameWorlds = find("worlds", UIList.class);
        biomes = find("biomes", UIList.class);
        blocks = find("blocks", UIList.class);

        description = find("description", UIText.class);
        descriptionTitle = find("descriptionTitle", UILabel.class);
        worldDescription = find("worldDescription", ScrollableArea.class);
        descriptionContainer = find("descriptionContainer", ScrollableArea.class);

        if (descriptionContainer != null && worldDescription != null && descriptionTitle != null &&
                gameModules != null && gameWorlds != null && biomes != null && blocks != null) {
            setUpGameModules();
            setUpGameWorlds();
            setUpBlocks();
            setUpBiomes();
        }

        WidgetUtil.trySubscribe(this, "showErrors", button -> showErrors());
        WidgetUtil.trySubscribe(this, "close", button -> triggerBackAnimation());
    }

    @Override
    public void onOpened() {
        errors = new ArrayList<>();

        loadGeneralInfo();
        loadGameModules();
        loadBiomes();
        loadBlocks();
        loadGameWorlds();

        if (!errors.isEmpty()) {
            showErrors();
        }

        tryFind("tabs", UITabBox.class).ifPresent(tabs -> tabs.select(0));
        tryFind("showErrors", UIButton.class).ifPresent(button -> button.setEnabled(!errors.isEmpty()));

        super.onOpened();
    }

    private void showErrors() {
        StringBuilder errorMessageBuilder = new StringBuilder();
        errors.forEach(error -> errorMessageBuilder
                .append(errors.indexOf(error) + 1)
                .append(". ")
                .append(error)
                .append('\n'));
        getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage(translationSystem.translate("${engine:menu#game-details-errors-message-title}"), errorMessageBuilder.toString());
    }

    private void setUpBiomes() {
        biomes.subscribeSelection(((widget, biome) -> {
            if (biome == null) {
                return;
            }

            description.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(translationSystem.translate("${engine:menu#biome-name}: "));
                    sb.append(biome.getId());
                    sb.append('\n');
                    sb.append(translationSystem.translate("${engine:menu#biome-fog}: "));
                    sb.append(biome.getFog());
                    sb.append('\n');
                    sb.append(translationSystem.translate("${engine:menu#biome-humidity}: "));
                    sb.append(biome.getHumidity());
                    sb.append('\n');
                    sb.append(translationSystem.translate("${engine:menu#biome-temperature}: "));
                    sb.append(biome.getTemperature());
                    return sb.toString();
                }
            });

            descriptionTitle.setText(translationSystem.translate("${engine:menu#game-details-biomes}"));

            gameModules.setSelection(null);
            gameWorlds.setSelection(null);
            blocks.setSelection(null);

            worldDescription.setVisible(false);
            descriptionContainer.setVisible(true);
        }));

        biomes.setItemRenderer(new AbstractItemRenderer<Biome>() {
            String getString(Biome biome) {
                return biome.getId();
            }

            @Override
            public void draw(Biome value, Canvas canvas) {
                if (value.getId().contains("Core:")) {
                    canvas.setMode("internal");
                } else {
                    canvas.setMode("external");
                }
                canvas.drawText(getString(value), canvas.getRegion());
            }

            @Override
            public Vector2i getPreferredSize(Biome biome, Canvas canvas) {
                String text = getString(biome);
                return new Vector2i(canvas.getCurrentStyle().getFont().getWidth(text),
                        canvas.getCurrentStyle().getFont().getLineHeight());
            }
        });
    }

    private void setUpBlocks() {
        blocks.subscribeSelection(((widget, familyName) -> {
            if (familyName == null) {
                return;
            }

            description.bindText(
                    new ReadOnlyBinding<String>() {
                        @Override
                        public String get() {
                            List<String> blockFamilyNames = blockFamilyIds.get(familyName);
                            if (blockFamilyNames != null) {
                                return blockFamilyNames.stream().sorted().collect(Collectors.joining("\n"));
                            }
                            return "";
                        }
                    }
            );

            descriptionTitle.setText(translationSystem.translate("${engine:menu#game-details-blocks}"));

            gameModules.setSelection(null);
            gameWorlds.setSelection(null);
            biomes.setSelection(null);

            worldDescription.setVisible(false);
            descriptionContainer.setVisible(true);
        }));
    }

    private void setUpGameWorlds() {
        gameWorlds.subscribeSelection((widget, worldInfo) -> {
            if (worldInfo == null) {
                return;
            }

            gameModules.setSelection(null);
            biomes.setSelection(null);
            blocks.setSelection(null);

            descriptionTitle.setText(translationSystem.translate("${engine:menu#game-details-world-description}"));

            tryFind("worldTitle", UILabel.class).ifPresent(w -> w.setText(worldInfo.getTitle()));
            tryFind("worldSeed", UILabel.class).ifPresent(w -> w.setText(worldInfo.getSeed()));
            tryFind("worldTime", UILabel.class)
                    .ifPresent(w -> w.setText(DateTimeHelper.getDeltaBetweenTimestamps(new Date(0).getTime(), worldInfo.getTime())));
            tryFind("worldGenerator", UILabel.class).ifPresent(w -> w.setText(worldInfo.getWorldGenerator().toString()));

            descriptionContainer.setVisible(false);
            worldDescription.setVisible(true);
        });
    }

    private void setUpGameModules() {

        final Binding<ModuleSelectionInfo> moduleInfoBinding = new ReadOnlyBinding<ModuleSelectionInfo>() {
            @Override
            public ModuleSelectionInfo get() {
                if (gameModules.getSelection() != null) {
                    return gameModules.getSelection();
                }
                return null;
            }
        };

        gameModules.subscribeSelection((widget, moduleSelectionInfo) -> {
            if (moduleSelectionInfo == null || moduleSelectionInfo.getMetadata() == null) {
                return;
            }
            if (description != null) {
                description.bindText(new ReadOnlyBinding<String>() {
                    @Override
                    public String get() {
                        final StringBuilder sb = new StringBuilder();
                        final ModuleSelectionInfo moduleSelectionInfo = moduleInfoBinding.get();

                        if (moduleSelectionInfo == null) {
                            return translationSystem.translate("${engine:menu#game-details-invalid-module-error}");
                        }

                        final ModuleMetadata moduleMetadata = moduleSelectionInfo.getMetadata();

                        if (moduleMetadata != null) {
                            if (moduleSelectionInfo.isLatestVersion()) {
                                sb.append(translationSystem.translate("${engine:menu#game-details-invalid-module-version-warning}"))
                                        .append('\n')
                                        .append('\n');
                            }
                            if (moduleMetadata.getVersion() != null) {
                                sb.append(translationSystem.translate("${engine:menu#game-details-version}"))
                                        .append(" ")
                                        .append(moduleMetadata.getVersion().toString())
                                        .append('\n')
                                        .append('\n');
                            }
                            String moduleDescription = moduleMetadata.getDescription().toString();
                            if (StringUtils.isBlank(moduleDescription)) {
                                moduleDescription = translationSystem.translate("${engine:menu#game-details-no-description}");
                            }
                            sb.append(translationSystem.translate("${engine:menu#game-details-description}"))
                                    .append(moduleDescription).append('\n').append('\n');

                            StringBuilder dependenciesNames;
                            List<DependencyInfo> dependencies = moduleMetadata.getDependencies();
                            if (dependencies != null && !dependencies.isEmpty()) {
                                dependenciesNames = new StringBuilder(translationSystem
                                        .translate("${engine:menu#module-dependencies-exist}") + ":" + '\n');
                                for (DependencyInfo dependency : dependencies) {
                                    dependenciesNames
                                            .append("   ")
                                            .append(dependency.getId().toString())
                                            .append('\n');
                                }
                            } else {
                                dependenciesNames = new StringBuilder(translationSystem
                                        .translate("${engine:menu#module-dependencies-empty}") + ".");
                            }
                            return sb.append(dependenciesNames).toString();
                        }

                        if (moduleSelectionInfo.isUnavailableVersion()) {
                            return sb.append(translationSystem.translate("${engine:menu#game-details-invalid-module-error}"))
                                    .append("\n")
                                    .append('\n')
                                    .append(translationSystem.translate("${engine:menu#game-details-version}"))
                                    .append(" ")
                                    .append(moduleSelectionInfo.getUnavailableModuleVersion())
                                    .toString();
                        }
                        return translationSystem.translate("${engine:menu#game-details-invalid-module-error}");
                    }
                });
            }
            gameWorlds.setSelection(null);
            biomes.setSelection(null);
            blocks.setSelection(null);
            descriptionTitle.setText(translationSystem.translate("${engine:menu#game-details-module-description}") + " | " + moduleSelectionInfo.getMetadata().getDisplayName());
            descriptionContainer.setVisible(true);
            worldDescription.setVisible(false);
        });

        gameModules.setItemRenderer(new AbstractItemRenderer<ModuleSelectionInfo>() {
            String getString(ModuleSelectionInfo value) {
                if (value.getMetadata() != null) {
                    return value.getMetadata().getDisplayName().toString();
                } else if (value.isUnavailableVersion()) {
                    return value.getUnavailableModuleName();
                }
                return "";
            }

            @Override
            public void draw(ModuleSelectionInfo value, Canvas canvas) {
                if (value.isStrictVersion()) {
                    canvas.setMode("strict");
                } else if (value.isLatestVersion()) {
                    canvas.setMode("latest");
                } else {
                    canvas.setMode("invalid");
                }
                canvas.drawText(getString(value), canvas.getRegion());
            }

            @Override
            public Vector2i getPreferredSize(ModuleSelectionInfo value, Canvas canvas) {
                String text = getString(value);
                return new Vector2i(canvas.getCurrentStyle().getFont().getWidth(text),
                        canvas.getCurrentStyle().getFont().getLineHeight());
            }
        });
    }

    private void loadGameModules() {
        final List<ModuleSelectionInfo> sortedGameModules = gameInfo.getManifest().getModules().stream()
                .sorted(Comparator.comparing(NameVersion::getName))
                .map(nameVersion -> {
                    Module module = moduleManager.getRegistry().getModule(nameVersion.getName(), nameVersion.getVersion());
                    if (module != null) {
                        return ModuleSelectionInfo.strictVersion(module);
                    } else {
                        logger.warn("Can't find module in your classpath - {}:{}", nameVersion.getName(), nameVersion.getVersion());
                        module = moduleManager.getRegistry().getLatestModuleVersion(nameVersion.getName());
                        if (module != null) {
                            logger.debug("Get the latest available version of module {} in your classpath", nameVersion.getName());
                            errors.add(String.format("Can't find module %s:%s in your classpath; loaded description for the latest available version.", nameVersion.getName(), nameVersion.getVersion()));
                            return ModuleSelectionInfo.latestVersion(module);
                        }
                        logger.error("Can't find any versions of module {} in your classpath!", nameVersion.getName());
                        errors.add(String.format("Can't find any versions of module %s in your classpath!", nameVersion.getName()));
                        return ModuleSelectionInfo.unavailableVersion(nameVersion.getName().toString(), nameVersion.getVersion().toString());
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        gameModules.setList(sortedGameModules);
        gameModules.select(0);
    }

    private void loadGeneralInfo() {
        final String title = gameInfo.getManifest().getTitle();
        tryFind("title", UILabel.class)
                .ifPresent(w -> w.setText(translationSystem.translate("${engine:menu#game-details-title}") + " : " + title));
        tryFind("gameTitle", UILabel.class).ifPresent(w -> w.setText(title));
        tryFind("seed", UILabel.class).ifPresent(w -> w.setText(gameInfo.getManifest().getSeed()));
        tryFind("duration", UILabel.class)
                .ifPresent(w -> w.setText(
                        DateTimeHelper.getDeltaBetweenTimestamps(new Date(0).getTime(), gameInfo.getManifest().getTime()))
                );
        tryFind("gameWorldGenerator", UILabel.class)
                .ifPresent(w -> w.setText(gameInfo.getManifest()
                        .getWorldInfo(TerasologyConstants.MAIN_WORLD)
                        .getWorldGenerator()
                        .getObjectName()
                        .toString()));
        tryFind("lastAccessDate", UILabel.class)
                .ifPresent(w -> w.setText(dateFormat.format(gameInfo.getTimestamp())));
    }

    private void loadBiomes() {
        List<Biome> biomesList = Collections.emptyList();
        final List<Name> moduleIds = gameInfo.getManifest().getModules().stream()
                .map(NameVersion::getName)
                .collect(Collectors.toCollection(ArrayList::new));

        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        ResolutionResult result = resolver.resolve(moduleIds);
        if (result.isSuccess()) {
            ModuleEnvironment env = moduleManager.loadEnvironment(result.getModules(), true);
            BiomeManager biomeManager = null;
            try {
                biomeManager = new BiomeManager(env, gameInfo.getManifest().getBiomeIdMap());
            } catch (Exception ex) {
                errors.add(translationSystem.translate("${engine:menu#game-details-biomes-error}") + " - " + ex.getMessage());
                logger.error("Couldn't load biomes: {}", ex.getMessage());
            }
            if (biomeManager != null) {
                biomesList = biomeManager.getBiomes().stream()
                        .sorted(Comparator.comparing(Biome::getName))
                        .collect(Collectors.toList());
            }
        } else {
            errors.add(translationSystem.translate("${engine:menu#game-details-biomes-error}"));
        }
        biomes.setList(biomesList);
    }

    private void loadGameWorlds() {
        gameWorlds.setList(Lists.newArrayList(gameInfo.getManifest().getWorlds().iterator()));
    }

    private void loadBlocks() {
        blockFamilyIds = Maps.newHashMap();

        gameInfo.getManifest().getBlockIdMap().entrySet().forEach(blockId -> {
            String familyName = blockId.getKey().split(":")[0].toLowerCase();
            blockFamilyIds.computeIfAbsent(familyName, k -> new ArrayList<>());
            blockFamilyIds.get(familyName).add(blockId.toString());
        });

        blocks.setList(Lists.newArrayList(blockFamilyIds.keySet()));
    }

    public void setGameInfo(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public void setPreviewImage(TextureRegion texture) {
        UITimedImageSlideshow slider = find("preview", UITimedImageSlideshow.class);
        if (slider != null) {
            slider.addImage(texture);
        }
    }
}
