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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import net.logstash.logback.composite.JsonProviders;
import net.logstash.logback.composite.LogstashVersionJsonProvider;
import net.logstash.logback.composite.loggingevent.LogLevelJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggerNameJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggingEventFormattedTimestampJsonProvider;
import net.logstash.logback.composite.loggingevent.MdcJsonProvider;
import net.logstash.logback.composite.loggingevent.MessageJsonProvider;
import net.logstash.logback.composite.loggingevent.StackTraceJsonProvider;
import net.logstash.logback.composite.loggingevent.ThreadNameJsonProvider;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import net.logstash.logback.stacktrace.ShortenedThrowableConverter;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;

/**
 * This is a logback Logstash appender that enriches error logs and sent them to the Logstash in server.
 * The constructor has the default configuration of this appender. The destination of this appender will be set when error reporting is enabled.
 */
public class TelemetryLogstashAppender extends LogstashTcpSocketAppender {

    public static final String TELEMETRY_APPENDER_NAME = "LOGSTASH";

    public static final String DEFAULT_LOGSTASH_HOST = "telemetry.terasology.com";

    public static final String DEFAULT_LOGSTASH_OWNER = "Terasology Community";

    public static final String DEFAULT_LOGSTASH_NAME = "Logstash";

    public static final int DEFAULT_LOGSTASH_PORT = 9000;

    private Context gameContext;

    public TelemetryLogstashAppender(Context context) {

        this.setName(TELEMETRY_APPENDER_NAME);

        this.setContext((LoggerContext) LoggerFactory.getILoggerFactory());

        this.addErrorFilter();

        this.setDefaultEncoder();

        this.gameContext = context;
    }

    private void addErrorFilter() {

        ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel("error");
        filter.start();
        this.addFilter(filter);
    }

    private void setDefaultEncoder() {

        LoggingEventCompositeJsonEncoder loggingEventCompositeJsonEncoder = new LoggingEventCompositeJsonEncoder();
        JsonProviders jsonProviders = new JsonProviders();
        jsonProviders.addProvider(new LoggingEventFormattedTimestampJsonProvider());
        jsonProviders.addProvider(new LogstashVersionJsonProvider());
        jsonProviders.addProvider(new MdcJsonProvider());
        jsonProviders.addProvider(new MessageJsonProvider());
        jsonProviders.addProvider(new LogLevelJsonProvider());
        jsonProviders.addProvider(new LoggerNameJsonProvider());
        jsonProviders.addProvider(new ThreadNameJsonProvider());

        // custom providers
        jsonProviders.addProvider(new SystemContextJsonProvider());
        jsonProviders.addProvider(new ModulesJsonProvider());
        jsonProviders.addProvider(new UserIdJsonProvider());

        StackTraceJsonProvider stackTraceJsonProvider = new StackTraceJsonProvider();
        ShortenedThrowableConverter shortenedThrowableConverter = new ShortenedThrowableConverter();
        shortenedThrowableConverter.setMaxDepthPerThrowable(30);
        shortenedThrowableConverter.setMaxLength(2046);
        shortenedThrowableConverter.setShortenedClassNameLength(20);
        shortenedThrowableConverter.setRootCauseFirst(true);
        stackTraceJsonProvider.setThrowableConverter(shortenedThrowableConverter);
        jsonProviders.addProvider(stackTraceJsonProvider);

        loggingEventCompositeJsonEncoder.setProviders(jsonProviders);
        this.setEncoder(loggingEventCompositeJsonEncoder);
    }

    public Context getGameContext() {
        return gameContext;
    }
}
