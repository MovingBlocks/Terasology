/*
 * Copyright 2012
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

import com.google.common.io.Files;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;

/**
 * @author Immortius
 */
public final class PathManager {
    private static final String WORLD_DIR = "SAVED_WORLDS";
    private static final String LOG_DIR = "logs";
    private static final String MOD_DIR = "mods";

    private static PathManager instance;
    private File rootPath;
    private File worldPath;
    private File logPath;
    private File modPath;

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
            if (SystemUtils.IS_OS_WINDOWS) {
                rootPath = new File(System.getenv("APPDATA") + "\\.terasology");
            } else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_SOLARIS) {
                rootPath = new File("~/.terasology");
            } else if (SystemUtils.IS_OS_MAC) {
                rootPath = new File(SystemUtils.getUserHome() + "/Library/" + "terasology");
            } else {
                rootPath = new File(SystemUtils.getUserHome() + "/.terasology");
            }
        } else {
            rootPath = new File(".").getAbsoluteFile();
        }
        updateDirs();
    }

    public File getWorldSavePath(String worldTitle) {
        // TODO: remove special characters from world title
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


    private void updateDirs() {
        rootPath.mkdirs();
        worldPath = new File(rootPath, WORLD_DIR);
        worldPath.mkdirs();
        logPath = new File(rootPath, LOG_DIR);
        logPath.mkdirs();
        modPath = new File(rootPath, MOD_DIR);
        modPath.mkdirs();
    }
}
