// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

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
