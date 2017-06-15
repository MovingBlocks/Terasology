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

    public abstract Unstructured getMetric();

    public Map<String,Object> generateMetricMap() {

        Map<String, Object> metricMap = new HashMap<String,Object>();
        Set<Field> fields = ReflectionUtils.getFields(this.getClass(),ReflectionUtils.withAnnotation(TelemetryField.class));

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                metricMap.put(field.getName(), field.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return metricMap;
    }

    public abstract Map getMap();
}
