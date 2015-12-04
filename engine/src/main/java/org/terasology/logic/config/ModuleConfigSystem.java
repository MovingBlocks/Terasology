/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.registry.Share;

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
                logger.error("Encountered more than one Module Config for module - " + moduleName +
                        ", this is not recommended, as the property values visible are not going to be well defined.");
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
