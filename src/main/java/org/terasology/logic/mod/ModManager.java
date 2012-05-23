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

package org.terasology.logic.mod;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.terasology.asset.sources.ArchiveSource;
import org.terasology.asset.sources.DirectorySource;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.PathManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;

/**
 * @author Immortius
 */
public class ModManager {

    private Logger logger = Logger.getLogger(getClass().getName());
    private Map<String,Mod> mods = Maps.newHashMap();

    public ModManager() {
        refresh();
    }

    public void refresh() {
        mods.clear();
        Gson gson = new Gson();
        File modPath = PathManager.getInstance().getModPath();
        for(File modFile : modPath.listFiles()) {
            if (modFile.isDirectory())
            {
                File modInfoFile = new File(modFile.getPath(), "mod.txt");
                if (modInfoFile.exists()) {
                    try {
                        ModInfo modInfo = gson.fromJson(new FileReader(modInfoFile), ModInfo.class);
                        mods.put(modInfo.getId(), new Mod(modFile, modInfo, new DirectorySource(modInfo.getId(), modFile)));
                        logger.info("Discovered mod: " + modInfo.getDisplayName());
                    } catch (FileNotFoundException e) {
                        logger.log(Level.WARNING, "Failed to load mod manifest for mod at " + modFile, e);
                    } catch (JsonIOException e) {
                        logger.log(Level.WARNING, "Failed to load mod manifest for mod at " + modFile, e);
                    }
                }
            } else if (modFile.isFile() && modFile.getName().endsWith(".zip")) {
                try {
                    ZipFile zipFile = new ZipFile(modFile);
                    ZipEntry modInfoEntry = zipFile.getEntry("mod.txt");
                    if (modInfoEntry != null) {
                        try {
                            ModInfo modInfo = gson.fromJson(new InputStreamReader(zipFile.getInputStream(modInfoEntry)), ModInfo.class);
                            mods.put(modInfo.getId(), new Mod(modFile, modInfo, new ArchiveSource(modInfo.getId(), modFile)));
                            logger.info("Discovered mod: " + modInfo.getDisplayName());
                        } catch (FileNotFoundException e) {
                            logger.log(Level.WARNING, "Failed to load mod manifest for mod at " + modFile, e);
                        } catch (JsonIOException e) {
                            logger.log(Level.WARNING, "Failed to load mod manifest for mod at " + modFile, e);
                        }
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Invalid mod file: " + modFile, e);
                }
            }
        }
        for (String activeModId : Config.getInstance().getActiveMods()) {
            Mod mod = mods.get(activeModId);
            if (mod != null) {
                mod.setEnabled(true);
            }
        }
     }

    public Collection<Mod> getMods() {
        return mods.values();
    }

    public void saveModSelectionToConfig() {
        List<String> activeModIds = Lists.newArrayList();
        for (Mod mod : mods.values()) {
            if (mod.isEnabled()) {
                activeModIds.add(mod.getModInfo().getId());
            }
        }
    }

    public Iterable<Mod> getActiveMods() {
        List<Mod> activeMods = Lists.newArrayListWithCapacity(mods.size());
        for (Mod mod : mods.values()) {
            if (mod.isEnabled()) {
                activeMods.add(mod);
            }
        }
        return activeMods;
    }
}
