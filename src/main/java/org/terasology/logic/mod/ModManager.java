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

package org.terasology.logic.mod;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.terasology.asset.sources.ArchiveSource;
import org.terasology.asset.sources.DirectorySource;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.PathManager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Immortius
 */
public class ModManager {

    private static final String ASSETS_SUBDIRECTORY = "assets";

    private Logger logger = Logger.getLogger(getClass().getName());
    private Map<String, Mod> mods = Maps.newHashMap();
    private ClassLoader activeModClassLoader;

    private Reflections engineReflections;
    private Reflections activeModReflections;

    public ModManager() {
        engineReflections = new Reflections(
                new ConfigurationBuilder()
                        .addClassLoader(getClass().getClassLoader())
                        .addUrls(ClasspathHelper.forClassLoader(getClass().getClassLoader()))
                        .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));
        refresh();
    }

    public Reflections getEngineReflections() {
        return engineReflections;
    }

    public Reflections getActiveModReflections() {
        if (activeModReflections != null) {
            return activeModReflections;
        }
        return engineReflections;
    }

    public void refresh() {
        mods.clear();
        Gson gson = new Gson();
        File modPath = PathManager.getInstance().getModPath();

        // Directories first (they should override zips)
        for (File modFile : modPath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        })) {
            File modInfoFile = new File(modFile, "mod.txt");
            if (modInfoFile.exists()) {
                try {
                    ModInfo modInfo = gson.fromJson(new FileReader(modInfoFile), ModInfo.class);
                    if (!mods.containsKey(modInfo.getId())) {
                        mods.put(modInfo.getId(), new Mod(modFile, modInfo, new DirectorySource(modInfo.getId(), new File(modFile, ASSETS_SUBDIRECTORY))));
                        logger.info("Discovered mod: " + modInfo.getDisplayName());
                    } else {
                        logger.info("Discovered duplicate mod: " + modInfo.getDisplayName() + ", skipping");
                    }
                } catch (FileNotFoundException e) {
                    logger.log(Level.WARNING, "Failed to load mod manifest for mod at " + modFile, e);
                } catch (JsonIOException e) {
                    logger.log(Level.WARNING, "Failed to load mod manifest for mod at " + modFile, e);
                }
            }
        }

        // Zip files next
        for (File modFile : modPath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && (pathname.getName().endsWith(".zip") || pathname.getName().endsWith(".jar"));
            }
        })) {
            try {
                ZipFile zipFile = new ZipFile(modFile);
                ZipEntry modInfoEntry = zipFile.getEntry("mod.txt");
                if (modInfoEntry != null) {
                    try {
                        ModInfo modInfo = gson.fromJson(new InputStreamReader(zipFile.getInputStream(modInfoEntry)), ModInfo.class);
                        if (!mods.containsKey(modInfo.getId())) {
                            mods.put(modInfo.getId(), new Mod(modFile, modInfo, new ArchiveSource(modInfo.getId(), modFile, ASSETS_SUBDIRECTORY)));
                            logger.info("Discovered mod: " + modInfo.getDisplayName());
                        } else {
                            logger.info("Discovered duplicate mod: " + modInfo.getDisplayName() + ", skipping");
                        }
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
        for (String activeModId : Config.getInstance().getActiveMods()) {
            Mod mod = mods.get(activeModId);
            if (mod != null) {
                mod.setEnabled(true);
            }
        }

        activeModClassLoader = null;
    }

    public void applyActiveMods() {
        List<URL> urls = Lists.newArrayList();
        for (Mod mod : getActiveMods()) {
            urls.add(mod.getModClasspathUrl());
        }
        activeModClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
        for (Mod mod : getActiveMods()) {
            mod.setClassLoader(activeModClassLoader);
        }
        activeModReflections = new Reflections(new ConfigurationBuilder().addClassLoader(getClass().getClassLoader()).addClassLoader(activeModClassLoader));
        activeModReflections.merge(getEngineReflections());
        for (Mod mod : getActiveMods()) {
            activeModReflections.merge(mod.getReflections());
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
