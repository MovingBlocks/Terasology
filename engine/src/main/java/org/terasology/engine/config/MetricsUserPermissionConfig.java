// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config;

import java.util.HashMap;
import java.util.Map;

/**
 * This config is used in Telemetry.
 * It gives the user more options such as sending one part of the fields but not the other part.
 */
public class MetricsUserPermissionConfig {

    private Map<String, Boolean> bindingMap = new HashMap<>();

    public Map<String, Boolean> getBindingMap() {
        return bindingMap;
    }

    public void setBindingMap(Map<String, Boolean> bindingMap) {
        this.bindingMap = bindingMap;
    }
}
