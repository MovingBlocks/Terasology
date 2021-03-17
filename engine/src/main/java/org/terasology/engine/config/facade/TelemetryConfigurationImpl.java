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
package org.terasology.engine.config.facade;

import org.terasology.engine.config.TelemetryConfig;

import java.util.Map;

/**
 * An adapter for {@link org.terasology.engine.config.TelemetryConfig}.
 * It could be used in modules.
 */
public class TelemetryConfigurationImpl implements TelemetryConfiguration {

    private TelemetryConfig telemetryConfig;

    public TelemetryConfigurationImpl(TelemetryConfig telemetryConfig) {
        this.telemetryConfig = telemetryConfig;
    }

    public boolean isTelemetryEnabled() {
        return telemetryConfig.isTelemetryEnabled();
    }

    public boolean isErrorReportingEnabled() {
        return telemetryConfig.isErrorReportingEnabled();
    }

    public int fetchBindingSize() {
        Map<String, Boolean> bindingMap = telemetryConfig.getMetricsUserPermissionConfig().getBindingMap();
        return bindingMap.size();
    }

    public Boolean get(String telemetryField) {
        Map<String, Boolean> bindingMap = telemetryConfig.getMetricsUserPermissionConfig().getBindingMap();
        return bindingMap.get(telemetryField);
    }

    public boolean containsField(String telemetryField) {
        Map<String, Boolean> bindingMap = telemetryConfig.getMetricsUserPermissionConfig().getBindingMap();
        return bindingMap.containsKey(telemetryField);
    }
}
