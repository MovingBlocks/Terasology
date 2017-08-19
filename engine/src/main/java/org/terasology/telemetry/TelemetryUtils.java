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
import org.terasology.context.Context;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.registry.CoreRegistry;
import org.terasology.telemetry.logstash.TelemetryLogstashAppender;
import org.terasology.telemetry.metrics.Metric;

import java.util.HashMap;
import java.util.Map;

/**
 * Utils methods for telemetry.
 */
public class TelemetryUtils {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryUtils.class);

    /**
     * track a metric.
     * @param emitter Emitter sending telemetry to the server.
     * @param nameSpace The name the class tracking this metric.
     * @param event The new event.
     * @param metric the Metric class instance that this event belongs to.
     * @param bindingMap the binding map contains fields who has user's permission.
     */
    public static void trackMetric(Emitter emitter, String nameSpace, Unstructured event, Metric metric, Map<String, Boolean> bindingMap) {
        Subject subject = new Subject.SubjectBuilder()
                .userId(TelemetryParams.userId)
                .build();

        // initialise tracker
        Tracker tracker = new Tracker.TrackerBuilder(emitter, nameSpace, TelemetryParams.APP_ID_TERASOLOGY)
                .subject(subject)
                .platform(TelemetryParams.PLATFORM_DESKTOP)
                .build();

        Context context = CoreRegistry.get(Context.class);
        DisplayDevice display = context.get(DisplayDevice.class);
        if (bindingMap.size() == 0 && display.isHeadless()) {
            tracker.track(event);
        } else if (bindingMap.size() != 0) {
            TelemetryCategory telemetryCategory = metric.getClass().getAnnotation(TelemetryCategory.class);
            if (telemetryCategory != null) {
                if (bindingMap.containsKey(telemetryCategory.id())) {
                    if ((bindingMap.get(telemetryCategory.id()))) {
                        tracker.track(event);
                    }
                }
            }
        }
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
