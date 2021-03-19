/*
 * Copyright 2017 MovingBlocks
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
