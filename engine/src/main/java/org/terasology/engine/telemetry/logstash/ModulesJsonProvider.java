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
package org.terasology.engine.telemetry.logstash;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.FieldNamesAware;
import net.logstash.logback.composite.JsonWritingUtils;
import net.logstash.logback.fieldnames.LogstashFieldNames;
import org.terasology.engine.context.Context;
import org.terasology.engine.telemetry.Metrics;
import org.terasology.engine.telemetry.TelemetryUtils;
import org.terasology.engine.telemetry.metrics.Metric;
import org.terasology.engine.telemetry.metrics.ModulesMetric;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * ModulesJsonProvider provides game module id and version to {@link org.terasology.engine.telemetry.logstash.TelemetryLogstashAppender}.
 * These information will then send to the server.
 * The fieldName can be reset in logback.xml.
 */
public class ModulesJsonProvider extends AbstractFieldJsonProvider<ILoggingEvent> implements FieldNamesAware<LogstashFieldNames> {

    public static final String FIELD_MODULES = "modules";

    public ModulesJsonProvider() {
        setFieldName(FIELD_MODULES);
    }

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent iLoggingEvent) throws IOException {

        TelemetryLogstashAppender appender = TelemetryUtils.fetchTelemetryLogstashAppender();
        Context context = appender.getGameContext();

        if (context != null) {
            Metrics metrics = context.get(Metrics.class);
            Optional<Metric> optional = metrics.getMetric(ModulesMetric.class);
            if (optional.isPresent()) {
                Metric modulesMetric = optional.get();
                Map<String, ?> map = modulesMetric.createTelemetryFieldToValue();
                Map<String, String> stringMap = TelemetryUtils.toStringMap(map);
                JsonWritingUtils.writeMapStringFields(generator, getFieldName(), stringMap);
            }
        }

    }

    public void setFieldNames(LogstashFieldNames fieldNames) {
        setFieldName(fieldNames.getMessage());
    }
}
