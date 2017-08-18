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
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.registry.CoreRegistry;
import org.terasology.telemetry.TelemetryField;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An abstract class for a telemetry metric.
 * A new metric should extends this class, with annotation {@link org.terasology.telemetry.TelemetryCategory} and all metric fields with annotation {@link org.terasology.telemetry.TelemetryField}.
 * An example is {@link org.terasology.telemetry.metrics.SystemContextMetric}.
 */
public abstract class Metric {

    private static final Logger logger = LoggerFactory.getLogger(Metric.class);

    protected Map metricMap;

    /**
     * Generates a snowplow unstructured event that the snowplow tracker can track.
     * @return an snowplow unstructured event.
     */
    public abstract Unstructured getUnstructuredMetric();

    /**
     * Fetches all TelemetryFields and create a map associating field's name (key) to field's value (value).
     * @return a map with key (field's name) and value (field's value).
     */
    public Map<String, ?> getFieldValueMap() {

        metricMap = new HashMap();
        Set<Field> fields = ReflectionUtils.getFields(this.getClass(), ReflectionUtils.withAnnotation(TelemetryField.class));

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                metricMap.put(field.getName(), field.get(this));
            } catch (IllegalAccessException e) {
                logger.error("The field is not inaccessible: ", e);
            }
        }

        return metricMap;
    }

    /**
     * Filter the metric map by the binding map.
     * If the user doesn't want the field to be sent, its value will be covered by "Disabled Field".
     * @param bindingMap the binding map.
     * @return a new metric map that covers the field that the user doesn't want to send by "Disabled Field".
     */
    protected Map<String, ?> filterMetricMap(Map<String, Boolean> bindingMap) {

        Context context = CoreRegistry.get(Context.class);
        DisplayDevice display = context.get(DisplayDevice.class);
        if (display.isHeadless()) {
            return metricMap;
        }
        Map metricMapAfterPermission = new HashMap<>();
        for (Object key : metricMap.keySet()) {
            String fieldName = key.toString();
            if (bindingMap.containsKey(fieldName)) {
                if (bindingMap.get(fieldName)) {
                    metricMapAfterPermission.put(fieldName, metricMap.get(fieldName));
                } else {
                    metricMapAfterPermission.put(fieldName, "Disabled Field");
                }
            }
        }

        return metricMapAfterPermission;
    }
}
