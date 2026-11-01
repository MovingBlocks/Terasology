// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.config;

import org.terasology.context.annotation.API;

@API
public interface ModuleConfigManager {
    String getStringVariable(String moduleName, String propertyName, String defaultValue);
    int getIntVariable(String moduleName, String propertyName, int defaultValue);
    float getFloatVariable(String moduleName, String propertyName, float defaultValue);
    boolean getBooleanVariable(String moduleName, String propertyName, boolean defaultValue);
}
