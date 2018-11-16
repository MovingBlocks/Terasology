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

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkMode;
import org.terasology.recording.RecordAndReplayUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIText;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class NameRecordingScreen extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:nameRecordingScreen!instance");

    private static final Logger logger = LoggerFactory.getLogger(NameRecordingScreen.class);

    @In
    protected Config config;

    private GameInfo gameInfo;

    private RecordAndReplayUtils recordAndReplayUtils;

    // widgets
    private UILabel title;
    private UILabel description;
    private UIText nameInput;
    private UIButton enter;
    private UIButton cancel;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        initWidgets();

        // TODO: Translation strings for rename prompt
        title.setText("Name Recording");
        description.setText("Set a unique name for this recording.");

        enter.subscribe(button -> enterPressed());

        cancel.subscribe(button -> cancelPressed());
    }

    private void initWidgets() {
        title = find("title", UILabel.class);
        description = find("description", UILabel.class);
        nameInput = find("nameInput", UIText.class);
        enter = find("enterButton", UIButton.class);
        cancel = find("cancelButton", UIButton.class);
    }

    private void enterPressed() { // TODO: More translation strings
        if(!isNameValid(nameInput.getText())) {
            description.setText("This name is blank, or has disallowed characters in it! Please set a different name.");
            return;
        }
        if(doesRecordingExist(nameInput.getText())) {
            description.setText("This name is already taken! Please set a different name.");
            return;
        }

        loadGame(nameInput.getText());
    }

    private void cancelPressed() {
        triggerBackAnimation();
    }

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

    private void copySaveDirectoryToRecordingLibrary(String oldTitle, String newTitle) {
        File saveDirectory = new File(PathManager.getInstance().getSavePath(oldTitle).toString());
        Path destinationPath = PathManager.getInstance().getRecordingPath(newTitle);
        File destDirectory = new File(destinationPath.toString());
        try {
            FileUtils.copyDirectoryStructure(saveDirectory, destDirectory);
            rewriteRecordingTitle(destinationPath, newTitle);
        } catch (Exception e) {
            logger.error("Error trying to copy the save directory:", e);
        }
    }

    private void rewriteRecordingTitle(Path destinationPath, String newTitle) throws IOException {
        GameManifest manifest = GameManifest.load(destinationPath.resolve(GameManifest.DEFAULT_FILE_NAME));
        manifest.setTitle(newTitle);
        GameManifest.save(destinationPath.resolve(GameManifest.DEFAULT_FILE_NAME), manifest);
    }

    private boolean isNameValid(String name) { // In the future, this probably also check for invalid file characters (?, ", .) etc.
        return !StringUtils.isBlank(name); // newGameScreen does it this way, so it should be fine..?
    }

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
}
