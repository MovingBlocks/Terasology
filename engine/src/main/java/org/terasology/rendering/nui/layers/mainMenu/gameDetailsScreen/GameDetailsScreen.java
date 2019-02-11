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
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.AbstractItemRenderer;
import org.terasology.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.rendering.nui.layers.mainMenu.SelectGameScreen;
import org.terasology.rendering.nui.layers.mainMenu.moduleDetailsScreen.ModuleDetailsScreen;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UIImageSlideshow;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.rendering.nui.widgets.UITabBox;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.utilities.time.DateTimeHelper;
import org.terasology.world.biomes.Biome;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.internal.WorldInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Shows detailed information about saved game.
 */
public class GameDetailsScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:gameDetailsScreen");

    private static final Logger logger = LoggerFactory.getLogger(SelectGameScreen.class);

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private GameInfo gameInfo;
    private List<String> errors;
    private Map<String, List<String>> blockFamilyIds;

    @In
    private ModuleManager moduleManager;
    @In
    private TranslationSystem translationSystem;
    @In
    private Context context;

    private UIList<ModuleSelectionInfo> gameModules;
    private final Binding<ModuleSelectionInfo> moduleInfoBinding = new ReadOnlyBinding<ModuleSelectionInfo>() {
        @Override
        public ModuleSelectionInfo get() {
            if (gameModules.getSelection() != null) {
                return gameModules.getSelection();
            }
            return null;
        }
    };
    private UIList<WorldInfo> gameWorlds;
    private UIList<Biome> biomes;
    private UIList<String> blocks;
    private UIText description;
    private UIText generalInfo;
    private UILabel descriptionTitle;
    private UIButton openModuleDetails;
    private UIImageSlideshow previewSlideshow;
    private UITabBox tabs;
    private UIButton showErrors;
    private UIButton close;
    private UIButton slideLeft;
    private UIButton slideRight;
    private UIButton slideStop;
    private UILabel title;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        initWidgets();

        if (isScreenValid()) {

            setUpGameModules();
            setUpGameWorlds();
            setUpBlocks();
            setUpBiomes();
            setUpPreviewSlideshow();
            setUpOpenModuleDetails();

            showErrors.subscribe(e -> showErrors());
            close.subscribe(e -> triggerBackAnimation());
        }
    }

    @Override
    public void onOpened() {
        super.onOpened();

        if (isScreenValid()) {
            errors = new ArrayList<>();

            loadGeneralInfo();
            loadGameModules();
            loadBiomes();
            loadBlocks();
            loadGameWorlds();

            if (!errors.isEmpty()) {
                showErrors();
            }

            tabs.select(0);
            showErrors.setEnabled(!errors.isEmpty());
        } else {
            final MessagePopup popup = getManager().createScreen(MessagePopup.ASSET_URI, MessagePopup.class);
            popup.setMessage(translationSystem.translate("${engine:menu#game-details-errors-message-title}"), translationSystem.translate("${engine:menu#game-details-errors-message-body}"));
            popup.subscribeButton(e -> triggerBackAnimation());
            getManager().pushScreen(popup);
            // disable child widgets
            setEnabled(false);
        }
    }

    private void initWidgets() {
        gameModules = find("modules", UIList.class);
        gameWorlds = find("worlds", UIList.class);
        biomes = find("biomes", UIList.class);
        blocks = find("blocks", UIList.class);

        description = find("description", UIText.class);
        descriptionTitle = find("descriptionTitle", UILabel.class);
        openModuleDetails = find("openModuleDetails", UIButton.class);
        previewSlideshow = find("preview", UIImageSlideshow.class);
        tabs = find("tabs", UITabBox.class);
        showErrors = find("showErrors", UIButton.class);
        close = find("close", UIButton.class);

        slideLeft = find("slideLeft", UIButton.class);
        slideRight = find("slideRight", UIButton.class);
        slideStop = find("slideStop", UIButton.class);

        title = find("title", UILabel.class);
        generalInfo = find("generalInfo", UIText.class);
    }

    private void showErrors() {
        final StringBuilder errorMessageBuilder = new StringBuilder();
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

            descriptionTitle.setText(translationSystem.translate("${engine:menu#game-details-biomes}"));
            description.setText(getBiomeDescription(biome));

            gameModules.setSelection(null);
            gameWorlds.setSelection(null);
            blocks.setSelection(null);
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

    private String getBiomeDescription(final Biome biome) {
        return translationSystem.translate("${engine:menu#biome-name}: ") + biome.getId() + '\n' + '\n' +
                translationSystem.translate("${engine:menu#biome-fog}: ") + biome.getFog() + '\n' + '\n' +
                translationSystem.translate("${engine:menu#biome-humidity}: ") + biome.getHumidity() + '\n' + '\n' +
                translationSystem.translate("${engine:menu#biome-temperature}: ") + biome.getTemperature();
    }

    private void setUpBlocks() {
        blocks.subscribeSelection(((widget, familyName) -> {
            if (familyName == null) {
                return;
            }

            descriptionTitle.setText(translationSystem.translate("${engine:menu#game-details-blocks}"));
            description.setText(getBlockInfoDescription(familyName));

            gameModules.setSelection(null);
            gameWorlds.setSelection(null);
            biomes.setSelection(null);
        }));
    }

    private String getBlockInfoDescription(final String familyName) {
        String familyNames = "";
        final List<String> blockFamilyNames = blockFamilyIds.get(familyName);
        if (blockFamilyNames != null) {
            familyNames = blockFamilyNames.stream().sorted().collect(Collectors.joining("\n"));
        }
        return familyNames;
    }

    private void setUpGameWorlds() {
        gameWorlds.subscribeSelection((widget, worldInfo) -> {
            if (worldInfo == null) {
                return;
            }

            descriptionTitle.setText(translationSystem.translate("${engine:menu#game-details-world-description}"));
            description.setText(getWorldDescription(worldInfo));

            gameModules.setSelection(null);
            biomes.setSelection(null);
            blocks.setSelection(null);
        });

        gameWorlds.setItemRenderer(new AbstractItemRenderer<WorldInfo>() {
            @Override
            public void draw(WorldInfo value, Canvas canvas) {
                canvas.drawText(value.getCustomTitle());
            }

            @Override
            public Vector2i getPreferredSize(WorldInfo value, Canvas canvas) {
                String text = value.getCustomTitle();
                return new Vector2i(canvas.getCurrentStyle().getFont().getWidth(text), canvas.getCurrentStyle().getFont().getLineHeight());
            }
        });
    }

    private String getWorldDescription(final WorldInfo worldInfo) {
        return translationSystem.translate("${engine:menu#game-details-game-title} ") + worldInfo.getCustomTitle() + '\n' + '\n' +
                translationSystem.translate("${engine:menu#game-details-game-seed} ") + worldInfo.getSeed() + '\n' + '\n' +
                translationSystem.translate("${engine:menu#game-details-world-generator}: ") + worldInfo.getWorldGenerator().toString() + '\n' + '\n' +
                translationSystem.translate("${engine:menu#game-details-game-duration} ") + DateTimeHelper.getDeltaBetweenTimestamps(new Date(0).getTime(), worldInfo.getTime());
    }

    private void setUpPreviewSlideshow() {
        slideLeft.subscribe(b -> previewSlideshow.prevImage());
        slideLeft.bindEnabled(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return previewSlideshow.getImages().size() > 1;
            }
        });

        slideRight.subscribe(b -> previewSlideshow.nextImage());
        slideRight.bindEnabled(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return previewSlideshow.getImages().size() > 1;
            }
        });

        slideStop.subscribe(e -> {
            if (previewSlideshow.isActive()) {
                previewSlideshow.stop();
                slideStop.setActive(true);
            } else {
                previewSlideshow.start();
                slideStop.setActive(false);
            }
        });
        slideStop.bindEnabled(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return previewSlideshow.getImages().size() > 1;
            }
        });
    }

    private void setUpGameModules() {
        gameModules.subscribeSelection((widget, moduleSelectionInfo) -> {
            if (moduleSelectionInfo == null) {
                return;
            }

            descriptionTitle.setText(translationSystem.translate("${engine:menu#game-details-module-description}"));
            description.setText(getModuleDescription(moduleSelectionInfo));

            gameWorlds.setSelection(null);
            biomes.setSelection(null);
            blocks.setSelection(null);
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

        gameModules.subscribe(((widget, item) -> openModuleDetailsScreen()));
    }

    private void openModuleDetailsScreen() {
        final ModuleDetailsScreen moduleDetailsScreen = getManager().createScreen(ModuleDetailsScreen.ASSET_URI, ModuleDetailsScreen.class);

        final Collection<Module> modules = gameModules.getList().stream()
                        .map(ModuleSelectionInfo::getModule)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        moduleDetailsScreen.setModules(modules);

        moduleDetailsScreen.setSelectedModule(
                modules.stream()
                        .filter(module -> module.getId().equals(moduleInfoBinding.get().getModule().getId()))
                        .findFirst()
                        .orElse(null)
        );

        getManager().pushScreen(moduleDetailsScreen);
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
        generalInfo.setText(getGeneralInfo(gameInfo));
        title.setText(translationSystem.translate("${engine:menu#game-details-title}") + " : " + gameInfo.getManifest().getTitle());
    }

    private String getGeneralInfo(final GameInfo gameInfo) {
        return translationSystem.translate("${engine:menu#game-details-game-title} ") + gameInfo.getManifest().getTitle() + '\n' + '\n' +
                translationSystem.translate("${engine:menu#game-details-last-play}: ") + dateFormat.format(gameInfo.getTimestamp()) + '\n' + '\n' +
                translationSystem.translate("${engine:menu#game-details-game-duration} ") + DateTimeHelper
                .getDeltaBetweenTimestamps(new Date(0).getTime(), gameInfo.getManifest().getTime()) + '\n' + '\n' +
                translationSystem.translate("${engine:menu#game-details-game-seed} ") + gameInfo.getManifest().getSeed() + '\n' + '\n' +
                translationSystem.translate("${engine:menu#game-details-world-generator}: ") + '\t' + gameInfo.getManifest().getWorldInfo(TerasologyConstants.MAIN_WORLD).getWorldGenerator().getObjectName().toString();
    }

    private void loadBiomes() {
        List<Biome> biomesList = Collections.emptyList();
        final List<Name> moduleIds = gameInfo.getManifest().getModules().stream()
                .map(NameVersion::getName)
                .collect(Collectors.toCollection(ArrayList::new));

        final DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        final ResolutionResult result = resolver.resolve(moduleIds);
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

    public void setPreviews(final List<UIImage> images) {
        if (images != null && !images.isEmpty()) {
            previewSlideshow.clean();
            images.forEach(previewSlideshow::addImage);
        }
    }

    private String getModuleDescription(final ModuleSelectionInfo moduleSelectionInfo) {
        final StringBuilder sb = new StringBuilder();
        final ModuleMetadata moduleMetadata = moduleSelectionInfo.getMetadata();

        if (moduleMetadata != null) {
            sb.append(translationSystem.translate("${engine:menu#game-details-game-title} "))
                    .append(moduleMetadata.getDisplayName())
                    .append('\n')
                    .append('\n');

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
            final List<DependencyInfo> dependencies = moduleMetadata.getDependencies();
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

    private void setUpOpenModuleDetails() {
        openModuleDetails.subscribe(button -> openModuleDetailsScreen());
        openModuleDetails.bindEnabled(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return moduleInfoBinding.get() != null && moduleInfoBinding.get().getMetadata() != null;
            }
        });
    }

    private boolean isScreenValid() {
        if (Stream.of(gameModules, gameWorlds, biomes, blocks, description, descriptionTitle, openModuleDetails,
                previewSlideshow, tabs, showErrors, close, slideLeft, slideRight, slideStop, title)
                .anyMatch(Objects::isNull)) {
            logger.error("Can't initialize screen correctly. At least one widget was missed!");
            return false;
        }
        return true;
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
