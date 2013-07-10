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
package org.terasology.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import org.terasology.config.ModConfig;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.TerasologyConstants;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.world.WorldInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Immortius
 */
public class GameManifest {
    public static final String DEFAULT_FILE_NAME = "GameManifest.json";

    private String title = "";
    private String seed = "";
    private long time = 0;
    private List<String> registeredBlockFamilies = Lists.newArrayList();
    private Map<String, Byte> blockIdMap = Maps.newHashMap();
    private Map<String, WorldInfo> worldInfo = Maps.newHashMap();
    private ModConfig modConfiguration = new ModConfig();

    public GameManifest() {
    }

    public GameManifest(String title, String seed, long time, ModConfig modConfiguration) {
        if (title != null) {
            this.title = title;
        }
        if (seed != null) {
            this.seed = seed;
        }
        this.time = time;
        this.modConfiguration.copy(modConfiguration);
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

    public Map<String, Byte> getBlockIdMap() {
        return blockIdMap;
    }

    public void setBlockIdMap(Map<String, Byte> blockIdMap) {
        this.blockIdMap = blockIdMap;
    }

    public ModConfig getModConfiguration() {
        return modConfiguration;
    }

    public WorldInfo getWorldInfo(String name) {
        return worldInfo.get(name);
    }

    public void addWorldInfo(WorldInfo worldInfo) {
        this.worldInfo.put(worldInfo.getTitle(), worldInfo);
    }

    public Iterable<WorldInfo> getAllWorldInfo() {
        return this.worldInfo.values();
    }

    public static void save(Path toFile, GameManifest gameManifest) throws IOException {
        try (Writer writer = Files.newBufferedWriter(toFile, TerasologyConstants.CHARSET)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(gameManifest, writer);
        }
    }

    public static GameManifest load(Path filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath, TerasologyConstants.CHARSET)) {
            GameManifest result = new GsonBuilder().create().fromJson(reader, GameManifest.class);
            if (result.modConfiguration.size() == 0) {
                for (Mod mod : CoreRegistry.get(ModManager.class).getMods()) {
                    result.modConfiguration.addMod(mod.getModInfo().getId());
                }
            }
            return result;
        }
    }

}
