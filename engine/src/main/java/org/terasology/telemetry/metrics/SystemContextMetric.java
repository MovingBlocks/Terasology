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
package org.terasology.telemetry.metrics;

import com.snowplowanalytics.snowplow.tracker.events.Unstructured;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import org.lwjgl.opengl.GL11;
import org.terasology.context.Context;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.registry.CoreRegistry;
import org.terasology.telemetry.TelemetryCategory;
import org.terasology.telemetry.TelemetryField;

import java.util.Map;

/**
 * This is a metric for system context.
 */
@TelemetryCategory(id = "systemContext",
        displayName = "${engine:menu#telemetry-system-context}"
)
public class SystemContextMetric extends Metric {

    public static final String SCHEMA_OS = "iglu:org.terasology/systemContext/jsonschema/1-0-0";
    private Context context;

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


    public SystemContextMetric() {

        osName = System.getProperty("os.name");
        osVersion = System.getProperty("os.version");
        osArchitecture = System.getProperty("os.arch");
        javaVendor = System.getProperty("java.vendor");
        javaVersion = System.getProperty("java.version");
        jvmName = System.getProperty("java.vm.name");
        jvmVersion = System.getProperty("java.vm.version");
        context = CoreRegistry.get(Context.class);
        DisplayDevice display = context.get(DisplayDevice.class);
        if (!display.isHeadless()) {
            openGLVendor = GL11.glGetString(GL11.GL_VENDOR);
            openGLVersion = GL11.glGetString(GL11.GL_VERSION);
            openGLRenderer = GL11.glGetString(GL11.GL_RENDERER);
        }
        processorNumbers = Runtime.getRuntime().availableProcessors();
        memoryMaxByte = Runtime.getRuntime().maxMemory();
    }

    @Override
    public Unstructured getUnstructuredMetric() {

        getFieldValueMap();

        SelfDescribingJson systemContextData = new SelfDescribingJson(SCHEMA_OS, metricMap);

        return Unstructured.builder()
                .eventData(systemContextData)
                .build();
    }
}
