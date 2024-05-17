// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.gameDetailsScreen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.codehaus.plexus.util.StringUtils;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.engine.rendering.nui.layers.mainMenu.SelectGameScreen;
import org.terasology.engine.rendering.nui.layers.mainMenu.moduleDetailsScreen.ModuleDetailsScreen;
import org.terasology.engine.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.engine.utilities.time.DateTimeHelper;
import org.terasology.engine.world.generator.internal.WorldGeneratorManager;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleMetadata;
import org.terasology.gestalt.module.dependencyresolution.DependencyInfo;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.NameVersion;
import org.terasology.gestalt.naming.Version;
import org.terasology.nui.Canvas;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.itemRendering.AbstractItemRenderer;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIImage;
import org.terasology.nui.widgets.UIImageSlideshow;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UIList;
import org.terasology.nui.widgets.UITabBox;
import org.terasology.nui.widgets.UIText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private GameInfo gameInfo;
    private List<String> errors;
    private Map<String, List<String>> blockFamilyIds;

    @In
    private WorldGeneratorManager worldGeneratorManager;
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
            loadBlocks();
            loadGameWorlds();

            if (!errors.isEmpty()) {
                showErrors();
            }

            tabs.select(0);
            showErrors.setEnabled(!errors.isEmpty());
        } else {
            final MessagePopup popup = getManager().createScreen(MessagePopup.ASSET_URI, MessagePopup.class);
            popup.setMessage(translationSystem.translate("${engine:menu#game-details-errors-message-title}"),
                    translationSystem.translate("${engine:menu#game-details-errors-message-body}"));
            popup.subscribeButton(e -> triggerBackAnimation());
            getManager().pushScreen(popup);
            // disable child widgets
            setEnabled(false);
        }
    }

    private void initWidgets() {
        gameModules = find("modules", UIList.class);
        gameWorlds = find("worlds", UIList.class);
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
        getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage(translationSystem.translate(
                "${engine:menu#game-details-errors-message-title}"), errorMessageBuilder.toString());
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
            blocks.setSelection(null);
        });

        gameWorlds.setItemRenderer(new AbstractItemRenderer<WorldInfo>() {
            @Override
            public void draw(WorldInfo value, Canvas canvas) {
                canvas.drawText(value.getTitle());
            }

            @Override
            public Vector2i getPreferredSize(WorldInfo value, Canvas canvas) {
                String text = value.getTitle();
                return new Vector2i(
                        canvas.getCurrentStyle().getFont().getWidth(text),
                        canvas.getCurrentStyle().getFont().getLineHeight());
            }
        });
    }

    private String getWorldDescription(final WorldInfo worldInfo) {
        String gameTitle = worldInfo.getTitle();
        return translationSystem.translate("${engine:menu#game-details-game-title} ") + gameTitle
                + '\n' + '\n'
                + translationSystem.translate("${engine:menu#game-details-game-seed} ") + worldInfo.getSeed()
                + '\n' + '\n'
                + translationSystem.translate("${engine:menu#game-details-world-generator}: ")
                + worldGeneratorManager.getWorldGeneratorInfo(worldInfo.getWorldGenerator()).getDisplayName()
                + '\n' + '\n'
                + translationSystem.translate("${engine:menu#game-details-game-duration} ")
                + DateTimeHelper.getDeltaBetweenTimestamps(new Date(0).getTime(), worldInfo.getTime());
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
                return new Vector2i(
                        canvas.getCurrentStyle().getFont().getWidth(text),
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
                    Name name = nameVersion.getName();
                    Version version = nameVersion.getVersion();
                    Module module = moduleManager.getRegistry().getModule(name, version);
                    if (module != null) {
                        return ModuleSelectionInfo.strictVersion(module);
                    } else {
                        logger.warn("Can't find module in your classpath - {}:{}", name, version);
                        module = moduleManager.getRegistry().getLatestModuleVersion(name);
                        if (module != null) {
                            logger.debug("Get the latest available version of module {} in your classpath", name);
                            errors.add(String.format("Can't find module %s:%s in your classpath; " +
                                            "loaded description for the latest available version.",
                                    name, version));
                            return ModuleSelectionInfo.latestVersion(module);
                        }
                        logger.error("Can't find any versions of module {} in your classpath!", name);
                        errors.add(String.format("Can't find any versions of module %s in your classpath!", name));
                        return ModuleSelectionInfo.unavailableVersion(name.toString(), version.toString());
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

    private String getGeneralInfo(final GameInfo theGameInfo) {
        GameManifest manifest = theGameInfo.getManifest();

        return translationSystem.translate("${engine:menu#game-details-game-title} ")
                + manifest.getTitle() + '\n' + '\n' +
                translationSystem.translate("${engine:menu#game-details-last-play}: ")
                + DATE_FORMAT.format(theGameInfo.getTimestamp()) + '\n' + '\n' +
                translationSystem.translate("${engine:menu#game-details-game-duration} ")
                + DateTimeHelper.getDeltaBetweenTimestamps(new Date(0).getTime(), manifest.getTime()) + '\n' + '\n' +
                translationSystem.translate("${engine:menu#game-details-game-seed} ")
                + manifest.getSeed() + '\n' + '\n' +
                translationSystem.translate("${engine:menu#game-details-world-generator}: ") + '\t'
                + manifest.mainWorldDisplayName(worldGeneratorManager);
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
        if (Stream.of(gameModules, gameWorlds, blocks, description, descriptionTitle, openModuleDetails,
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
