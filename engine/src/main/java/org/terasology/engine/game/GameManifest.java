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
package org.terasology.engine.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.entitySystem.Component;
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
 */
public class GameManifest {
    public static final String DEFAULT_FILE_NAME = "manifest.json";

    private String title = "";
    private String seed = "";
    private long time;
    private List<String> registeredBlockFamilies = Lists.newArrayList();
    private Map<String, Short> blockIdMap = Maps.newHashMap();
    private Map<String, WorldInfo> worlds = Maps.newHashMap();
    private List<NameVersion> modules = Lists.newArrayList();
    private Map<SimpleUri, Map<String, JsonElement>> moduleConfigs = Maps.newHashMap();

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

    public Map<SimpleUri, Map<String, JsonElement>> getModuleConfigs() {
        return moduleConfigs;
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

    public static void save(Path toFile, GameManifest gameManifest) throws IOException {
        try (Writer writer = Files.newBufferedWriter(toFile, TerasologyConstants.CHARSET)) {
            createGson().toJson(gameManifest, writer);
        }
    }

    /**
     * @param generatorUri the generator Uri
     * @param configs      the new config params for the world generator
     */
    public void setModuleConfigs(SimpleUri generatorUri, Map<String, Component> configs) {
        Gson gson = createGson();
        Map<String, JsonElement> map = Maps.newHashMap();
        for (Map.Entry<String, Component> entry : configs.entrySet()) {
            JsonElement json = gson.toJsonTree(entry.getValue());
            map.put(entry.getKey(), json);
        }
        getModuleConfigs().put(generatorUri, map);
    }

    /**
     * @param uri   the uri to look up
     * @param key   the look-up key
     * @param clazz the class to convert the data to
     * @return a config component for the given uri and class or <code>null</code>
     */
    public <T extends Component> T getModuleConfig(SimpleUri uri, String key, Class<T> clazz) {
        Map<String, JsonElement> map = getModuleConfigs().get(uri);
        if (map == null) {
            return null;
        }

        JsonElement element = map.get(key);
        Gson gson = createGson();
        return gson.fromJson(element, clazz);
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

    public List<NameVersion> getModules() {
        return ImmutableList.copyOf(modules);
    }

    public void addModule(Name id, Version version) {
        modules.add(new NameVersion(id, version));
    }

}
