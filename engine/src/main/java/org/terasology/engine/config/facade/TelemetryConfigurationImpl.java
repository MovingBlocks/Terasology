// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
