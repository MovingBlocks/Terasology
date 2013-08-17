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

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Immortius
 */
public class ModuleConfig {
    private List<String> mods = Lists.newArrayList();

    public ModuleConfig() {
    }

    public void copy(ModuleConfig other) {
        this.mods.clear();
        this.mods.addAll(other.mods);
    }

    public void addMod(String mod) {
        if (!mods.contains(mod)) {
            mods.add(mod);
        }
    }

    public Iterable<String> listMods() {
        return mods;
    }

    public boolean removeMod(String mod) {
        return mods.remove(mod);
    }

    public int size() {
        return mods.size();
    }

    public boolean hasMod(String modName) {
        return mods.contains(modName);
    }
}
