// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RegisterSystem
@Share(ModuleConfigManager.class)
public class ModuleConfigSystem extends BaseComponentSystem implements ModuleConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ModuleConfigSystem.class);

    @In
    private PrefabManager prefabManager;

    private Map<String, Map<String, String>> propertiesPerModule = new HashMap<>();

    @Override
    public void preBegin() {
        for (Prefab prefab : prefabManager.listPrefabs(ModuleConfigComponent.class)) {
            ModuleConfigComponent moduleConfig = prefab.getComponent(ModuleConfigComponent.class);
            String moduleName = moduleConfig.moduleName;
            Map<String, String> properties;
            if (propertiesPerModule.containsKey(moduleName)) {
                logger.error("Encountered more than one Module Config for module - {}, this is not recommended, " +
                        "as the property values visible are not going to be well defined.", moduleName);
                properties = propertiesPerModule.get(moduleName);
            } else {
                properties = new HashMap<>();
                propertiesPerModule.put(moduleName, properties);
            }
            properties.putAll(moduleConfig.properties);
        }
    }

    @Override
    public String getStringVariable(String moduleName, String propertyName, String defaultValue) {
        return getVariable(moduleName, propertyName, s -> s, defaultValue);
    }

    @Override
    public int getIntVariable(String moduleName, String propertyName, int defaultValue) {
        return getVariable(moduleName, propertyName, Integer::parseInt, defaultValue);
    }

    @Override
    public float getFloatVariable(String moduleName, String propertyName, float defaultValue) {
        return getVariable(moduleName, propertyName, Float::parseFloat, defaultValue);
    }

    @Override
    public boolean getBooleanVariable(String moduleName, String propertyName, boolean defaultValue) {
        return getVariable(moduleName, propertyName, Boolean::parseBoolean, defaultValue);
    }

    private <T> T getVariable(String moduleName, String propertyName, Function<String, T> extractFunction, T defaultValue) {
        Map<String, String> moduleProperties = propertiesPerModule.get(moduleName);
        if (moduleProperties == null) {
            return defaultValue;
        }
        String propertyValue = moduleProperties.get(propertyName);
        if (propertyValue == null) {
            return defaultValue;
        }
        return extractFunction.apply(propertyValue);
    }
}
