// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.core.paths.PathManager;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.engine.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.nui.widgets.UIButton;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplayStatus;
import org.terasology.engine.recording.RecordAndReplayUtils;
import org.terasology.engine.registry.In;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Screen for the record menu.
 */
public class RecordScreen extends SelectionScreen {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:recordScreen");

    private static final Logger logger = LoggerFactory.getLogger(RecordScreen.class);

    private RecordAndReplayUtils recordAndReplayUtils;

    @In
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;

    // widgets
    private UIButton load;
    private UIButton close;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        initWidgets();

        if (isValidScreen()) {

            initSaveGamePathWidget(PathManager.getInstance().getSavesPath());

            NameRecordingScreen nameRecordingScreen = getManager().createScreen(NameRecordingScreen.ASSET_URI, NameRecordingScreen.class);

            getGameInfos().subscribeSelection((widget, item) -> {
                load.setEnabled(item != null);
                updateDescription(item);
            });

            getGameInfos().subscribe((widget, item) -> launchNamingScreen(nameRecordingScreen, item));

            load.subscribe(button -> {
                final GameInfo gameInfo = getGameInfos().getSelection();
                if (gameInfo != null) {
                    launchNamingScreen(nameRecordingScreen, gameInfo);
                }
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
            refreshGameInfoList(GameProvider.getSavedGames());
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
        close = find("close", UIButton.class);
    }

    /**
     * Launches {@link NameRecordingScreen} with the info of the game selected in this screen.
     *
     * @param nameRecordingScreen The instance of the screen to launch
     * @param info The info of the selected game.
     */
    private void launchNamingScreen(NameRecordingScreen nameRecordingScreen, GameInfo info) {
        nameRecordingScreen.setGameInfo(info);
        nameRecordingScreen.setRecordAndReplayUtils(recordAndReplayUtils);
        triggerForwardAnimation(nameRecordingScreen);
    }

    void setRecordAndReplayUtils(RecordAndReplayUtils recordAndReplayUtils) {
        this.recordAndReplayUtils = recordAndReplayUtils;
    }

    @Override
    protected boolean isValidScreen() {
        if (Stream.of(load, close)
                .anyMatch(Objects::isNull)
                || !super.isValidScreen()) {
            logger.error("Can't initialize screen correctly. At least one widget was missed!");
            return false;
        }
        return true;
    }

}
