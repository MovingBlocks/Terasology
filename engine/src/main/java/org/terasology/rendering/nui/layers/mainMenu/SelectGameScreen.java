// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.layers.mainMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkMode;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.layers.mainMenu.gameDetailsScreen.GameDetailsScreen;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

public class SelectGameScreen extends SelectionScreen {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:selectGameScreen");
    private static final String REMOVE_STRING = "saved game";
    private static final Logger logger = LoggerFactory.getLogger(SelectGameScreen.class);

    private UniverseWrapper universeWrapper;

    // widgets
    private UILabel gameTypeTitle;
    private UIButton load;
    private UIButton delete;
    private UIButton details;
    private UIButton close;
    private UIButton create;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        initWidgets();

        if (isValidScreen()) {

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

            initSaveGamePathWidget(PathManager.getInstance().getSavesPath());

            getGameInfos().subscribeSelection((widget, item) -> {
                load.setEnabled(item != null);
                delete.setEnabled(item != null);
                details.setEnabled(item != null);
                updateDescription(item);
            });

            getGameInfos().subscribe((widget, item) -> loadGame(item));

            load.subscribe(e -> {
                final GameInfo gameInfo = getGameInfos().getSelection();
                if (gameInfo != null) {
                    loadGame(gameInfo);
                }
            });

            delete.subscribe(e -> {
                TwoButtonPopup confirmationPopup = getManager().pushScreen(TwoButtonPopup.ASSET_URI, TwoButtonPopup.class);
                confirmationPopup.setMessage(translationSystem.translate("${engine:menu#remove-confirmation-popup-title}"),
                        translationSystem.translate("${engine:menu#remove-confirmation-popup-message}"));
                confirmationPopup.setLeftButton(translationSystem.translate("${engine:menu#dialog-yes}"), this::removeSelectedGame);
                confirmationPopup.setRightButton(translationSystem.translate("${engine:menu#dialog-no}"), () -> {
                });
            });

            final NewGameScreen newGameScreen = getManager().createScreen(NewGameScreen.ASSET_URI, NewGameScreen.class);
            create.subscribe(e -> {
                newGameScreen.setUniverseWrapper(universeWrapper);
                triggerForwardAnimation(newGameScreen);
            });

            close.subscribe(e -> triggerBackAnimation());

            details.subscribe(e -> {
                final GameInfo gameInfo = getGameInfos().getSelection();
                if (gameInfo != null) {
                    final GameDetailsScreen detailsScreen = getManager().createScreen(GameDetailsScreen.ASSET_URI, GameDetailsScreen.class);
                    detailsScreen.setGameInfo(gameInfo);
                    detailsScreen.setPreviews(previewSlideshow.getImages());
                    getManager().pushScreen(detailsScreen);
                }
            });
        }
    }

    private void removeSelectedGame() {
        final Path world = PathManager.getInstance().getSavePath(getGameInfos().getSelection().getManifest().getTitle());
        remove(getGameInfos(), world, REMOVE_STRING);
    }

    @Override
    public void onOpened() {
        super.onOpened();

        if (isValidScreen()) {
            if (GameProvider.isSavesFolderEmpty()) {
                final NewGameScreen newGameScreen = getManager().createScreen(NewGameScreen.ASSET_URI, NewGameScreen.class);
                newGameScreen.setUniverseWrapper(universeWrapper);
                triggerForwardAnimation(newGameScreen);
            }

            if (isLoadingAsServer() && !super.config.getPlayer().hasEnteredUsername()) {
                getManager().pushScreen(EnterUsernamePopup.ASSET_URI, EnterUsernamePopup.class);
            }

            refreshGameInfoList(GameProvider.getSavedGames());
        } else {
            final MessagePopup popup = getManager().createScreen(MessagePopup.ASSET_URI, MessagePopup.class);
            popup.setMessage(translationSystem.translate("${engine:menu#game-details-errors-message-title}"), translationSystem.translate("${engine:menu#game-details-errors-message-body}"));
            popup.subscribeButton(e -> triggerBackAnimation());
            getManager().pushScreen(popup);
            // disable child widgets
            setEnabled(false);
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
            final GameManifest manifest = item.getManifest();
            config.getWorldGeneration().setDefaultSeed(manifest.getSeed());
            config.getWorldGeneration().setWorldTitle(manifest.getTitle());
            CoreRegistry.get(GameEngine.class).changeState(new StateLoading(manifest, (isLoadingAsServer()) ? NetworkMode.DEDICATED_SERVER : NetworkMode.NONE));
        } catch (Exception e) {
            logger.error("Failed to load saved game", e);
            getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error Loading Game", e.getMessage());
        }
    }

    @Override
    protected void initWidgets() {
        super.initWidgets();
        load = find("load", UIButton.class);
        delete = find("delete", UIButton.class);
        close = find("close", UIButton.class);
        details = find("details", UIButton.class);
        create = find("create", UIButton.class);
        gameTypeTitle = find("gameTypeTitle", UILabel.class);
    }

    public boolean isLoadingAsServer() {
        return universeWrapper.getLoadingAsServer();
    }

    public void setUniverseWrapper(UniverseWrapper wrapper) {
        this.universeWrapper = wrapper;
    }

    @Override
    protected boolean isValidScreen() {
        if (Stream.of(load, delete, close, details, create, gameTypeTitle)
                .anyMatch(Objects::isNull) ||
                !super.isValidScreen()) {
            logger.error("Can't initialize screen correctly. At least one widget was missed!");
            return false;
        }
        return true;
    }
}
