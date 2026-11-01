// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.telemetry.metrics;

import com.snowplowanalytics.snowplow.tracker.events.Unstructured;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.facade.TelemetryConfiguration;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.context.annotation.API;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.telemetry.Metrics;
import org.terasology.engine.telemetry.TelemetryCategory;
import org.terasology.engine.telemetry.TelemetryField;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * An abstract class for a telemetry metric.
 * A new metric should extends this class, with annotation {@link org.terasology.engine.telemetry.TelemetryCategory}.
 * All metric fields should be annotated {@link org.terasology.engine.telemetry.TelemetryField}.
 * An example is {@link org.terasology.engine.telemetry.metrics.SystemContextMetric}.
 * The metric will be instantiated automatically in {@link org.terasology.engine.telemetry.Metrics}
 * By convention, a new Metric can have only one constructor and constructor will need no arguments
 * or only {@link org.terasology.engine.context.Context}.
 * If a Metric Constructor needs some specific arguments other than {@link org.terasology.engine.context.Context},
 * it should be instantiated and added to {@link org.terasology.engine.telemetry.Metrics} manually.
 */
@API
public abstract class Metric {

    private static final Logger logger = LoggerFactory.getLogger(Metric.class);

    /**
     * The map contains telemetry field name as key and field value as value.
     * If the telemetry field is a map, then this map equals to that.
     */
    protected Map<String, Object> telemetryFieldToValue = new HashMap<>();

    /**
     * Generates a snowplow unstructured event that the snowplow tracker can track.
     * @return an snowplow unstructured event.
     */
    public abstract Optional<Unstructured> getUnstructuredMetric();


    /**
     * Generates a snowplow unstructured event.
     * This method helps to implement abstract getUnstructuredMetric method.
     * You can find example in {@link org.terasology.engine.telemetry.metrics.ModulesMetric}
     * and {@link org.terasology.engine.telemetry.metrics.SystemContextMetric}
     * @param schema the snowplow event register schema.
     * @param mapSentToServer the map that contains the data sent to the server.
     * @return Null option if the mapSentToServer doesn't contain data.
     */
    public Optional<Unstructured> getUnstructuredMetric(String schema, Map<String, Object> mapSentToServer) {
        Optional<Unstructured> optional = Optional.empty();
        if (!isEmpty()) {
            SelfDescribingJson modulesData = new SelfDescribingJson(schema, mapSentToServer);
            Unstructured unstructured =  Unstructured.builder().
                    eventData(modulesData).
                    build();
            optional = Optional.of(unstructured);
        }
        return optional;
    }

    /**
     * Fetches all TelemetryFields and create a map associating field's name (key) to field's value (value).
     * @return a map with key (field's name) and value (field's value).
     */
    public Map<String, ?> createTelemetryFieldToValue() {
        return AccessController.doPrivileged((PrivilegedAction<Map<String, ?>>) () -> {

            telemetryFieldToValue = new HashMap<>();
            Set<Field> fields = ReflectionUtils.getFields(this.getClass(), ReflectionUtils.withAnnotation(TelemetryField.class));

            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    telemetryFieldToValue.put(field.getName(), field.get(this));
                } catch (IllegalAccessException e) {
                    logger.error("The field is not inaccessible: ", e);
                }
            }

            return telemetryFieldToValue;
        });
    }

    /**
     * Filter the metric map by the binding map.
     * If the user doesn't want the field to be sent, its value will be covered by "Disabled Field".
     * @param bindingMap the binding map.
     * @return a new metric map that covers the field that the user doesn't want to send by "Disabled Field".
     */
    protected Map<String, Object> filterMetricMap(Map<String, Boolean> bindingMap) {
        TelemetryCategory telemetryCategory = this.getClass().getAnnotation(TelemetryCategory.class);
        Context context = CoreRegistry.get(Context.class);
        DisplayDevice display = context.get(DisplayDevice.class);
        if (display.isHeadless() || telemetryCategory.isOneMapMetric()) {
            return telemetryFieldToValue;
        }
        Map<String, Object> metricMapAfterPermission = new HashMap<>();
        for (String fieldName : telemetryFieldToValue.keySet()) {
            String fieldNameWithID = telemetryCategory.id() + ":" + fieldName;
            if (bindingMap.containsKey(fieldNameWithID)) {
                if (bindingMap.get(fieldNameWithID)) {
                    metricMapAfterPermission.put(fieldName, telemetryFieldToValue.get(fieldName));
                } else {
                    metricMapAfterPermission.put(fieldName, "Disabled Field");
                }
            }
        }

        return metricMapAfterPermission;
    }

    /**
     * Filter the metric map by the binding map.
     * If the user doesn't want the field to be sent, its value will be covered by "Disabled Field".
     * This method could be used in module since {@link org.terasology.engine.config.facade.TelemetryConfiguration} is exposed to modules
     * @param telemetryConfiguration the telemetry configuration exposed modules
     * @return a new metric map that covers the field that the user doesn't want to send by "Disabled Field".
     */
    protected Map<String, Object> filterMetricMap(TelemetryConfiguration telemetryConfiguration) {
        TelemetryCategory telemetryCategory = this.getClass().getAnnotation(TelemetryCategory.class);
        Context context = CoreRegistry.get(Context.class);
        DisplayDevice display = context.get(DisplayDevice.class);
        if (display.isHeadless() || telemetryCategory.isOneMapMetric()) {
            return telemetryFieldToValue;
        }
        Map<String, Object> metricMapAfterPermission = new HashMap<>();
        for (String fieldName : telemetryFieldToValue.keySet()) {
            String fieldNameWithID = telemetryCategory.id() + ":" + fieldName;
            if (telemetryConfiguration.containsField(fieldNameWithID)) {
                if (telemetryConfiguration.get(fieldNameWithID)) {
                    metricMapAfterPermission.put(fieldName, telemetryFieldToValue.get(fieldName));
                } else {
                    metricMapAfterPermission.put(fieldName, "Disabled Field");
                }
            }
        }
        return metricMapAfterPermission;
    }

    /**
     * Add the new metric to {@link org.terasology.engine.telemetry.Metrics} instance.
     * This method will only be used when a metric constructor needs some specific arguments
     * other than {@link org.terasology.engine.context.Context}.
     * @param metrics the metrics class instance in the game context.
     */
    public void addToMetrics(Metrics metrics) {
        metrics.addMetric(this);
    }

    /**
     * Get a list of all the telemetry field names marked with {@link org.terasology.engine.telemetry.TelemetryField} annotation in this class.
     * The field name is in the form telemetryCategory.id() + ":" fieldName.
     * @return the list of all the telemetry field names in this class.
     */
    public List<String> createTelemetryFieldList() {
        TelemetryCategory telemetryCategory = this.getClass().getAnnotation(TelemetryCategory.class);
        List<String> fieldsList = new ArrayList<>();
        if (!telemetryCategory.isOneMapMetric()) {
            Set<Field> fields = ReflectionUtils.getFields(this.getClass(), ReflectionUtils.withAnnotation(TelemetryField.class));
            for (Field field : fields) {
                String fieldName = telemetryCategory.id() + ":" + field.getName();
                fieldsList.add(fieldName);
            }
        }
        return fieldsList;
    }

    public boolean isEmpty() {
        return (telemetryFieldToValue.size() == 0);
    }
}
