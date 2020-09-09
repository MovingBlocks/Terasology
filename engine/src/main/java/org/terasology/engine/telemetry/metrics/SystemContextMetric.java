// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.telemetry.metrics;

import com.snowplowanalytics.snowplow.tracker.events.Unstructured;
import org.lwjgl.opengl.GL11;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.DisplayDevice;
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
    private final Context contextInCoreRegistry;
    private final Map<String, Boolean> bindingMap;

    @TelemetryField
    private final String osName;

    @TelemetryField
    private final String osVersion;

    @TelemetryField
    private final String osArchitecture;

    @TelemetryField
    private final String javaVendor;

    @TelemetryField
    private final String javaVersion;

    @TelemetryField
    private final String jvmName;

    @TelemetryField
    private final String jvmVersion;

    @TelemetryField
    private final String openGLVendor;

    @TelemetryField
    private final String openGLVersion;

    @TelemetryField
    private final String openGLRenderer;

    @TelemetryField
    private final int processorNumbers;

    @TelemetryField
    private final long memoryMaxByte;

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
        if (!display.isHeadless()) {
            openGLVendor = GL11.glGetString(GL11.GL_VENDOR);
            openGLVersion = GL11.glGetString(GL11.GL_VERSION);
            openGLRenderer = GL11.glGetString(GL11.GL_RENDERER);
        } else {
            openGLVendor = "headless";
            openGLVersion = "headless";
            openGLRenderer = "headless";
        }
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
