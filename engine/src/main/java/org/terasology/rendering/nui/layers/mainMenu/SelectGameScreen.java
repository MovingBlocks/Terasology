/*
 * Copyright 2018 MovingBlocks
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
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.AWTTextureFormat;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.utilities.Assets;
import org.terasology.utilities.FilesUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class SelectGameScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:selectGameScreen");
    public static final ResourceUrn PREVIEW_IMAGE_URI = new ResourceUrn("engine:savedGamePreview");
    public static final ResourceUrn DEFAULT_PREVIEW_IMAGE_URI = new ResourceUrn("engine:defaultPreview");

    private static final Logger logger = LoggerFactory.getLogger(SelectGameScreen.class);

    @In
    private Config config;

    @In
    private TranslationSystem translationSystem;

    private UniverseWrapper universeWrapper;

    private UIImage previewImage;
    private UILabel worldGenerator;
    private UILabel moduleNames;

    @Override
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

        final UILabel saveGamePath = find("saveGamePath", UILabel.class);
        if (saveGamePath != null) {
            saveGamePath.setText(
                    translationSystem.translate("${engine:menu#save-game-path} ") +
                            PathManager.getInstance().getSavesPath().toAbsolutePath().toString());
        }

        final UIList<GameInfo> gameList = find("gameList", UIList.class);

        refreshList(gameList);

        gameList.subscribeSelection((widget, item) -> {
            find("load", UIButton.class).setEnabled(item != null);
            find("delete", UIButton.class).setEnabled(item != null);
            find("details", UIButton.class).setEnabled(item != null);
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
            confirmationPopup.setLeftButton(translationSystem.translate("${engine:menu#dialog-yes}"), () -> removeSelectedGame(gameList));
            confirmationPopup.setRightButton(translationSystem.translate("${engine:menu#dialog-no}"), () -> { });
        });
        NewGameScreen newGameScreen = getManager().createScreen(NewGameScreen.ASSET_URI, NewGameScreen.class);
        WidgetUtil.trySubscribe(this, "create", button -> {
            newGameScreen.setUniverseWrapper(universeWrapper);
            triggerForwardAnimation(newGameScreen);
        });

        WidgetUtil.trySubscribe(this, "close", button -> triggerBackAnimation());

        WidgetUtil.trySubscribe(this, "details", button -> {
            GameInfo gameInfo = gameList.getSelection();
            if (gameInfo != null) {
                GameDetailsScreen detailsScreen = getManager().createScreen(GameDetailsScreen.ASSET_URI, GameDetailsScreen.class);
                detailsScreen.setGameInfo(gameInfo);
                detailsScreen.setPreviewImage(previewImage.getImage());
                getManager().pushScreen(detailsScreen);
            }
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

    private void removeSelectedGame(final UIList<GameInfo> gameList) {
        GameInfo gameInfo = gameList.getSelection();
        if (gameInfo != null) {
            Path world = PathManager.getInstance().getSavePath(gameInfo.getManifest().getTitle());
            try {
                FilesUtil.recursiveDelete(world);
                gameList.getList().remove(gameInfo);
                gameList.setSelection(null);
            } catch (Exception e) {
                logger.error("Failed to delete saved game", e);
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

        if (GameProvider.getSavedGames().isEmpty()) {
            CreateGameScreen screen = getManager().createScreen(CreateGameScreen.ASSET_URI, CreateGameScreen.class);
            screen.setLoadingAsServer(isLoadingAsServer());
            getManager().pushScreen(screen);
        }

        if (isLoadingAsServer() && !config.getPlayer().hasEnteredUsername()) {
            getManager().pushScreen(EnterUsernamePopup.ASSET_URI, EnterUsernamePopup.class);
        }
    }

    private void loadGame(GameInfo item) {
        if (isLoadingAsServer()) {
            Path blacklistPath = PathManager.getInstance().getHomePath().resolve("blacklist.json");
            Path whitelistPath = PathManager.getInstance().getHomePath().resolve("whitelist.json");
            if (!Files.exists(blacklistPath)) {
                try {
                    Files.createFile(blacklistPath);
                } catch (IOException e) {
                    logger.error("IO Exception on blacklist generation", e);
                }
            }
            if (!Files.exists(whitelistPath)) {
                try {
                    Files.createFile(whitelistPath);
                } catch (IOException e) {
                    logger.error("IO Exception on whitelist generation", e);
                }
            }
        }
        try {
            GameManifest manifest = item.getManifest();

            config.getWorldGeneration().setDefaultSeed(manifest.getSeed());
            config.getWorldGeneration().setWorldTitle(manifest.getTitle());
            CoreRegistry.get(GameEngine.class).changeState(new StateLoading(manifest, (isLoadingAsServer()) ? NetworkMode.DEDICATED_SERVER : NetworkMode.NONE));
        } catch (Exception e) {
            logger.error("Failed to load saved game", e);
            getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error Loading Game", e.getMessage());
        }
    }

    public boolean isLoadingAsServer() {
        return universeWrapper.getLoadingAsServer();
    }

    public void setUniverseWrapper(UniverseWrapper wrapper) {
        this.universeWrapper = wrapper;
    }

    private void refreshList(UIList<GameInfo> gameList) {
        gameList.setList(GameProvider.getSavedGames());
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

        previewImage = find("previewImage", UIImage.class);
        previewImage.setImage(texture);
    }
}
