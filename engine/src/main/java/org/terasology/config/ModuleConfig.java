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
import org.terasology.gestalt.naming.Name;

import java.util.List;

/**
 */
public class ModuleConfig {
    private List<Name> modules = Lists.newArrayList();
    private String defaultGameplayModuleName = "";

    public ModuleConfig() {
    }

    public void copy(ModuleConfig other) {
        this.modules.clear();
        this.modules.addAll(other.modules);
        this.defaultGameplayModuleName = other.defaultGameplayModuleName;
    }

    public void addModule(Name id) {
        if (!modules.contains(id)) {
            modules.add(id);
        }
    }

    public Iterable<Name> listModules() {
        return modules;
    }

    public boolean removeModule(Name id) {
        return modules.remove(id);
    }

    public int size() {
        return modules.size();
    }

    public boolean hasModule(Name modName) {
        return modules.contains(modName);
    }

    public void clear() {
        modules.clear();
    }

    public String getDefaultGameplayModuleName() {
        return defaultGameplayModuleName;
    }

    public void setDefaultGameplayModuleName(String defaultGameplayModuleName) {
        this.defaultGameplayModuleName = defaultGameplayModuleName;
    }
}
