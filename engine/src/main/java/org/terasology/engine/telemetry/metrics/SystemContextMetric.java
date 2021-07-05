// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.telemetry.metrics;

import com.snowplowanalytics.snowplow.tracker.events.Unstructured;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.core.subsystem.DisplayDeviceInfo;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.telemetry.TelemetryCategory;
import org.terasology.engine.telemetry.TelemetryField;

import java.util.Map;
import java.util.Optional;

/**
 * This is a metric for system context.
 */
@TelemetryCategory(id = "systemContext",
        displayName = "${engine:menu#telemetry-system-context}",
        isOneMapMetric = false
)
public final class SystemContextMetric extends Metric {

    public static final String SCHEMA_OS = "iglu:org.terasology/systemContext/jsonschema/1-0-0";
    private Context contextInCoreRegistry;
    private Map<String, Boolean> bindingMap;

    @TelemetryField
    private String osName;

    @TelemetryField
    private String osVersion;

    @TelemetryField
    private String osArchitecture;

    @TelemetryField
    private String javaVendor;

    @TelemetryField
    private String javaVersion;

    @TelemetryField
    private String jvmName;

    @TelemetryField
    private String jvmVersion;

    @TelemetryField
    private String openGLVendor;

    @TelemetryField
    private String openGLVersion;

    @TelemetryField
    private String openGLRenderer;

    @TelemetryField
    private int processorNumbers;

    @TelemetryField
    private long memoryMaxByte;

    public SystemContextMetric(Context context) {
        bindingMap = context.get(Config.class).getTelemetryConfig().getMetricsUserPermissionConfig().getBindingMap();

        osName = System.getProperty("os.name");
        osVersion = System.getProperty("os.version");
        osArchitecture = System.getProperty("os.arch");
        javaVendor = System.getProperty("java.vendor");
        javaVersion = System.getProperty("java.version");
        jvmName = System.getProperty("java.vm.name");
        jvmVersion = System.getProperty("java.vm.version");
        contextInCoreRegistry = CoreRegistry.get(Context.class);
        DisplayDevice display = contextInCoreRegistry.get(DisplayDevice.class);
        DisplayDeviceInfo displayDeviceInfo = display.getInfo();
        openGLVendor = displayDeviceInfo.getOpenGlVendor();
        openGLVersion = displayDeviceInfo.getOpenGLVersion();
        openGLRenderer = displayDeviceInfo.getOpenGLRenderer();
        processorNumbers = Runtime.getRuntime().availableProcessors();
        memoryMaxByte = Runtime.getRuntime().maxMemory();
    }

    @Override
    public Optional<Unstructured> getUnstructuredMetric() {
        createTelemetryFieldToValue();
        Map<String, Object> filteredMetricMap = filterMetricMap(bindingMap);
        return getUnstructuredMetric(SCHEMA_OS, filteredMetricMap);
    }
}
