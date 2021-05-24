// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config;

import com.google.common.collect.Lists;
import org.terasology.gestalt.naming.Name;

import java.util.List;

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
