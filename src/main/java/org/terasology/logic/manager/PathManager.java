/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.logic.manager;

import java.io.File;

import org.lwjgl.LWJGLUtil;

/**
 * @author Immortius
 */
public final class PathManager {
    private static final String WORLD_DIR = "SAVED_WORLDS";
    private static final String LOG_DIR = "logs";
    private static final String MOD_DIR = "mods";
    private static final String SCREENS_DIR = "screens";
    private static final String CONFIG_DIR = "config";

    private static PathManager instance;
    private File rootPath;
    private File worldPath;
    private File logPath;
    private File modPath;
    private File screenPath;

    private PathManager() {
        determineRootPath(false);
    }

    public static PathManager getInstance() {
        if (instance == null) {
            instance = new PathManager();
        }
        return instance;
    }

    public void determineRootPath(boolean useAppDir) {
        if (!useAppDir) {
            switch (LWJGLUtil.getPlatform()) {
                case LWJGLUtil.PLATFORM_LINUX:
                    rootPath = new File("~/.terasology");
                    break;
                case LWJGLUtil.PLATFORM_MACOSX:
                    rootPath = new File(System.getProperty("user.home") + "/Library/Application Support/" + "Terasology");
                    break;
                case LWJGLUtil.PLATFORM_WINDOWS:
                    rootPath = new File(System.getenv("APPDATA") + "\\.terasology");
                    break;
                default:
                    rootPath = new File(System.getProperty("user.home") + "/.terasology");
            }
        } else {
            rootPath = new File("").getAbsoluteFile();
        }
        updateDirs();
    }

    public File getWorldSavePath(String worldTitle) {
        // TODO: remove special characters from world title?
        File result = new File(worldPath, worldTitle);
        result.mkdirs();
        return result;
    }

    public File getDataPath() {
        return rootPath;
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

    public File getScreensPath() {
        return screenPath;
    }

    private void updateDirs() {
        rootPath.mkdirs();
        worldPath = new File(rootPath, WORLD_DIR);
        worldPath.mkdirs();
        logPath = new File(rootPath, LOG_DIR);
        logPath.mkdirs();
        modPath = new File(rootPath, MOD_DIR);
        modPath.mkdirs();
        screenPath = new File(rootPath, SCREENS_DIR);
        screenPath.mkdirs();
    }
}
