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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.engine.TerasologyConstants;
import org.terasology.i18n.TranslationSystem;
import org.terasology.naming.Name;
import org.terasology.naming.NameVersion;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.AWTTextureFormat;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.utilities.Assets;
import org.terasology.utilities.FilesUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This abstract class has common methods and attributes used by SelectGameScreen, RecordScreen and ReplayScreen.
 */
public abstract class SelectionScreen extends CoreScreenLayer {

    private static final ResourceUrn PREVIEW_IMAGE_URI = new ResourceUrn("engine:savedGamePreview");
    private static final ResourceUrn DEFAULT_PREVIEW_IMAGE_URI = new ResourceUrn("engine:defaultPreview");

    private static final Logger logger = LoggerFactory.getLogger(SelectionScreen.class);

    protected UIImage previewImage;

    @In
    protected Config config;

    @In
    protected TranslationSystem translationSystem;

    private UILabel worldGenerator;
    private UILabel moduleNames;

    private UIList<GameInfo> gameInfos;

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    @Override
    public void onOpened() {
        super.onOpened();
        if (!config.getPlayer().hasEnteredUsername()) {
            getManager().pushScreen(EnterUsernamePopup.ASSET_URI, EnterUsernamePopup.class);
        }
    }

    void updateDescription(GameInfo item) {
        if (item == null) {
            worldGenerator.setText("");
            moduleNames.setText("");
            loadPreviewImage(null);
            return;
        }

        String mainWorldGenerator = item.getManifest()
                .getWorldInfo(TerasologyConstants.MAIN_WORLD)
                .getWorldGenerator()
                .getObjectName()
                .toString();

        String commaSeparatedModules = item.getManifest()
                .getModules()
                .stream()
                .map(NameVersion::getName)
                .map(Name::toString)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.joining(", "));

        worldGenerator.setText(mainWorldGenerator);
        moduleNames.setText(commaSeparatedModules);

        loadPreviewImage(item);
    }

    private void loadPreviewImage(GameInfo item) {
        Texture texture;
        if (item != null && item.getPreviewImage() != null) {
            TextureData textureData = null;
            try {
                textureData = AWTTextureFormat.convertToTextureData(item.getPreviewImage(), Texture.FilterMode.LINEAR);
            } catch (IOException e) {
                logger.error("Converting preview image to texture data {} failed", e);
            }
            texture = Assets.generateAsset(PREVIEW_IMAGE_URI, textureData, Texture.class);
        } else {
            texture = Assets.getTexture(DEFAULT_PREVIEW_IMAGE_URI).get();
        }

        previewImage.setImage(texture);
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
                logger.error("Failed to delete " + removeString, e);
                getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error Deleting Game", e.getMessage());
            }
        }
    }

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());
        if (!initScreenWidgets()) {
            getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error", "Can't initialize the screen!");
        }
    }

    private boolean initScreenWidgets() {
        worldGenerator = find("worldGenerator", UILabel.class);
        moduleNames = find("moduleNames", UILabel.class);
        previewImage = find("previewImage", UIImage.class);
        gameInfos = find("gameList", UIList.class);
        if (worldGenerator == null || moduleNames == null || gameInfos == null || previewImage == null) {
            logger.error("Screen can't be initialized correctly, because required widgets are missed!\nworldGenerator = {}, moduleNames = {}, previewImage = {}, gameList = {}", worldGenerator, moduleNames, previewImage, gameInfos);
            return false;
        }
        return true;
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
        final UILabel saveGamePath = find("saveGamePath", UILabel.class);
        if (saveGamePath != null) {
            saveGamePath.setText(
                    translationSystem.translate("${engine:menu#save-game-path} ") +
                            savePath.toAbsolutePath().toString());
        } else {
            logger.warn("Can't find saveGamePath widget!");
        }
    }
}
