/*
 * Copyright 2015 MovingBlocks
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
import org.terasology.naming.Name;
import org.terasology.naming.Version;
import org.terasology.naming.gson.NameTypeAdapter;
import org.terasology.naming.gson.VersionTypeAdapter;
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
import java.util.Set;

/**
 * Terasology user config. Holds the various global configuration information that the user can modify. It can be saved
 * and loaded in a JSON format.
 *
 * @author Immortius
 */
public final class RootConfig {
    private static final Logger logger = LoggerFactory.getLogger(RootConfig.class);

    private SystemConfig system = new SystemConfig();
    private PlayerConfig player = new PlayerConfig();
    private PermissionConfig permission = new PermissionConfig();
    private InputConfig input = new InputConfig();
    private AudioConfig audio = new AudioConfig();
    private RenderingConfig rendering = new RenderingConfig();
    private ModuleConfig defaultModSelection = new ModuleConfig();
    private WorldGenerationConfig worldGeneration = new WorldGenerationConfig();
    private Map<SimpleUri, Map<String, JsonElement>> moduleConfigs = Maps.newHashMap();
    private NetworkConfig network = new NetworkConfig();
    private SecurityConfig security = new SecurityConfig();

    /**
     * Create a new, empty config
     */
    public RootConfig() {
    }

    public PermissionConfig getPermission() {
        return permission;
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

    public Map<SimpleUri, Map<String, JsonElement>> getModuleConfigs() {
        return moduleConfigs;
    }
}
