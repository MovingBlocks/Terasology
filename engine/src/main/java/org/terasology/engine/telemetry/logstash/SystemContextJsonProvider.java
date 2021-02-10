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
package org.terasology.telemetry.logstash;

import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.FieldNamesAware;
import net.logstash.logback.composite.JsonWritingUtils;
import net.logstash.logback.fieldnames.LogstashFieldNames;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.terasology.context.Context;
import org.terasology.telemetry.Metrics;
import org.terasology.telemetry.TelemetryUtils;
import org.terasology.telemetry.metrics.Metric;
import org.terasology.telemetry.metrics.SystemContextMetric;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * SystemContextJsonProvider provides system context information to {@link org.terasology.telemetry.logstash.TelemetryLogstashAppender}.
 * These information will then send to the server.
 * You can find all the fields that will be sent in {@link org.terasology.telemetry.metrics.SystemContextMetric}
 * The fieldName can be reset in logback.xml.
 */
public class SystemContextJsonProvider extends AbstractFieldJsonProvider<ILoggingEvent> implements FieldNamesAware<LogstashFieldNames> {

    public static final String FIELD_SYSTEM_CONTEXT = "system_context";

    public SystemContextJsonProvider() {
        setFieldName(FIELD_SYSTEM_CONTEXT);
    }

    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {

        TelemetryLogstashAppender appender = TelemetryUtils.fetchTelemetryLogstashAppender();
        Context context = appender.getGameContext();

        if (context != null) {
            Metrics metrics = context.get(Metrics.class);
            Optional<Metric> optional = metrics.getMetric(SystemContextMetric.class);
            if (optional.isPresent()) {
                Metric systemContextMetric = optional.get();
                Map<String, ?> map = systemContextMetric.createTelemetryFieldToValue();
                Map<String, String> stringMap = TelemetryUtils.toStringMap(map);

                JsonWritingUtils.writeMapStringFields(generator, getFieldName(), stringMap);
            }
        }
    }

    public void setFieldNames(LogstashFieldNames fieldNames) {
        setFieldName(fieldNames.getMessage());
    }
}
