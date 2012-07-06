/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.logic.mod;

import org.terasology.asset.AssetSource;
import org.terasology.logic.manager.AssetManager;

import java.io.File;

/**
 * @author Immortius
 */
public class Mod {
    private ModInfo modInfo;
    private File modRoot;
    private AssetSource modSource;
    private boolean enabled;

    public Mod(File modRoot, ModInfo info, AssetSource modSource) {
        this.modInfo = info;
        this.modRoot = modRoot;
        this.modSource = modSource;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                AssetManager.getInstance().addAssetSource(modSource);
            } else {
                AssetManager.getInstance().removeAssetSource(modSource);
            }
        }
    }

    public File getModRoot() {
        return modRoot;
    }

    public ModInfo getModInfo() {
        return modInfo;
    }
}
