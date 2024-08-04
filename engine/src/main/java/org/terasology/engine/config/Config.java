// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config;

import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.subsystem.Resolution;
import org.terasology.engine.utilities.gson.CaseInsensitiveEnumTypeAdapterFactory;
import org.terasology.engine.utilities.gson.InputHandler;
import org.terasology.engine.utilities.gson.ResolutionHandler;
import org.terasology.engine.utilities.gson.SetMultimapTypeAdapter;
import org.terasology.engine.utilities.gson.UriTypeAdapterFactory;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.Version;
import org.terasology.gestalt.naming.gson.NameTypeAdapter;
import org.terasology.gestalt.naming.gson.VersionTypeAdapter;
import org.terasology.input.Input;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Terasology user config. Holds the various global configuration information that the user can modify. It can be saved
 * and loaded in a JSON format.
 */
@API
public final class Config {
    public static final String PROPERTY_OVERRIDE_DEFAULT_CONFIG = "org.terasology.engine.config.default.override";
    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    private RootConfig config;

    private Context context;

    public Config(Context context) {
        this.context = context;
    }

    public PermissionConfig getPermission() {
        return config.getPermission();
    }

    public InputConfig getInput() {
        return config.getInput();
    }

    public BindsConfig getBinds() {
        return config.getBinds();
    }

    public ModuleConfig getDefaultModSelection() {
        return config.getDefaultModSelection();
    }

    public NetworkConfig getNetwork() {
        return config.getNetwork();
    }

    public RenderingConfig getRendering() {
        return config.getRendering();
    }

    public WorldGenerationConfig getWorldGeneration() {
        return config.getWorldGeneration();
    }

    public SecurityConfig getSecurity() {
        return config.getSecurity();
    }

    public NUIEditorConfig getNuiEditor() {
        return config.getNuiEditor();
    }

    public IdentityStorageServiceConfig getIdentityStorageService() {
        return config.getIdentityStorageService();
    }

    public TelemetryConfig getTelemetryConfig() {
        return config.getTelemetryConfig();
    }

    public String renderConfigAsJson(Object configObject) {
        return createGson().toJsonTree(configObject).toString();
    }

    public SelectModulesConfig getSelectModulesConfig() {
        return config.getSelectModulesConfig();
    }

    public UniverseConfig getUniverseConfig() {
        return config.getUniverseConfig();
    }

    public WebBrowserConfig getWebBrowserConfig() {
        return config.getWebBrowserConfig();
    }

    /**
     * Saves this config to the default configuration file
     */
    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(getConfigFile(), TerasologyConstants.CHARSET)) {
            createGson().toJson(config, writer);
        } catch (IOException e) {
            logger.error("Failed to save config", e);
        }
    }

    public void loadDefaults() {
        JsonObject jsonConfig = loadDefaultToJson();
        config = createGson().fromJson(jsonConfig, RootConfig.class);
    }

    public void load() {
        JsonObject jsonConfig = loadDefaultToJson();
        Optional<JsonObject> defaultsConfig = loadFileToJson(getOverrideDefaultConfigFile());
        if (defaultsConfig.isPresent()) {
            merge(jsonConfig, defaultsConfig.get());
        }
        Optional<JsonObject> userConfig = loadFileToJson(getConfigFile());
        if (userConfig.isPresent()) {
            merge(jsonConfig, userConfig.get());
        }

        config = createGson().fromJson(jsonConfig, RootConfig.class);
    }

    private Path getOverrideDefaultConfigFile() {
        return Paths.get(System.getProperty(PROPERTY_OVERRIDE_DEFAULT_CONFIG, ""));
    }

    public JsonObject loadDefaultToJson() {
        try (Reader baseReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/default.cfg")))) {
            return new JsonParser().parse(baseReader).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException("Missing default configuration file");
        }
    }

    public Optional<JsonObject> loadFileToJson(Path configPath) {
        if (Files.isRegularFile(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath, TerasologyConstants.CHARSET)) {
                JsonElement userConfig = new JsonParser().parse(reader);
                if (userConfig.isJsonObject()) {
                    return Optional.of(userConfig.getAsJsonObject());
                }
            } catch (IOException e) {
                logger.error("Failed to load config file {}, falling back on default config", configPath);
            }
        }
        return Optional.empty();
    }

    /**
     * @return The default configuration file location
     */
    private Path getConfigFile() {
        return PathManager.getInstance().getHomePath().resolve("config.cfg");
    }

    protected static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Name.class, new NameTypeAdapter())
                .registerTypeAdapter(Version.class, new VersionTypeAdapter())
                .registerTypeAdapter(BindsConfig.class, new BindsConfig.Handler())
                .registerTypeAdapter(SetMultimap.class, new SetMultimapTypeAdapter<>(Input.class))
                .registerTypeAdapter(SecurityConfig.class, new SecurityConfig.Handler())
                .registerTypeAdapter(Input.class, new InputHandler())
                .registerTypeAdapter(Resolution.class, new ResolutionHandler())
                //.registerTypeAdapter(UniverseConfig.class, new UniverseConfig.Handler())
                .registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory())
                .registerTypeAdapterFactory(new UriTypeAdapterFactory())
                .setPrettyPrinting().create();
    }

    private static Gson createGsonForModules() {
        return new GsonBuilder()
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
     * @param uri the uri to look uo
     * @return a set that contains all keys for that uri, never <code>null</code>
     */
    public Set<String> getModuleConfigKeys(SimpleUri uri) {
        Map<String, JsonElement> map = config.getModuleConfigs().get(uri);
        if (map == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(map.keySet());
    }

    /**
     * @param uri   the uri to look up
     * @param key   the look-up key
     * @param clazz the class to convert the data to
     * @return a config component for the given uri and class or <code>null</code>
     */
    public <T extends Component<T>> T getModuleConfig(SimpleUri uri, String key, Class<T> clazz) {
        Map<String, JsonElement> map = config.getModuleConfigs().get(uri);
        if (map == null) {
            return null;
        }

        JsonElement element = map.get(key);
        Gson gson = createGsonForModules();
        return gson.fromJson(element, clazz);
    }

    /**
     * @param generatorUri the generator Uri
     * @param configs      the new config params for the world generator
     */
    public void setModuleConfigs(SimpleUri generatorUri, Map<String, Component> configs) {
        Gson gson = createGsonForModules();
        Map<String, JsonElement> map = Maps.newHashMap();
        for (Map.Entry<String, Component> entry : configs.entrySet()) {
            JsonElement json = gson.toJsonTree(entry.getValue());
            map.put(entry.getKey(), json);
        }
        config.getModuleConfigs().put(generatorUri, map);
    }
}
