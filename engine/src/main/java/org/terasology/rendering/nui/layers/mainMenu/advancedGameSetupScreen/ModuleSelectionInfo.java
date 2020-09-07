/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu.advancedGameSetupScreen;

import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleMetadata;

final class ModuleSelectionInfo {
    private Module latestVersion;
    private Module selectedVersion;
    private Module onlineVersion;
    private boolean explicitSelection;
    private boolean validToSelect = true;

    private ModuleSelectionInfo(Module module) {
        this.latestVersion = module;
    }

    public void setLocalVersion(Module module) {
        latestVersion = module;
    }

    public static ModuleSelectionInfo remote(Module module) {
        ModuleSelectionInfo info = new ModuleSelectionInfo(null);
        info.setOnlineVersion(module);
        return info;
    }

    public static ModuleSelectionInfo local(Module module) {
        return new ModuleSelectionInfo(module);
    }

    public ModuleMetadata getMetadata() {
        if (selectedVersion != null) {
            return selectedVersion.getMetadata();
        } else if (latestVersion != null) {
            return latestVersion.getMetadata();
        } else if (onlineVersion != null) {
            return onlineVersion.getMetadata();
        }

        return null;
    }

    public boolean isPresent() {
        return latestVersion != null;
    }

    public boolean isSelected() {
        return selectedVersion != null;
    }

    public Module getOnlineVersion() {
        return onlineVersion;
    }

    public Module getLatestVersion() {
        return latestVersion;
    }

    public void setOnlineVersion(Module onlineVersion) {
        this.onlineVersion = onlineVersion;
    }

    public void setSelectedVersion(Module selectedVersion) {
        this.selectedVersion = selectedVersion;
    }

    public boolean isExplicitSelection() {
        return explicitSelection;
    }

    public void setExplicitSelection(boolean explicitSelection) {
        this.explicitSelection = explicitSelection;
    }

    public boolean isValidToSelect() {
        return validToSelect;
    }

    public void setValidToSelect(boolean validToSelect) {
        this.validToSelect = validToSelect;
    }
}
