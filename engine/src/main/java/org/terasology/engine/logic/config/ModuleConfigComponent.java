// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.config;

import com.google.common.collect.Maps;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Map;

public class ModuleConfigComponent implements Component<ModuleConfigComponent> {
    public String moduleName;
    public Map<String, String> properties;

    @Override
    public void copy(ModuleConfigComponent other) {
        this.moduleName = other.moduleName;
        this.properties = Maps.newHashMap(other.properties);
    }
}
