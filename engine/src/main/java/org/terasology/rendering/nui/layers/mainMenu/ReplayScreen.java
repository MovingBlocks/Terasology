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
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkMode;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.recording.RecordAndReplayStatus;
import org.terasology.recording.RecordAndReplayUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.rendering.nui.widgets.UIButton;

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
                TwoButtonPopup confirmationPopup = getManager().pushScreen(TwoButtonPopup.ASSET_URI, TwoButtonPopup.class);
                confirmationPopup.setMessage(translationSystem.translate("${engine:menu#remove-confirmation-popup-title}"),
                        translationSystem.translate("${engine:menu#remove-confirmation-popup-message}"));
                confirmationPopup.setLeftButton(translationSystem.translate("${engine:menu#dialog-yes}"), this::removeSelectedReplay);
                confirmationPopup.setRightButton(translationSystem.translate("${engine:menu#dialog-no}"), () -> { });
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
            popup.setMessage(translationSystem.translate("${engine:menu#game-details-errors-message-title}"), translationSystem.translate("${engine:menu#game-details-errors-message-body}"));
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
        final Path world = PathManager.getInstance().getRecordingPath(getGameInfos().getSelection().getManifest().getTitle());
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
            getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error Loading Game", e.getMessage());
        }
    }

    void setRecordAndReplayUtils(RecordAndReplayUtils recordAndReplayUtils) {
        this.recordAndReplayUtils = recordAndReplayUtils;
    }

    @Override
    protected boolean isValidScreen() {
        if (Stream.of(load, delete, close)
                .anyMatch(Objects::isNull) ||
                !super.isValidScreen()) {
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
