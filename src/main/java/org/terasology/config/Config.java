/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.lwjgl.opengl.PixelFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.input.Input;
import org.terasology.logic.manager.PathManager;
import org.terasology.utilities.gson.InputHandler;
import org.terasology.utilities.gson.MultimapHandler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Terasology user config. Holds the various global configuration information that the user can modify. It can be saved
 * and loaded in a JSON format.
 *
 * @author Immortius
 */
public final class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    private SystemConfig system = new SystemConfig();
    private InputConfig input = new InputConfig();
    private AudioConfig audio = new AudioConfig();
    private RenderingConfig rendering = new RenderingConfig();
    private ModConfig defaultModSelection = new ModConfig();
    private WorldGenerationConfig worldGeneration = new WorldGenerationConfig();
    private AdvancedConfig advanced = AdvancedConfig.createDefault();

    /**
     * Create a new, empty config
     */
    public Config() {
    }

    /**
     * @return Input configuration (mostly binds)
     */
    public InputConfig getInputConfig() {
        return input;
    }

    public ModConfig getDefaultModSelection() {
        return defaultModSelection;
    }
    
    public AdvancedConfig getAdvanced() {
        return advanced;
    }

    public AudioConfig getAudio() {
        return audio;
    }

    public SystemConfig getSystem() {
        return system;
    }

    public InputConfig getInput() {
        return input;
    }

    public RenderingConfig getRendering() {
        return rendering;
    }

    public WorldGenerationConfig getWorldGeneration() {
        return worldGeneration;
    }

    /**
     * Saves this config to the default configuration file
     */
    public void save() {
        try {
            save(getConfigFile(), this);
        } catch (IOException e) {
            logger.error("Failed to save config", e);
        }
    }

    /**
     * @return The default configuration file location
     */
    public static File getConfigFile() {
        return new File(PathManager.getInstance().getDataPath(), "config.cfg");
    }

    /**
     * Saves a Config to a file, in a JSON format
     * @param toFile
     * @param config
     * @throws IOException
     */
    public static void save(File toFile, Config config) throws IOException {
        FileWriter writer = new FileWriter(toFile);
        try {
            new GsonBuilder()
                    .registerTypeAdapter(BindsConfig.class, new BindsConfig.Handler())
                    .registerTypeAdapter(Multimap.class, new MultimapHandler<Input>(Input.class))
                    .registerTypeAdapter(Input.class, new InputHandler())
                    .registerTypeAdapter(AdvancedConfig.class, new AdvancedConfig.Handler())
                    .registerTypeAdapter(PixelFormat.class, new PixelFormatHandler())
                    .setPrettyPrinting().create().toJson(config, writer);
        } finally {
            // JAVA7: better closing support
            writer.close();
        }
    }

    /**
     * Loads a JSON format configuration file as a new Config
     * @param fromFile
     * @return The loaded configuration
     * @throws IOException
     */
    public static Config load(File fromFile) throws IOException {
        FileReader reader = new FileReader(fromFile);
        try {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(BindsConfig.class, new BindsConfig.Handler())
                    .registerTypeAdapter(Multimap.class, new MultimapHandler<Input>(Input.class))
                    .registerTypeAdapter(Input.class, new InputHandler())
                    .registerTypeAdapter(AdvancedConfig.class, new AdvancedConfig.Handler())
                    .registerTypeAdapter(PixelFormat.class, new PixelFormatHandler())
                    .create();
            JsonElement baseConfig = gson.toJsonTree(new Config());
            JsonParser parser = new JsonParser();
            JsonElement config = parser.parse(reader);
            if (!config.isJsonObject()) {
                return new Config();
            } else {
                merge(baseConfig.getAsJsonObject(), config.getAsJsonObject());
                return gson.fromJson(baseConfig, Config.class);
            }
        } finally {
            reader.close();
        }
    }

    private static void merge(JsonObject target, JsonObject from) {
        for(Map.Entry<String, JsonElement> entry : from.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                if (target.has(entry.getKey()) && target.get(entry.getKey()).isJsonObject()) {
                    merge(target.get(entry.getKey()).getAsJsonObject(), entry.getValue().getAsJsonObject());
                } else {
                    target.remove(entry.getKey());
                    target.add(entry.getKey(), entry.getValue());
                }
            } else {
                target.remove(entry.getKey());
                target.add(entry.getKey(), entry.getValue());
            }
        }
    }

    private static class PixelFormatHandler  implements JsonSerializer<PixelFormat>, JsonDeserializer<PixelFormat> {

        @Override
        public PixelFormat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
                return new PixelFormat().withDepthBits(json.getAsInt());
            }
            return new PixelFormat().withDepthBits(24);
        }

        @Override
        public JsonElement serialize(PixelFormat src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getDepthBits());
        }
    }
}
