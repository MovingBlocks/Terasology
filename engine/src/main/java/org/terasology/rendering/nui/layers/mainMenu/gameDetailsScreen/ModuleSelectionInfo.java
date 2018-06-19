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
package org.terasology.rendering.nui.layers.mainMenu.gameDetailsScreen;

import org.terasology.module.Module;
import org.terasology.module.ModuleMetadata;

/**
 * TODO: add description
 */
public final class ModuleSelectionInfo {

    private Module strictVersion;
    private Module latestVersion;

    private ModuleSelectionInfo() {
    }

    public static ModuleSelectionInfo strictVersion(Module module) {
        return new ModuleSelectionInfo().setStrictVersion(module);
    }

    public static ModuleSelectionInfo latestVersion(Module module) {
        return new ModuleSelectionInfo().setLatestVersion(module);
    }

    public static ModuleSelectionInfo unavailableVersion() {
        return new ModuleSelectionInfo();
    }

    public ModuleMetadata getMetadata() {
        if (isStrictVersion()) {
            return strictVersion.getMetadata();
        } else if (isLatestVersion()) {
            return latestVersion.getMetadata();
        }
        return null;
    }

    public boolean isStrictVersion() {
        return strictVersion != null;
    }

    public boolean isLatestVersion() {
        return latestVersion != null;
    }

    public boolean isUnavailableVersion() {
        return !isStrictVersion() && !isLatestVersion();
    }

    private ModuleSelectionInfo setStrictVersion(Module strictVersion) {
        this.strictVersion = strictVersion;
        return this;
    }

    private ModuleSelectionInfo setLatestVersion(Module latestVersion) {
        this.latestVersion = latestVersion;
        return this;
    }

}
