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
package org.terasology.engine.subsystem.common;

import ch.qos.logback.classic.LoggerContext;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.TelemetryConfig;
import org.terasology.config.facade.TelemetryConfiguration;
import org.terasology.config.facade.TelemetryConfigurationImpl;
import org.terasology.context.Context;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.telemetry.Metrics;
import org.terasology.telemetry.TelemetryEmitter;
import org.terasology.telemetry.logstash.TelemetryLogstashAppender;

import java.net.MalformedURLException;
import java.net.URL;

import static org.terasology.telemetry.TelemetryEmitter.DEFAULT_COLLECTOR_HOST;
import static org.terasology.telemetry.TelemetryEmitter.DEFAULT_COLLECTOR_PORT;
import static org.terasology.telemetry.TelemetryEmitter.DEFAULT_COLLECTOR_PROTOCOL;

/**
 * This is a telemetry engine sub system.
 * It will initialise all the telemetry stuff such as the {@link com.snowplowanalytics.snowplow.tracker.emitter.Emitter} and configure the {@link org.terasology.telemetry.logstash.TelemetryLogstashAppender}.
 * It will also adds the {@link org.terasology.telemetry.Metrics} and the {@link org.terasology.telemetry.TelemetryEmitter} to the context so that we can be use them later in other class for telemetry.
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
            errorReportingDestination = TelemetryLogstashAppender.DEFAULT_LOGSTASH_HOST + ":" + TelemetryLogstashAppender.DEFAULT_LOGSTASH_PORT;
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
