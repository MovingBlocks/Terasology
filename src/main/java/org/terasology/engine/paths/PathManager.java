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

import org.lwjgl.LWJGLUtil;
import org.terasology.engine.paths.windows.SavedGamesPathFinder;

import javax.swing.*;
import java.io.File;

/**
 * @author Immortius
 */
public final class PathManager {
    private static final String WORLD_DIR = "worlds";
    private static final String LOG_DIR = "logs";
    private static final String MOD_DIR = "mods";
    private static final String SCREENSHOT_DIR = "screenshots";
    private static final String NATIVES_DIR = "natives";

    private static PathManager instance;
    private File installPath;
    private File homePath;
    private File worldPath;
    private File logPath;
    private File modPath;
    private File screenshotPath;
    private File nativesPath;

    private PathManager() {
        installPath = new File("").getAbsoluteFile();
        homePath = installPath;
        updateDirs();
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
                homePath = new File("~/.terasology");
                break;
            case LWJGLUtil.PLATFORM_MACOSX:
                homePath = new File(System.getProperty("user.home") + "/Library/Application Support/" + "Terasology");
                break;
            case LWJGLUtil.PLATFORM_WINDOWS:
                String savedGamesPath = SavedGamesPathFinder.findSavedGamesPath();
                File rawPath;
                if (savedGamesPath != null) {
                    rawPath = new File(savedGamesPath);
                } else {
                    rawPath = new JFileChooser().getFileSystemView().getDefaultDirectory();
                }
                homePath = new File(rawPath, "Terasology");
                break;
            default:
                homePath = new File(System.getProperty("user.home") + "/.terasology");
        }
        updateDirs();
    }

    public File getWorldSavePath(String worldTitle) {
        // TODO: remove special characters from world title?
        File result = new File(worldPath, worldTitle);
        result.mkdirs();
        return result;
    }

    public File getHomePath() {
        return homePath;
    }

    public File getInstallPath() {
        return installPath;
    }

    public File getWorldPath() {
        return worldPath;
    }

    public File getLogPath() {
        return logPath;
    }

    public File getModPath() {
        return modPath;
    }

    public File getScreenshotPath() {
        return screenshotPath;
    }

    public File getNativesPath() {
        return nativesPath;
    }

    private void updateDirs() {
        homePath.mkdirs();
        worldPath = new File(homePath, WORLD_DIR);
        worldPath.mkdirs();
        logPath = new File(homePath, LOG_DIR);
        logPath.mkdirs();
        modPath = new File(homePath, MOD_DIR);
        modPath.mkdirs();
        screenshotPath = new File(homePath, SCREENSHOT_DIR);
        screenshotPath.mkdirs();
        nativesPath = new File(installPath, NATIVES_DIR);
    }
}
