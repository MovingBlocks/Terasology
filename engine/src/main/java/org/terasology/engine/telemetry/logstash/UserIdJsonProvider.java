// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.telemetry.logstash;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.FieldNamesAware;
import net.logstash.logback.composite.JsonWritingUtils;
import net.logstash.logback.fieldnames.LogstashFieldNames;
import org.terasology.engine.telemetry.TelemetryParams;

import java.io.IOException;

/**
 * This class append the user id to error log. And then the {@link org.terasology.engine.telemetry.logstash.TelemetryLogstashAppender} will send logs to the server.
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
