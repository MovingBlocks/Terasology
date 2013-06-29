/*
 * Copyright 2013 Moving Blocks
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
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
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
    private File installPath;
    private File homePath;
    private File savesPath;
    private File logPath;
    private File homeModPath;
    private File installModPath;
    private File currentWorldPath;

    private ImmutableList<File> modPaths = ImmutableList.of();
    private File screenshotPath;
    private File nativesPath;

    private PathManager() {
        // By default, the path should be the code location (where terasology.jar is)
        try {
            URL path = PathManager.class.getProtectionDomain().getCodeSource().getLocation();
            File codeLocation = new File(path.toURI());
            if (codeLocation.isFile()) {
                installPath = codeLocation.getParentFile();
            }
        } catch (URISyntaxException e) {
            // Can't use logger, because logger not set up when PathManager is used.
            System.out.println("Failed to convert code location to uri");
        }
        // If terasology.jar's location could not be resolved (maybe running from an IDE) then fallback on working path
        if (installPath == null) {
            installPath = new File("").getAbsoluteFile();
        }
        homePath = installPath;
    }

    public static PathManager getInstance() {
        if (instance == null) {
            instance = new PathManager();
        }
        return instance;
    }

    public void useOverrideHomePath(File rootPath) {
        this.homePath = rootPath;
        updateDirs();
    }

    public void useDefaultHomePath() {
        switch (LWJGLUtil.getPlatform()) {
            case LWJGLUtil.PLATFORM_LINUX:
                homePath = new File(new File(System.getProperty("user.home")), TERASOLOGY_HIDDEN_FOLDER_NAME);
                break;
            case LWJGLUtil.PLATFORM_MACOSX:
                homePath = new File(new File(System.getProperty("user.home")), "Library/Application Support/" + TERASOLOGY_FOLDER_NAME);
                break;
            case LWJGLUtil.PLATFORM_WINDOWS:
                String savedGamesPath = SavedGamesPathFinder.findSavedGamesPath();
                if (savedGamesPath == null) {
                    savedGamesPath = SavedGamesPathFinder.findDocumentsPath();
                }
                File rawPath;
                if (savedGamesPath != null) {
                    rawPath = new File(savedGamesPath);
                } else {
                    rawPath = new JFileChooser().getFileSystemView().getDefaultDirectory();
                }
                homePath = new File(rawPath, TERASOLOGY_FOLDER_NAME);
                break;
            default:
                homePath = new File(new File(System.getProperty("user.home")), TERASOLOGY_HIDDEN_FOLDER_NAME);
                break;
        }
        updateDirs();
    }

    public File getCurrentSavePath() {
        return currentWorldPath;
    }

    public void setCurrentSaveTitle(String worldTitle) {
        currentWorldPath = getSavePath(worldTitle);
        currentWorldPath.mkdirs();
    }

    public File getHomePath() {
        return homePath;
    }

    public File getInstallPath() {
        return installPath;
    }

    public File getSavesPath() {
        return savesPath;
    }

    public File getLogPath() {
        return logPath;
    }

    public List<File> getModPaths() {
        return modPaths;
    }

    public File getScreenshotPath() {
        return screenshotPath;
    }

    public File getNativesPath() {
        return nativesPath;
    }

    private void updateDirs() {
        homePath.mkdirs();
        savesPath = new File(homePath, SAVED_GAMES_DIR);
        savesPath.mkdirs();
        logPath = new File(homePath, LOG_DIR);
        logPath.mkdirs();
        homeModPath = new File(homePath, MOD_DIR);
        homeModPath.mkdirs();
        installModPath = new File(installPath, MOD_DIR);
        installModPath.mkdirs();
        if (homeModPath.equals(installModPath)) {
            modPaths = ImmutableList.of(homeModPath);
        } else {
            modPaths = ImmutableList.of(homeModPath, installModPath);
        }
        screenshotPath = new File(homePath, SCREENSHOT_DIR);
        screenshotPath.mkdirs();
        nativesPath = new File(installPath, NATIVES_DIR);
        if (currentWorldPath == null) {
            currentWorldPath = homePath;
        }
    }

    public File getHomeModPath() {
        return modPaths.get(0);
    }

    public File getSavePath(String title) {
        return new File(savesPath, title.replaceAll("[^A-Za-z0-9-_ ]", ""));
    }
}
