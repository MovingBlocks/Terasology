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

package org.terasology.engine.config;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import org.terasology.engine.core.SimpleUri;

import java.util.Map;

/**
 * Terasology user config. Holds the various global configuration information that the user can modify. It can be saved
 * and loaded in a JSON format.
 */
public final class RootConfig {
    private SystemConfig system = new SystemConfig();
    private PlayerConfig player = new PlayerConfig();
    private PermissionConfig permission = new PermissionConfig();
    private InputConfig input = new InputConfig();
    private BindsConfig binds = new BindsConfig();
    private RenderingConfig rendering = new RenderingConfig();
    private ModuleConfig defaultModSelection = new ModuleConfig();
    private WorldGenerationConfig worldGeneration = new WorldGenerationConfig();
    private Map<SimpleUri, Map<String, JsonElement>> moduleConfigs = Maps.newHashMap();
    private NetworkConfig network = new NetworkConfig();
    private SecurityConfig security = new SecurityConfig();
    private NUIEditorConfig nuiEditor = new NUIEditorConfig();
    private SelectModulesConfig selectModulesConfig = new SelectModulesConfig();
    private IdentityStorageServiceConfig identityStorageService = new IdentityStorageServiceConfig();
    private TelemetryConfig telemetryConfig = new TelemetryConfig();
    private UniverseConfig universeConfig = new UniverseConfig();
    private WebBrowserConfig webBrowserConfig = new WebBrowserConfig();

    /**
     * Create a new, empty config
     */
    public RootConfig() {
    }

    public PermissionConfig getPermission() {
        return permission;
    }

    public InputConfig getInput() {
        return input;
    }

    public BindsConfig getBinds() {
        return binds;
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

    public SelectModulesConfig getSelectModulesConfig() {
        return selectModulesConfig;
    }

    public NUIEditorConfig getNuiEditor() {
        return nuiEditor;
    }

    public IdentityStorageServiceConfig getIdentityStorageService() {
        return identityStorageService;
    }

    public TelemetryConfig getTelemetryConfig() {
        return telemetryConfig;
    }

    public Map<SimpleUri, Map<String, JsonElement>> getModuleConfigs() {
        return moduleConfigs;
    }

    public UniverseConfig getUniverseConfig() {
        return universeConfig;
    }

    public WebBrowserConfig getWebBrowserConfig() {
        return webBrowserConfig;
    }
}
