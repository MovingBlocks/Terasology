/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.config;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.terasology.engine.TerasologyConstants;
import org.terasology.world.internal.WorldInfo;

import java.lang.reflect.Type;
import java.util.List;

public class UniverseConfig {
    private List<WorldInfo> worlds = Lists.newArrayList();
    private WorldInfo spawnWorld;
    private String universeSeed;

    public UniverseConfig() {
        worlds.clear();
    }

    public void addWorldManager(WorldInfo worldManager) {
        if (worldManager.getTitle().equals(TerasologyConstants.MAIN_WORLD)) {
            worlds.clear();
            this.spawnWorld = worldManager;
        }
        worlds.add(worldManager);
    }

    public void setSpawnWorld(WorldInfo targetWorld) {
        spawnWorld = targetWorld;
    }

    public void setUniverseSeed(String seed) {
        universeSeed = seed;
    }

//    static class Handler implements JsonSerializer<UniverseConfig>, JsonDeserializer<UniverseConfig> {
//
//        private static final String WORLDS = "worlds";
//        private static final String SPAWN_WORLD = "spawnWorld";
//        private static final String UNIVERSE_SEED = "universeSeed";
//
//        @Override
//        public UniverseConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//            UniverseConfig result = new UniverseConfig();
//            JsonObject jsonObject = json.getAsJsonObject();
//            if (jsonObject.has(WORLDS)) {
//                JsonArray worldsArray = jsonObject.getAsJsonArray(WORLDS);
//                for (JsonElement jsonElement : worldsArray) {
//                    WorldInfo worldInfo = context.deserialize(jsonElement, WorldInfo.class);
//                    result.addWorldManager(worldInfo);
//                }
//            }
//            if (jsonObject.has(SPAWN_WORLD)) {
//                result.spawnWorld = context.deserialize(jsonObject.getAsJsonObject(SPAWN_WORLD), WorldInfo.class);
//            }
//            if (jsonObject.has(UNIVERSE_SEED)) {
//                result.universeSeed = context.deserialize(jsonObject.getAsJsonObject(UNIVERSE_SEED), String.class);
//            }
//            return result;
//        }
//
//        @Override
//        public JsonElement serialize(UniverseConfig src, Type typeOfBindsConfig, JsonSerializationContext context) {
//            JsonObject result = new JsonObject();
//            result.add(WORLDS, context.serialize(src.worlds));
//            result.add(SPAWN_WORLD, context.serialize(src.spawnWorld));
//            result.add(UNIVERSE_SEED, context.serialize(src.universeSeed));
//            return result;
//        }
//    }
}
