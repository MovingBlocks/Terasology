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

package org.terasology.world;

import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import org.terasology.config.ModConfig;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Summary information on a world.
 *
 * @author Immortius
 */
// TODO: Store this stuff in a protobuf?
public class WorldInfo {
    public static final String DEFAULT_FILE_NAME = "WorldManifest.json";

    private String title = "";
    private String seed = "";
    private long time = 0;
    private Map<String, Byte> blockIdMap = Maps.newHashMap();
    private String[] chunkGenerators = new String[]{};
    private String gameType = null;
    private ModConfig modConfiguration = new ModConfig();

    public WorldInfo() {
    }

    public WorldInfo(String title, String seed, long time, String[] chunkGenerators, String gameType, ModConfig modConfig) {
        if (title != null) {
            this.title = title;
        }
        if (seed != null) {
            this.seed = seed;
        }
        if (chunkGenerators != null) {
            this.chunkGenerators = chunkGenerators;
        }
        this.time = time;
        this.gameType = gameType;
        this.modConfiguration.copy(modConfig);
    }

    public static void save(File toFile, WorldInfo worldInfo) throws IOException {
        FileWriter writer = new FileWriter(toFile);
        try {
            new GsonBuilder().setPrettyPrinting().create().toJson(worldInfo, writer);
        } finally {
            // JAVA7: better closing support
            writer.close();
        }
    }

    public static WorldInfo load(File fromFile) throws IOException {
        FileReader reader = new FileReader(fromFile);
        try {
            WorldInfo result = new GsonBuilder().create().fromJson(reader, WorldInfo.class);
            if (result.modConfiguration.size() == 0) {
                for (Mod mod : CoreRegistry.get(ModManager.class).getMods()) {
                    result.modConfiguration.addMod(mod.getModInfo().getId());
                }
            }
            return result;
        } finally {
            reader.close();
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title != null) {
            this.title = title;
        }
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        if (seed != null) {
            this.seed = seed;
        }
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getGameType() {
        return gameType;
    }

    public Map<String, Byte> getBlockIdMap() {
        return blockIdMap;
    }

    public void setBlockIdMap(Map<String, Byte> blockIdMap) {
        this.blockIdMap = blockIdMap;
    }

    public String[] getChunkGenerators() {
        return chunkGenerators;
    }

    public void setChunkGenerators(String[] chunkGenerators) {
        this.chunkGenerators = chunkGenerators;
    }

    public ModConfig getModConfiguration() {
        return modConfiguration;
    }
}
