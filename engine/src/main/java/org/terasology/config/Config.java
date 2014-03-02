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

package org.terasology.config;

import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
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
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.Component;
import org.terasology.input.Input;
import org.terasology.utilities.gson.CaseInsensitiveEnumTypeAdapterFactory;
import org.terasology.utilities.gson.InputHandler;
import org.terasology.utilities.gson.SetMultimapTypeAdapter;
import org.terasology.utilities.gson.UriTypeAdapterFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
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
    private PlayerConfig player = new PlayerConfig();
    private InputConfig input = new InputConfig();
    private AudioConfig audio = new AudioConfig();
    private RenderingConfig rendering = new RenderingConfig();
    private ModuleConfig defaultModSelection = new ModuleConfig();
    private WorldGenerationConfig worldGeneration = new WorldGenerationConfig();
    private Map<SimpleUri, Map<String, Component>> worldGenerationConfigs = Maps.newHashMap();
    private NetworkConfig network = new NetworkConfig();
    private SecurityConfig security = new SecurityConfig();

    /**
     * Create a new, empty config
     */
    public Config() {
    }

    /**
     * @return Input configuration (mostly binds)
     */
    public InputConfig getInput() {
        return input;
    }

    public ModuleConfig getDefaultModSelection() {
        return defaultModSelection;
    }

    public NetworkConfig getNetwork() {
        return network;
    }

    public PlayerConfig getPlayer() {
        return player;
    }

    public AudioConfig getAudio() {
        return audio;
    }

    public SystemConfig getSystem() {
        return system;
    }

    public RenderingConfig getRendering() {
        return rendering;
    }

    public WorldGenerationConfig getWorldGeneration() {
        return worldGeneration;
    }

    public SecurityConfig getSecurity() {
        return security;
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
    public static Path getConfigFile() {
        return PathManager.getInstance().getHomePath().resolve("config.cfg");
    }

    /**
     * Saves a Config to a file, in a JSON format
     *
     * @param toFile
     * @param config
     * @throws IOException
     */
    public static void save(Path toFile, Config config) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(toFile, TerasologyConstants.CHARSET)) {
            createGson().toJson(config, writer);
        }
    }

    /**
     * Loads a JSON format configuration file as a new Config
     *
     * @param fromFile
     * @return The loaded configuration
     * @throws IOException
     */
    public static Config load(Path fromFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(fromFile, TerasologyConstants.CHARSET)) {
            Gson gson = createGson();
            JsonElement baseConfig = gson.toJsonTree(new Config());
            JsonParser parser = new JsonParser();
            JsonElement config = parser.parse(reader);
            if (!config.isJsonObject()) {
                return new Config();
            } else {
                merge(baseConfig.getAsJsonObject(), config.getAsJsonObject());
                return gson.fromJson(baseConfig, Config.class);
            }
        } catch (JsonParseException e) {
            throw new IOException("Failed to load config", e);
        }
    }

    protected static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(BindsConfig.class, new BindsConfig.Handler())
                .registerTypeAdapter(SetMultimap.class, new SetMultimapTypeAdapter<>(Input.class))
                .registerTypeAdapter(SecurityConfig.class, new SecurityConfig.Handler())
                .registerTypeAdapter(Input.class, new InputHandler())
                .registerTypeAdapter(PixelFormat.class, new PixelFormatHandler())
                .registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory())
                .registerTypeAdapterFactory(new UriTypeAdapterFactory())
                .setPrettyPrinting().create();
    }

    private static void merge(JsonObject target, JsonObject from) {
        for (Map.Entry<String, JsonElement> entry : from.entrySet()) {
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

    /**
     * @return a read-only view on the config params for the world generator or <code>null</code>
     */
    public Map<String, Component> getWorldGenerationConfigs(SimpleUri generatorUri) {
        Map<String, Component> map = worldGenerationConfigs.get(generatorUri);
        if (map == null) {
            return null;
        } else {
            return Collections.unmodifiableMap(map);
        }
    }

    /**
     * @param generatorUri the generator Uri 
     * @param configs the new config params for the world generator
     */
    public void setWorldGenerationConfigs(SimpleUri generatorUri, Map<String, Component> configs) {
        this.worldGenerationConfigs.put(generatorUri, configs);
    }

    private static class PixelFormatHandler implements JsonSerializer<PixelFormat>, JsonDeserializer<PixelFormat> {

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
