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
package org.terasology.telemetry;

import ch.qos.logback.classic.LoggerContext;
import com.snowplowanalytics.snowplow.tracker.Subject;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.events.Unstructured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.facade.TelemetryConfiguration;
import org.terasology.context.Context;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.registry.CoreRegistry;
import org.terasology.telemetry.logstash.TelemetryLogstashAppender;
import org.terasology.telemetry.metrics.Metric;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Utils methods for telemetry.
 */
@API
public final class TelemetryUtils {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryUtils.class);

    private TelemetryUtils() {
        // static utility class, no instance needed
    }

    /**
     * Fetch metric in {@link org.terasology.context.Context} and send to the server.
     * This method could be used in the modules.
     * @param context The game context
     * @param metricClass The class of the metric which we want to track
     * @param nameSpace The name the class tracking this metric.
     */
    public static void fetchMetricAndSend(Context context, Class metricClass, String nameSpace) {
        Emitter emitter = context.get(Emitter.class);
        Metrics metrics = context.get(Metrics.class);
        TelemetryConfiguration telemetryConfiguration = context.get(TelemetryConfiguration.class);
        if (emitter != null && metrics != null && telemetryConfiguration != null) {
            Optional<Metric> metricOptional = metrics.getMetric(metricClass);
            if (metricOptional.isPresent()) {
                Metric metric = metricOptional.get();
                Optional<Unstructured> unstructuredOptional = metric.getUnstructuredMetric();
                if (unstructuredOptional.isPresent()) {
                    Unstructured unstructured = unstructuredOptional.get();
                    trackMetric(emitter, nameSpace, unstructured, metric, telemetryConfiguration);
                }
            }
        } else {
            logger.error("Emitter or metrics or telemetryConfiguration is not in context");
        }
    }

    /**
     * fetch metric in {@link org.terasology.telemetry.Metrics} and send to the server.
     * @param metrics Metrics class in the game context.
     * @param metricClass The class of metric.
     * @param emitter Emitter sending telemetry to the server.
     * @param nameSpace The name the class tracking this metric.
     * @param bindingMap the binding map contains fields who has user's permission.
     */
    public static void fetchMetricAndSend(Metrics metrics, Class metricClass, Emitter emitter, String nameSpace, Map<String, Boolean> bindingMap) {
        Optional<Metric> optional = metrics.getMetric(metricClass);
        if (optional.isPresent()) {
            Metric metric = optional.get();
            Optional<Unstructured> unstructuredOptional = metric.getUnstructuredMetric();
            if (unstructuredOptional.isPresent()) {
                Unstructured unstructured = unstructuredOptional.get();
                trackMetric(emitter, nameSpace, unstructured, metric, bindingMap);
            }
        }
    }

    /**
     * Fetch metric in {@link org.terasology.telemetry.Metrics} and send to the server.
     * @param emitter Emitter sending telemetry to the server.
     * @param nameSpace The name the class tracking this metric.
     * @param event The new event.
     * @param metric the Metric class instance that this event belongs to.
     * @param telemetryConfiguration the telemetryConfiguration adapter which could be used in modules.
     */
    public static void trackMetric(Emitter emitter, String nameSpace, Unstructured event, Metric metric, TelemetryConfiguration telemetryConfiguration) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            Context context = CoreRegistry.get(Context.class);
            DisplayDevice display = context.get(DisplayDevice.class);
            if (telemetryConfiguration.fetchBindingSize() == 0 && display.isHeadless()) {
                trackMetric(emitter, nameSpace, event);
            } else if (telemetryConfiguration.fetchBindingSize() != 0) {
                TelemetryCategory telemetryCategory = metric.getClass().getAnnotation(TelemetryCategory.class);
                if (telemetryCategory != null) {
                    if (telemetryConfiguration.containsField(telemetryCategory.id())) {
                        if ((telemetryConfiguration.get(telemetryCategory.id()))) {
                            trackMetric(emitter, nameSpace, event);
                        }
                    }
                }
            }
            return null;
        });
    }

    /**
     * track a metric.
     * @param emitter Emitter sending telemetry to the server.
     * @param nameSpace The name the class tracking this metric.
     * @param event The new event.
     * @param metric the Metric class instance that this event belongs to.
     * @param bindingMap the binding map contains fields who has user's permission.
     */
    public static void trackMetric(Emitter emitter, String nameSpace, Unstructured event, Metric metric, Map<String, Boolean> bindingMap) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            Context context = CoreRegistry.get(Context.class);
            DisplayDevice display = context.get(DisplayDevice.class);
            if (bindingMap.size() == 0 && display.isHeadless()) {
                trackMetric(emitter, nameSpace, event);
            } else if (bindingMap.size() != 0) {
                TelemetryCategory telemetryCategory = metric.getClass().getAnnotation(TelemetryCategory.class);
                if (telemetryCategory != null) {
                    if (bindingMap.containsKey(telemetryCategory.id())) {
                        if ((bindingMap.get(telemetryCategory.id()))) {
                            trackMetric(emitter, nameSpace, event);
                        }
                    }
                }
            }
            return null;
        });
    }

    /**
     * track a metric without in spite of user's telemetry configuration.
     * It's rather a method for test.
     * @param emitter Emitter sending telemetry to the server.
     * @param nameSpace The name the class tracking this metric.
     * @param event The new event.
     */
    public static void trackMetric(Emitter emitter, String nameSpace, Unstructured event) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            Subject subject = new Subject.SubjectBuilder()
                    .userId(TelemetryParams.userId)
                    .ipAddress("anonymous")
                    .build();

            Tracker tracker = new Tracker.TrackerBuilder(emitter, nameSpace, TelemetryParams.APP_ID_TERASOLOGY)
                    .subject(subject)
                    .platform(TelemetryParams.PLATFORM_DESKTOP)
                    .build();
            tracker.track(event);

            return null;
        });
    }

    /**
     * Transform a map to a string map.
     * @param map the map with key type and value unknown.
     * @return a string map.
     */
    public static Map<String, String> toStringMap(Map<?, ?> map) {
        Map<String, String> stringMap = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            stringMap.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return stringMap;
    }

    /**
     * Fetch the logstash appender in the logger context.
     * @return the {@link TelemetryLogstashAppender} in the logger context.
     */
    public static TelemetryLogstashAppender fetchTelemetryLogstashAppender() {
        TelemetryLogstashAppender appender;
        try {
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            appender = (TelemetryLogstashAppender) lc.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("LOGSTASH");
        } catch (Exception e) {
            logger.error("Error when fetching TelemetryLogstashAppender", e);
            return null;
        }
        return appender;
    }
}
