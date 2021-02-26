// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.layers.mainMenu;

import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;
import org.terasology.i18n.TranslationSystem;
import org.terasology.network.NetworkMode;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UIText;
import org.terasology.recording.RecordAndReplayUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Screen for setting the name of and ultimately loading a recording.
 */
public class NameRecordingScreen extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:nameRecordingScreen!instance");

    private static final Logger logger = LoggerFactory.getLogger(NameRecordingScreen.class);

    @In
    protected Config config;

    @In
    private TranslationSystem translationSystem;

    private GameInfo gameInfo;

    private RecordAndReplayUtils recordAndReplayUtils;

    // widgets
    private UILabel description;
    private UIText nameInput;
    private UIButton enter;
    private UIButton cancel;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        initWidgets();

        enter.subscribe(button -> enterPressed());
        cancel.subscribe(button -> cancelPressed());
    }

    @Override
    public void onScreenOpened() {
        super.onScreenOpened();
        // resets the description from any earlier error messages, in case the user re-opens the screen.
        description.setText(translationSystem.translate("${engine:menu#name-recording-description}"));
    }

    /**
     * Sets the values of all widget references.
     */
    private void initWidgets() {
        description = find("description", UILabel.class);
        nameInput = find("nameInput", UIText.class);
        enter = find("enterButton", UIButton.class);
        cancel = find("cancelButton", UIButton.class);
    }

    /**
     * Activates upon pressing the enter key.
     */
    private void enterPressed() {
        if (!isNameValid(nameInput.getText())) {
            description.setText(translationSystem.translate("${engine:menu#name-recording-error-invalid}"));
            return;
        }
        if (doesRecordingExist(nameInput.getText())) {
            description.setText(translationSystem.translate("${engine:menu#name-recording-error-duplicate}"));
            return;
        }

        loadGame(nameInput.getText());
    }

    /**
     * Activates upon pressing the cancel key.
     */
    private void cancelPressed() {
        triggerBackAnimation();
    }

    /**
     * Last step of the recording setup process. Copies the save files from the selected game, transplants them into the 'recordings' folder, and renames the map files
     * to match the provided recording name. Then launches the game loading state.
     *
     * @param newTitle The title of the new recording.
     */
    private void loadGame(String newTitle) {
        try {
            final GameManifest manifest = gameInfo.getManifest();

            copySaveDirectoryToRecordingLibrary(manifest.getTitle(), newTitle);
            recordAndReplayUtils.setGameTitle(newTitle);
            config.getWorldGeneration().setDefaultSeed(manifest.getSeed());
            config.getWorldGeneration().setWorldTitle(newTitle);
            CoreRegistry.get(GameEngine.class).changeState(new StateLoading(manifest, NetworkMode.NONE));
        } catch (Exception e) {
            logger.error("Failed to load saved game", e);
            getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error Loading Game", e.getMessage());
        }
    }

    /**
     * Copies the selected save files to a new recording directory.
     *
     * @param oldTitle The name of the original save directory.
     * @param newTitle The name of the new recording directory.
     */
    private void copySaveDirectoryToRecordingLibrary(String oldTitle, String newTitle) {
        File saveDirectory = new File(PathManager.getInstance().getSavePath(oldTitle).toString());
        Path destinationPath = PathManager.getInstance().getRecordingPath(newTitle);
        File destDirectory = new File(destinationPath.toString());
        try {
            FileUtils.copyDirectoryStructure(saveDirectory, destDirectory);
            rewriteManifestTitle(destinationPath, newTitle);
        } catch (Exception e) {
            logger.error("Error trying to copy the save directory:", e);
        }
    }

    /**
     * Rewrites the title of the save game manifest to match the new directory title.
     *
     * @param destinationPath The path of the new recording files.
     * @param newTitle The new name for the recording manifest.
     * @throws IOException
     */
    private void rewriteManifestTitle(Path destinationPath, String newTitle) throws IOException {
        // simply grabs the manifest, changes it, and saves again.
        GameManifest manifest = GameManifest.load(destinationPath.resolve(GameManifest.DEFAULT_FILE_NAME));
        manifest.setTitle(newTitle);
        GameManifest.save(destinationPath.resolve(GameManifest.DEFAULT_FILE_NAME), manifest);
    }

    /**
     * Tests if the provided string is valid for a game name.
     *
     * @param name The provided name string.
     * @return true if name is valid, false otherwise.
     */
    private boolean isNameValid(String name) {
        Path destinationPath = PathManager.getInstance().getRecordingPath(name);

        // invalid characters are filtered from paths, so if the file name is made up of entirely invalid characters, the path will have a blank file name.
        // also acts as a check for blank input.
        return !destinationPath.equals(PathManager.getInstance().getRecordingPath(""));
    }

    /**
     * Tests if there is an existing recording with the provided name string.
     *
     * @param name The provided name string.
     * @return true if recording exists, false otherwise.
     */
    private boolean doesRecordingExist(String name) {
        Path destinationPath = PathManager.getInstance().getRecordingPath(name);
        return FileUtils.fileExists(destinationPath.toString());
    }

    public void setRecordAndReplayUtils(RecordAndReplayUtils recordAndReplayUtils) {
        this.recordAndReplayUtils = recordAndReplayUtils;
    }

    public void setGameInfo(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
