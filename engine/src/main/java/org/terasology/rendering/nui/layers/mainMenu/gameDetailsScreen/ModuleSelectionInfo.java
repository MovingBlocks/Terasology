// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.gameDetailsScreen;

import org.terasology.module.Module;
import org.terasology.module.ModuleMetadata;

/**
 * Contains objects that used by {@link GameDetailsScreen} to select a module on tab Modules.
 */
public final class ModuleSelectionInfo {

    private Module module;
    private boolean strictVersion;
    private boolean latestVersion;

    private String unavailableModuleName;
    private String unavailableModuleVersion;

    private ModuleSelectionInfo() {
        this.strictVersion = false;
        this.latestVersion = false;
    }

    protected static ModuleSelectionInfo strictVersion(final Module module) {
        return new ModuleSelectionInfo().setStrictVersion(module);
    }

    protected static ModuleSelectionInfo latestVersion(final Module module) {
        return new ModuleSelectionInfo().setLatestVersion(module);
    }

    protected static ModuleSelectionInfo unavailableVersion(final String moduleName, final String moduleVersion) {
        ModuleSelectionInfo unavailableModuleInfo = new ModuleSelectionInfo();
        unavailableModuleInfo.setUnavailableModuleName(moduleName);
        unavailableModuleInfo.setUnavailableModuleVersion(moduleVersion);
        return unavailableModuleInfo;
    }

    public ModuleMetadata getMetadata() {
        if (!isUnavailableVersion()) {
            return module.getMetadata();
        }
        return null;
    }

    protected boolean isStrictVersion() {
        return strictVersion;
    }

    protected boolean isLatestVersion() {
        return latestVersion;
    }

    protected boolean isUnavailableVersion() {
        return !isStrictVersion() && !isLatestVersion();
    }

    protected String getUnavailableModuleName() {
        return unavailableModuleName;
    }

    protected void setUnavailableModuleName(final String unavailableModuleName) {
        this.unavailableModuleName = unavailableModuleName;
    }

    protected String getUnavailableModuleVersion() {
        return unavailableModuleVersion;
    }

    protected void setUnavailableModuleVersion(final String unavailableModuleVersion) {
        this.unavailableModuleVersion = unavailableModuleVersion;
    }

    private ModuleSelectionInfo setStrictVersion(final Module strictVersion) {
        this.module = strictVersion;
        this.strictVersion = true;
        return this;
    }

    private ModuleSelectionInfo setLatestVersion(final Module latestVersion) {
        this.module = latestVersion;
        this.latestVersion = true;
        return this;
    }

    protected Module getModule() {
        return module;
    }
}
