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

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.FieldNamesAware;
import net.logstash.logback.composite.JsonWritingUtils;
import net.logstash.logback.fieldnames.LogstashFieldNames;
import org.terasology.telemetry.TelemetryParams;

import java.io.IOException;

/**
 * This class append the user id to error log. And then the {@link org.terasology.telemetry.logstash.TelemetryLogstashAppender} will send logs to the server.
 * The user id is based on user's MAC address. Normally it differs from one to another.
 */
public class UserIdJsonProvider extends AbstractFieldJsonProvider<ILoggingEvent> implements FieldNamesAware<LogstashFieldNames> {

    public static final String FIELD_USER_ID = "user_id";

    public UserIdJsonProvider() {
        setFieldName(FIELD_USER_ID);
    }

    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        JsonWritingUtils.writeStringField(generator, getFieldName(), TelemetryParams.userId);
    }

    public void setFieldNames(LogstashFieldNames fieldNames) {
        setFieldName(fieldNames.getMessage());
    }
}
