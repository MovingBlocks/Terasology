// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.PlayerConfig;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.persistence.internal.GamePreviewImageProvider;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.texture.AWTTextureFormat;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureData;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.utilities.FilesUtil;
import org.terasology.engine.world.generator.internal.WorldGeneratorManager;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.NameVersion;
import org.terasology.nui.widgets.UIImage;
import org.terasology.nui.widgets.UIImageSlideshow;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UIList;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This abstract class has common methods and attributes used by SelectGameScreen, RecordScreen and ReplayScreen.
 */
public abstract class SelectionScreen extends CoreScreenLayer {

    private static final String PREVIEW_IMAGE_URI_PATTERN = "engine:savedGamePreview";
    private static final ResourceUrn DEFAULT_PREVIEW_IMAGE_URI = new ResourceUrn("engine:defaultPreview");
    private static final int MODULES_LINE_LIMIT = 180;

    private static final Logger logger = LoggerFactory.getLogger(SelectionScreen.class);

    @In
    protected Config config;
    @In
    protected PlayerConfig playerConfig;


    @In
    protected WorldGeneratorManager worldGeneratorManager;

    @In
    protected TranslationSystem translationSystem;

    protected UIImageSlideshow previewSlideshow;
    private UILabel worldGenerator;
    private UILabel moduleNames;
    private UIList<GameInfo> gameInfos;
    private UILabel saveGamePath;

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    void updateDescription(final GameInfo gameInfo) {
        if (gameInfo == null) {
            worldGenerator.setText("");
            moduleNames.setText("");
            loadPreviewImages(null);
            return;
        }

        GameManifest manifest = gameInfo.getManifest();
        String mainWorldGenerator = manifest.mainWorldDisplayName(worldGeneratorManager);

        final String commaSeparatedModules = manifest
                .getModules()
                .stream()
                .map(NameVersion::getName)
                .map(Name::toString)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.joining(", "));

        worldGenerator.setText(mainWorldGenerator);
        moduleNames.setText(commaSeparatedModules.length() > MODULES_LINE_LIMIT
                ? commaSeparatedModules.substring(0, MODULES_LINE_LIMIT) + "..."
                : commaSeparatedModules);

        loadPreviewImages(gameInfo);
    }

    private void loadPreviewImages(final GameInfo gameInfo) {
        List<Texture> textures = new ArrayList<>();
        if (gameInfo != null && gameInfo.getSavePath() != null) {
            final List<BufferedImage> bufferedImages = GamePreviewImageProvider.getAllPreviewImages(gameInfo.getSavePath());
            textures = bufferedImages
                    .stream()
                    .map(buffImage -> {
                        TextureData textureData;
                        try {
                            textureData = AWTTextureFormat.convertToTextureData(buffImage, Texture.FilterMode.LINEAR);
                        } catch (IOException e) {
                            logger.error("Converting preview image to texture data failed", e);
                            return null;
                        }
                        return Assets.generateAsset(new ResourceUrn(PREVIEW_IMAGE_URI_PATTERN + bufferedImages.indexOf(buffImage)),
                                textureData, Texture.class);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        if (textures.isEmpty()) {
            textures.add(Assets.getTexture(DEFAULT_PREVIEW_IMAGE_URI).get());
        }

        previewSlideshow.clean();
        textures.forEach(tex -> {
            UIImage image = new UIImage(null, tex, true);
            previewSlideshow.addImage(image);
        });
    }

    protected void remove(final UIList<GameInfo> gameList, Path world, String removeString) {
        final GameInfo gameInfo = gameList.getSelection();
        if (gameInfo != null) {
            try {
                FilesUtil.recursiveDelete(world);
                gameList.getList().remove(gameInfo);
                gameList.setSelection(null);
                gameList.select(0);
            } catch (Exception e) {
                logger.error("Failed to delete {}", removeString, e);
                getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                        .setMessage("Error Deleting Game", e.getMessage());
            }
        }
    }

    protected void initWidgets() {
        worldGenerator = find("worldGenerator", UILabel.class);
        moduleNames = find("moduleNames", UILabel.class);
        previewSlideshow = find("previewImage", UIImageSlideshow.class);
        gameInfos = find("gameList", UIList.class);
        saveGamePath = find("saveGamePath", UILabel.class);
    }

    UIList<GameInfo> getGameInfos() {
        return gameInfos;
    }

    void refreshGameInfoList(final List<GameInfo> updatedGameInfos) {
        if (gameInfos != null) {
            gameInfos.setList(updatedGameInfos);
            gameInfos.select(0);
        }
    }

    void initSaveGamePathWidget(final Path savePath) {
        saveGamePath.setText(
                translationSystem.translate("${engine:menu#save-game-path} ") +
                        savePath.toAbsolutePath().toString());
    }

    protected boolean isValidScreen() {
        if (Stream.of(worldGenerator, moduleNames, gameInfos, previewSlideshow, saveGamePath)
                .anyMatch(Objects::isNull)) {
            logger.error("Can't initialize screen correctly. At least one widget was missed!");
            return false;
        }
        return true;
    }

    public UIImageSlideshow getPreviewSlideshow() {
        return previewSlideshow;
    }
}
