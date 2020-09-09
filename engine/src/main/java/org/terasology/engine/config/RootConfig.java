// Copyright 2020 The Terasology Foundation
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
    private final SystemConfig system = new SystemConfig();
    private final PlayerConfig player = new PlayerConfig();
    private final PermissionConfig permission = new PermissionConfig();
    private final InputConfig input = new InputConfig();
    private final BindsConfig binds = new BindsConfig();
    private final RenderingConfig rendering = new RenderingConfig();
    private final ModuleConfig defaultModSelection = new ModuleConfig();
    private final WorldGenerationConfig worldGeneration = new WorldGenerationConfig();
    private final Map<SimpleUri, Map<String, JsonElement>> moduleConfigs = Maps.newHashMap();
    private final NetworkConfig network = new NetworkConfig();
    private final SecurityConfig security = new SecurityConfig();
    private final NUIEditorConfig nuiEditor = new NUIEditorConfig();
    private final SelectModulesConfig selectModulesConfig = new SelectModulesConfig();
    private final IdentityStorageServiceConfig identityStorageService = new IdentityStorageServiceConfig();
    private final TelemetryConfig telemetryConfig = new TelemetryConfig();
    private final UniverseConfig universeConfig = new UniverseConfig();
    private final WebBrowserConfig webBrowserConfig = new WebBrowserConfig();

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
