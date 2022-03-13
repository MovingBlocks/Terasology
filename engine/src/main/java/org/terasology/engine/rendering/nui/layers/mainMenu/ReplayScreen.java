// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.modes.StateLoading;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplayStatus;
import org.terasology.engine.recording.RecordAndReplayUtils;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.engine.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.nui.widgets.UIButton;

import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Screen for the replay menu.
 */
public class ReplayScreen extends SelectionScreen {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:replayScreen");
    private static final String REMOVE_STRING = "replay";
    private static final Logger logger = LoggerFactory.getLogger(ReplayScreen.class);

    private RecordAndReplayUtils recordAndReplayUtils;

    // widgets
    private UIButton load;
    private UIButton delete;
    private UIButton close;

    @In
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        initWidgets();

        if (isValidScreen()) {

            initSaveGamePathWidget(PathManager.getInstance().getRecordingsPath());

            getGameInfos().subscribeSelection((widget, item) -> {
                load.setEnabled(item != null);
                delete.setEnabled(item != null);
                updateDescription(item);
            });

            getGameInfos().subscribe((widget, item) -> loadGame(item));

            load.subscribe(e -> {
                GameInfo gameInfo = getGameInfos().getSelection();
                if (gameInfo != null) {
                    loadGame(gameInfo);
                }
            });

            delete.subscribe(button -> {
                TwoButtonPopup confirmationPopup = getManager().pushScreen(TwoButtonPopup.ASSET_URI,
                        TwoButtonPopup.class);
                confirmationPopup.setMessage(translationSystem.translate("${engine:menu#remove-confirmation-popup" +
                                "-title}"),
                        translationSystem.translate("${engine:menu#remove-confirmation-popup-message}"));
                confirmationPopup.setLeftButton(translationSystem.translate("${engine:menu#dialog-yes}"),
                        this::removeSelectedReplay);
                confirmationPopup.setRightButton(translationSystem.translate("${engine:menu#dialog-no}"), () -> {
                });
            });

            close.subscribe(button -> {
                recordAndReplayCurrentStatus.setStatus(RecordAndReplayStatus.NOT_ACTIVATED);
                triggerBackAnimation();
            });
        }
    }

    @Override
    public void onOpened() {
        super.onOpened();

        if (isValidScreen()) {
            refreshGameInfoList(GameProvider.getSavedRecordings());
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

    @Override
    protected void initWidgets() {
        super.initWidgets();
        load = find("load", UIButton.class);
        delete = find("delete", UIButton.class);
        close = find("close", UIButton.class);
    }

    private void removeSelectedReplay() {
        final Path world =
                PathManager.getInstance().getRecordingPath(getGameInfos().getSelection().getManifest().getTitle());
        remove(getGameInfos(), world, REMOVE_STRING);
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
            getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error Loading Game",
                    e.getMessage());
        }
    }

    void setRecordAndReplayUtils(RecordAndReplayUtils recordAndReplayUtils) {
        this.recordAndReplayUtils = recordAndReplayUtils;
    }

    @Override
    protected boolean isValidScreen() {
        if (Stream.of(load, delete, close).anyMatch(Objects::isNull) || !super.isValidScreen()) {
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
