/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.engine.paths;

import com.google.common.collect.ImmutableList;
import org.lwjgl.LWJGLUtil;
import org.terasology.engine.paths.windows.SavedGamesPathFinder;

import javax.swing.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Immortius
 */
public final class PathManager {
    public static final String TERASOLOGY_FOLDER_NAME = "Terasology";
    public static final String TERASOLOGY_HIDDEN_FOLDER_NAME = ".terasology";

    private static final String SAVED_GAMES_DIR = "saves";
    private static final String LOG_DIR = "logs";
    private static final String MOD_DIR = "mods";
    private static final String SCREENSHOT_DIR = "screenshots";
    private static final String NATIVES_DIR = "natives";

    private static PathManager instance;
    private Path installPath;
    private Path homePath;
    private Path savesPath;
    private Path logPath;
    private Path currentWorldPath;

    private ImmutableList<Path> modPaths = ImmutableList.of();
    private Path screenshotPath;
    private Path nativesPath;

    private PathManager() {
        // By default, the path should be the code location (where terasology.jar is)
        try {
            URL urlToSource = PathManager.class.getProtectionDomain().getCodeSource().getLocation();

            Path codeLocation = Paths.get(urlToSource.toURI());
            if (Files.isRegularFile(codeLocation)) {
                installPath = codeLocation.getParent();
            }
        } catch (URISyntaxException e) {
            // Can't use logger, because logger not set up when PathManager is used.
            System.out.println("Failed to convert code location to uri");
        }
        // If terasology.jar's location could not be resolved (maybe running from an IDE) then fallback on working path
        if (installPath == null) {
            installPath = Paths.get("").toAbsolutePath();
        }
        homePath = installPath;
    }

    public static PathManager getInstance() {
        if (instance == null) {
            instance = new PathManager();
        }
        return instance;
    }

    public void useOverrideHomePath(Path rootPath) throws IOException {
        this.homePath = rootPath;
        updateDirs();
    }

    public void useDefaultHomePath() throws IOException {
        switch (LWJGLUtil.getPlatform()) {
            case LWJGLUtil.PLATFORM_LINUX:
                homePath = Paths.get(System.getProperty("user.home"), TERASOLOGY_HIDDEN_FOLDER_NAME);
                break;
            case LWJGLUtil.PLATFORM_MACOSX:
                homePath = Paths.get(System.getProperty("user.home"), "Library", "Application Support", TERASOLOGY_FOLDER_NAME);
                break;
            case LWJGLUtil.PLATFORM_WINDOWS:
                String savedGamesPath = SavedGamesPathFinder.findSavedGamesPath();
                if (savedGamesPath == null) {
                    savedGamesPath = SavedGamesPathFinder.findDocumentsPath();
                }
                Path rawPath;
                if (savedGamesPath != null) {
                    rawPath = Paths.get(savedGamesPath);
                } else {
                    rawPath = new JFileChooser().getFileSystemView().getDefaultDirectory().toPath();
                }
                homePath = rawPath.resolve(TERASOLOGY_FOLDER_NAME);
                break;
            default:
                homePath = Paths.get(System.getProperty("user.home"), TERASOLOGY_HIDDEN_FOLDER_NAME);
                break;
        }
        updateDirs();
    }

    public Path getCurrentSavePath() {
        return currentWorldPath;
    }

    public void setCurrentSaveTitle(String worldTitle) throws IOException {
        currentWorldPath = getSavePath(worldTitle);
        Files.createDirectories(currentWorldPath);
    }

    public Path getHomePath() {
        return homePath;
    }

    public Path getInstallPath() {
        return installPath;
    }

    public Path getSavesPath() {
        return savesPath;
    }

    public Path getLogPath() {
        return logPath;
    }

    public List<Path> getModulePaths() {
        return modPaths;
    }

    public Path getScreenshotPath() {
        return screenshotPath;
    }

    public Path getNativesPath() {
        return nativesPath;
    }

    private void updateDirs() throws IOException {
        Files.createDirectories(homePath);
        savesPath = homePath.resolve(SAVED_GAMES_DIR);
        Files.createDirectories(savesPath);
        logPath = homePath.resolve(LOG_DIR);
        Files.createDirectories(logPath);
        Path homeModPath = homePath.resolve(MOD_DIR);
        Files.createDirectories(homeModPath);
        Path installModPath = installPath.resolve(MOD_DIR);
        Files.createDirectories(installModPath);
        if (Files.isSameFile(homeModPath, installModPath)) {
            modPaths = ImmutableList.of(homeModPath);
        } else {
            modPaths = ImmutableList.of(installModPath, homeModPath);
        }
        screenshotPath = homePath.resolve(SCREENSHOT_DIR);
        Files.createDirectories(screenshotPath);
        nativesPath = installPath.resolve(NATIVES_DIR);
        if (currentWorldPath == null) {
            currentWorldPath = homePath;
        }
    }

    public Path getHomeModPath() {
        return modPaths.get(0);
    }

    public Path getSavePath(String title) {
        return savesPath.resolve(title.replaceAll("[^A-Za-z0-9-_ ]", ""));
    }
}
