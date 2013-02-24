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
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetSource;
import org.terasology.asset.sources.ArchiveSource;
import org.terasology.asset.sources.DirectorySource;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This manager handles the available mods, which ones are active and access to their assets and code
 *
 * @author Immortius
 */
public class ModManager {

    public static final String ENGINE_PACKAGE = "engine";

    public static final String ASSETS_SUBDIRECTORY = "assets";
    public static final String OVERRIDES_SUBDIRECTORY = "overrides";

    private static final Logger logger = LoggerFactory.getLogger(ModManager.class);

    private Map<String, Mod> mods = Maps.newHashMap();
    private ClassLoader activeModClassLoader;
    private ClassLoader allModClassLoader;

    private Reflections allReflections;
    private Reflections engineReflections;
    private Reflections activeModReflections;

    public ModManager() {
        engineReflections = new Reflections(
                new ConfigurationBuilder()
                        .addClassLoader(getClass().getClassLoader())
                        .addUrls(ClasspathHelper.forPackage("org.terasology", getClass().getClassLoader()))
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

    /**
     * Provides the ability to reflect over the engine and all mods, not just active mods.  This should be used sparingly,
     * and classes retrieved from it should not be instantiated and used - this uses a different classloader than the
     * rest of the system.
     *
     * @return Reflections over the engine and all available mods
     */
    public Reflections getAllReflections() {
        if (allReflections == null) {
            List<URL> urls = Lists.newArrayList();
            for (Mod mod : getMods()) {
                if (mod.isCodeMod()) {
                    urls.add(mod.getModClasspathUrl());
                }
            }
            allReflections = new Reflections(new ConfigurationBuilder()
                    .addUrls(urls)
                    .addUrls(ClasspathHelper.forPackage("org.terasology", getClass().getClassLoader()))
                    .addClassLoader(allModClassLoader)
                    .addClassLoader(getClass().getClassLoader()));
            allReflections.merge(getEngineReflections());
            for (Mod mod : getMods()) {
                if (mod.isCodeMod()) {
                    allReflections.merge(mod.getReflections());
                }
            }
        }
        return allReflections;
    }

    /**
     * Rescans for mods.  This should not be done while a game is running, as it drops the mod classloader.
     */
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
                        File assetLocation = new File(modFile, ASSETS_SUBDIRECTORY);
                        File overridesLocation = new File(modFile, OVERRIDES_SUBDIRECTORY);
                        AssetSource source = new DirectorySource(modInfo.getId(), assetLocation, overridesLocation);
                        Mod mod = new Mod(modFile, modInfo, source);
                        mods.put(modInfo.getId(), mod);
                        logger.info("Discovered mod: {} (hasCode = {})", modInfo.getDisplayName(), mod.isCodeMod());
                    } else {
                        logger.info("Discovered duplicate mod: {}, skipping", modInfo.getDisplayName());
                    }
                } catch (FileNotFoundException e) {
                    logger.warn("Failed to load mod manifest for mod at {}", modFile, e);
                } catch (JsonIOException e) {
                    logger.warn("Failed to load mod manifest for mod at {}", modFile, e);
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
                            Mod mod = new Mod(modFile, modInfo, new ArchiveSource(modInfo.getId(), modFile, ASSETS_SUBDIRECTORY, OVERRIDES_SUBDIRECTORY));
                            mods.put(modInfo.getId(), mod);
                            logger.info("Discovered mod: {} (hasCode = {})", modInfo.getDisplayName(), mod.isCodeMod());
                        } else {
                            logger.info("Discovered duplicate mod: " + modInfo.getDisplayName() + ", skipping");
                        }
                    } catch (FileNotFoundException e) {
                        logger.warn("Failed to load mod manifest for mod at {}", modFile, e);
                    } catch (JsonIOException e) {
                        logger.warn("Failed to load mod manifest for mod at {}", modFile, e);
                    }
                }
            } catch (IOException e) {
                logger.error("Invalid mod file: {}", modFile, e);
            }
        }
        List<URL> urls = Lists.newArrayList();
        for (Mod mod : getMods()) {
            if (mod.isCodeMod()) {
                urls.add(mod.getModClasspathUrl());
            }
        }
        allModClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());
        for (Mod mod : getMods()) {
            mod.setInactiveClassLoader(allModClassLoader);
        }

        activeModClassLoader = null;
        allReflections = null;
    }

    public void applyActiveMods() {
        List<URL> urls = Lists.newArrayList();
        for (Mod mod : getActiveMods()) {
            if (mod.isCodeMod()) {
                urls.add(mod.getModClasspathUrl());
            }
        }
        activeModClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());
        for (Mod mod : getActiveMods()) {
            mod.setActiveClassLoader(activeModClassLoader);
        }
        activeModReflections = new Reflections(new ConfigurationBuilder().addClassLoader(getClass().getClassLoader()).addClassLoader(activeModClassLoader));
        activeModReflections.merge(getEngineReflections());
        for (Mod mod : getActiveMods()) {
            if (mod.isCodeMod()) {
                activeModReflections.merge(mod.getReflections());
            }
        }
    }

    public Collection<Mod> getMods() {
        return mods.values();
    }

    public Mod getMod(String modName) {
        return mods.get(modName);
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
