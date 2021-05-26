// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import ch.qos.logback.classic.LoggerContext;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.TelemetryConfig;
import org.terasology.engine.config.facade.TelemetryConfiguration;
import org.terasology.engine.config.facade.TelemetryConfigurationImpl;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.telemetry.Metrics;
import org.terasology.engine.telemetry.TelemetryEmitter;
import org.terasology.engine.telemetry.logstash.TelemetryLogstashAppender;

import java.net.MalformedURLException;
import java.net.URL;

import static org.terasology.engine.telemetry.TelemetryEmitter.DEFAULT_COLLECTOR_HOST;
import static org.terasology.engine.telemetry.TelemetryEmitter.DEFAULT_COLLECTOR_PORT;
import static org.terasology.engine.telemetry.TelemetryEmitter.DEFAULT_COLLECTOR_PROTOCOL;

/**
 * This is a telemetry engine sub system. It will initialise all the telemetry stuff such as the {@link
 * com.snowplowanalytics.snowplow.tracker.emitter.Emitter} and configure the {@link
 * org.terasology.engine.telemetry.logstash.TelemetryLogstashAppender}. It will also adds the {@link
 * org.terasology.engine.telemetry.Metrics} and the {@link org.terasology.engine.telemetry.TelemetryEmitter} to the
 * context so that we can be use them later in other class for telemetry.
 *
 * @see <a href="https://github.com/GabrielXia/telemetry/wiki">https://github.com/GabrielXia/telemetry/wiki</a>
 */
public class TelemetrySubSystem implements EngineSubsystem {

    private static final Logger logger = LoggerFactory.getLogger(TelemetrySubSystem.class);

    private Metrics metrics;

    private Emitter emitter;

    @Override
    public String getName() {
        return "Telemetry";
    }

    @Override
    public void preInitialise(Context rootContext) {

        // Add metrics to context, this helps show metric values in ui
        metrics = new Metrics();
        rootContext.put(Metrics.class, metrics);

        // Add snowplow emitter to context, contributors can use this emitter to emit other event
        emitter = TelemetryEmitter.builder().build();
        rootContext.put(Emitter.class, emitter);
    }

    @Override
    public void postInitialise(Context rootContext) {

        metrics.initialise(rootContext);

        // Add the telemetryConfig adapter to context. It could be used in modules.
        Config config = rootContext.get(Config.class);
        TelemetryConfig telemetryConfig = config.getTelemetryConfig();
        TelemetryConfiguration telemetryConfiguration = new TelemetryConfigurationImpl(telemetryConfig);
        rootContext.put(TelemetryConfiguration.class, telemetryConfiguration);

        addTelemetryLogstashAppender(rootContext);
        setTelemetryDestination(rootContext);
    }

    private void addTelemetryLogstashAppender(Context rootContext) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        TelemetryLogstashAppender telemetryLogstashAppender = new TelemetryLogstashAppender(rootContext);
        lc.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(telemetryLogstashAppender);

        Config config = rootContext.get(Config.class);
        TelemetryConfig telemetryConfig = config.getTelemetryConfig();
        String errorReportingDestination = telemetryConfig.getErrorReportingDestination();
        if (errorReportingDestination == null) {
            errorReportingDestination =
                    TelemetryLogstashAppender.DEFAULT_LOGSTASH_HOST + ":" + TelemetryLogstashAppender.DEFAULT_LOGSTASH_PORT;
            telemetryConfig.setErrorReportingDestination(errorReportingDestination);
        }
        if (telemetryConfig.isErrorReportingEnabled()) {
            telemetryLogstashAppender.addDestination(errorReportingDestination);
            telemetryLogstashAppender.start();
        }
    }

    private void setTelemetryDestination(Context rootContext) {
        Config config = rootContext.get(Config.class);
        TelemetryConfig telemetryConfig = config.getTelemetryConfig();
        String telemetryDestination = telemetryConfig.getTelemetryDestination();
        if (telemetryDestination != null) {
            try {
                URL url = new URL(telemetryDestination);
                TelemetryEmitter telemetryEmitter = (TelemetryEmitter) emitter;
                telemetryEmitter.changeUrl(url);
            } catch (MalformedURLException e) {
                logger.error("URL malformed", e);
            }
        } else {
            telemetryConfig.setTelemetryDestination(TelemetryEmitter.getDefaultCollectorURL(
                    DEFAULT_COLLECTOR_PROTOCOL, DEFAULT_COLLECTOR_HOST, DEFAULT_COLLECTOR_PORT).toString());
        }
    }

    @Override
    public void shutdown() {

        // shutdown emitter
        TelemetryEmitter telemetryEmitter = (TelemetryEmitter) emitter;
        telemetryEmitter.close();
    }
}
