// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.utilities.gson.CaseInsensitiveEnumTypeAdapterFactory;
import org.terasology.engine.utilities.gson.UriTypeAdapterFactory;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.NameVersion;
import org.terasology.gestalt.naming.Version;
import org.terasology.gestalt.naming.gson.NameTypeAdapter;
import org.terasology.gestalt.naming.gson.VersionTypeAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class GameManifest {
    public static final String DEFAULT_FILE_NAME = "manifest.json";

    private String title = "";
    private String seed = "";
    private long time;
    private List<String> registeredBlockFamilies = Lists.newArrayList();
    private Map<String, Short> blockIdMap = Maps.newHashMap();
    private final Map<String, WorldInfo> worlds = Maps.newHashMap();
    private final List<NameVersion> modules = Lists.newArrayList();

    public GameManifest() {
    }

    public GameManifest(String title, String seed, long time) {
        if (title != null) {
            this.title = title;
        }
        if (seed != null) {
            this.seed = seed;
        }
        this.time = time;
    }

    public static void save(Path toFile, GameManifest gameManifest) throws IOException {
        try (Writer writer = Files.newBufferedWriter(toFile, TerasologyConstants.CHARSET)) {
            createGson().toJson(gameManifest, writer);
        }
    }

    public static GameManifest load(Path filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath, TerasologyConstants.CHARSET)) {
            return createGson().fromJson(reader, GameManifest.class);
        }
    }

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory())
                .registerTypeAdapterFactory(new UriTypeAdapterFactory())
                .registerTypeAdapter(Version.class, new VersionTypeAdapter())
                .registerTypeAdapter(Name.class, new NameTypeAdapter())
                .setPrettyPrinting()
                .create();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public List<String> getRegisteredBlockFamilies() {
        return registeredBlockFamilies;
    }

    public void setRegisteredBlockFamilies(List<String> registeredBlockFamilies) {
        this.registeredBlockFamilies = registeredBlockFamilies;
    }

    public Map<String, Short> getBlockIdMap() {
        return blockIdMap;
    }

    public void setBlockIdMap(Map<String, Short> blockIdMap) {
        this.blockIdMap = blockIdMap;
    }

    public WorldInfo getWorldInfo(String name) {
        return worlds.get(name);
    }

    public Map<String, WorldInfo> getWorldInfoMap() {
        return worlds;
    }

    public void addWorld(WorldInfo worldInfo) {
        this.worlds.put(worldInfo.getTitle(), worldInfo);
    }

    public Iterable<WorldInfo> getWorlds() {
        return this.worlds.values();
    }

    public List<NameVersion> getModules() {
        return ImmutableList.copyOf(modules);
    }

    public void addModule(Name id, Version version) {
        modules.add(new NameVersion(id, version));
    }

}
