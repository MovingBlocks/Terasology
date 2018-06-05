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
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;
import org.terasology.i18n.TranslationSystem;
import org.terasology.naming.Name;
import org.terasology.naming.NameVersion;
import org.terasology.network.NetworkMode;
import org.terasology.recording.RecordAndReplayStatus;
import org.terasology.recording.RecordAndReplayUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.AWTTextureFormat;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.utilities.Assets;
import org.terasology.utilities.FilesUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Screen for the replay menu.
 */
public class ReplayScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:replayScreen");
    public static final ResourceUrn PREVIEW_IMAGE_URI = new ResourceUrn("engine:savedGamePreview");
    public static final ResourceUrn DEFAULT_PREVIEW_IMAGE_URI = new ResourceUrn("engine:defaultPreview");

    private static final Logger logger = LoggerFactory.getLogger(ReplayScreen.class);

    private UIImage previewImage;
    private UILabel worldGenerator;
    private UILabel moduleNames;
    private RecordAndReplayUtils recordAndReplayUtils;

    @In
    private Config config;

    @In
    private TranslationSystem translationSystem;


    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        final UILabel saveGamePath = find("saveGamePath", UILabel.class);
        if (saveGamePath != null) {
            Path recordingPath = PathManager.getInstance().getRecordingsPath();
            saveGamePath.setText(
                    translationSystem.translate("${engine:menu#save-game-path} ") +
                            recordingPath.toAbsolutePath().toString()); //save path
        }

        final UIList<GameInfo> gameList = find("gameList", UIList.class);

        refreshList(gameList);

        gameList.subscribeSelection((widget, item) -> {
            find("load", UIButton.class).setEnabled(item != null);
            find("delete", UIButton.class).setEnabled(item != null);
//            find("details", UIButton.class).setEnabled(item != null);
            updateDescription(item);
        });

        worldGenerator = find("worldGenerator", UILabel.class);
        moduleNames = find("moduleNames", UILabel.class);

        gameList.select(0);
        gameList.subscribe((widget, item) -> loadGame(item));

        WidgetUtil.trySubscribe(this, "load", button -> {
            GameInfo gameInfo = gameList.getSelection();
            if (gameInfo != null) {
                loadGame(gameInfo);
            }
        });

        WidgetUtil.trySubscribe(this, "delete", button -> {
            TwoButtonPopup confirmationPopup = getManager().pushScreen(TwoButtonPopup.ASSET_URI, TwoButtonPopup.class);
            confirmationPopup.setMessage(translationSystem.translate("${engine:menu#remove-confirmation-popup-title}"),
                    translationSystem.translate("${engine:menu#remove-confirmation-popup-message}"));
            confirmationPopup.setLeftButton(translationSystem.translate("${engine:menu#dialog-yes}"), () -> removeSelectedReplay(gameList));
            confirmationPopup.setRightButton(translationSystem.translate("${engine:menu#dialog-no}"), () -> { });
        });

        WidgetUtil.trySubscribe(this, "close", button -> {
            RecordAndReplayStatus.setCurrentStatus(RecordAndReplayStatus.NOT_ACTIVATED);
            triggerBackAnimation();
        });
    }

    private void updateDescription(GameInfo item) {
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

    private void removeSelectedReplay(final UIList<GameInfo> gameList) {
        GameInfo gameInfo = gameList.getSelection();
        if (gameInfo != null) {
            Path world = PathManager.getInstance().getRecordingPath(gameInfo.getManifest().getTitle());
            try {
                FilesUtil.recursiveDelete(world);
                gameList.getList().remove(gameInfo);
                gameList.setSelection(null);
            } catch (Exception e) {
                logger.error("Failed to delete replay", e);
                getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error Deleting Game", e.getMessage());
            }
        }
    }

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

    private void loadGame(GameInfo item) {
        try {
            GameManifest manifest = item.getManifest();
            recordAndReplayUtils.setGameTitle(manifest.getTitle());
            config.getWorldGeneration().setDefaultSeed(manifest.getSeed());
            config.getWorldGeneration().setWorldTitle(manifest.getTitle());
            CoreRegistry.get(GameEngine.class).changeState(new StateLoading(manifest, NetworkMode.NONE));
        } catch (Exception e) {
            logger.error("Failed to load saved game", e);
            getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error Loading Game", e.getMessage());
        }
    }

    private void refreshList(UIList<GameInfo> gameList) {
        gameList.setList(GameProvider.getSavedRecordings());
    }

    private void loadPreviewImage(GameInfo item) {
        Texture texture;
        if (item != null && item.getPreviewImage() != null) {
            TextureData textureData = null;
            try {
                textureData = AWTTextureFormat.convertToTextureData(item.getPreviewImage(), Texture.FilterMode.LINEAR);
            } catch( IOException e ) {
                logger.error("Converting preview image to texture data {} failed", e);
            }
            texture = Assets.generateAsset(PREVIEW_IMAGE_URI, textureData, Texture.class);
        } else {
            texture = Assets.getTexture(DEFAULT_PREVIEW_IMAGE_URI).get();
        }

        previewImage = find("previewImage", UIImage.class);
        previewImage.setImage(texture);
    }

    void setRecordAndReplayUtils(RecordAndReplayUtils recordAndReplayUtils) {
        this.recordAndReplayUtils = recordAndReplayUtils;
    }
}
