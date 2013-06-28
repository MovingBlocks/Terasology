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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import org.terasology.config.ModConfig;
import org.terasology.game.CoreRegistry;
import org.terasology.game.types.GameTypeUri;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.world.generator.MapGeneratorUri;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
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
    private Map<String, Short> blockIdMap = Maps.newHashMap();
    private MapGeneratorUri mapGeneratorUri = new MapGeneratorUri("core:perlin");
    private GameTypeUri gameTypeUri = null;
    private ModConfig modConfiguration = new ModConfig();

    @Deprecated
    private String[] chunkGenerators;
    @Deprecated
    private String gameType = null;

    public WorldInfo() {
    }

    public WorldInfo(String title, String seed, long time, MapGeneratorUri mapGeneratorUri, GameTypeUri gameTypeUri, ModConfig modConfig) {
        if (title != null) {
            this.title = title;
        }
        if (seed != null) {
            this.seed = seed;
        }
        if( mapGeneratorUri != null ) {
            this.mapGeneratorUri = mapGeneratorUri;
        }
        this.time = time;
        this.gameTypeUri = gameTypeUri;
        this.modConfiguration.copy(modConfig);
    }

    public static void save(File toFile, WorldInfo worldInfo) throws IOException {
        FileWriter writer = new FileWriter(toFile);
        try {
            new GsonBuilder().
                    registerTypeAdapter(MapGeneratorUri.class, new MapGeneratorUri.GsonAdapter()).
                    registerTypeAdapter(GameTypeUri.class, new GameTypeUri.GsonAdapter()).
                    setPrettyPrinting().create().toJson(worldInfo, writer);
        } finally {
            // JAVA7: better closing support
            writer.close();
        }
    }

    public static WorldInfo load(File fromFile) throws IOException {
        FileReader reader = new FileReader(fromFile);
        try {
            WorldInfo result = new GsonBuilder().
                    registerTypeAdapter(MapGeneratorUri.class, new MapGeneratorUri.GsonAdapter()).
                    registerTypeAdapter(GameTypeUri.class, new GameTypeUri.GsonAdapter()).
                    create().fromJson(reader, WorldInfo.class);
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

    public GameTypeUri getGameType() {
        if (gameType != null) {
            if (gameType.endsWith("FreeStyleType")) {
                gameTypeUri = new GameTypeUri("core:free-style");
            } else if (gameType.endsWith("SurvivalType")) {
                gameTypeUri = new GameTypeUri("core:survival");
            } else {
                throw new IllegalStateException("Unknown game type (old style): "+gameType );
            }
        }
        return gameTypeUri;
    }

    public Map<String, Short> getBlockIdMap() {
        return blockIdMap;
    }

    public void setBlockIdMap(Map<String, Short> blockIdMap) {
        this.blockIdMap = blockIdMap;
    }

    public MapGeneratorUri getMapGeneratorUri() {
        if( chunkGenerators!=null ) {
            for (String chunkGenerator : chunkGenerators) {
                if( chunkGenerator.endsWith("PerlinTerrainGenerator") ) {
                    mapGeneratorUri = new MapGeneratorUri("core:perlin");
                    break;
                } else if( chunkGenerator.endsWith("PerlinTerrainGeneratorWithSetup") ) {
                    mapGeneratorUri = new MapGeneratorUri("core:perlin-setup");
                    break;
                } else if( chunkGenerator.endsWith("FlatTerrainGenerator") ) {
                    mapGeneratorUri = new MapGeneratorUri("core:flat");
                    break;
                } else if( chunkGenerator.endsWith("MultiTerrainGenerator") ) {
                    mapGeneratorUri = new MapGeneratorUri("core:multi");
                    break;
                } else if( chunkGenerator.endsWith("BasicHMTerrainGenerator") ) {
                    mapGeneratorUri = new MapGeneratorUri("core:heightmap");
                    break;
                } else if( chunkGenerator.endsWith("PathfinderTestGenerator") ) {
                    mapGeneratorUri = new MapGeneratorUri("pathfinding:testgen");
                    break;
                }
            }
            if (mapGeneratorUri == null) {
                throw new IllegalStateException("Unknown chunk generators: "+ chunkGenerators );
            }
        }
        return mapGeneratorUri;
    }

    public void setMapGeneratorUri(MapGeneratorUri mapGeneratorUri) {
        this.mapGeneratorUri = mapGeneratorUri;
    }

    public ModConfig getModConfiguration() {
        return modConfiguration;
    }
}
