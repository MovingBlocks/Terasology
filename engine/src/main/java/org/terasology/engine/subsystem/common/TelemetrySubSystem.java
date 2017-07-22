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
import org.terasology.context.Context;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.telemetry.Metrics;
import org.terasology.telemetry.TelemetryEmitter;
import org.terasology.telemetry.logstash.TelemetryLogstashAppender;

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

    private Context context;

    @Override
    public String getName() {
        return "Telemetry";
    }

    @Override
    public void preInitialise(Context rootContext) {

        context = rootContext;

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

        addTelemetryLogstashAppender(rootContext);
    }

    private void addTelemetryLogstashAppender(Context rootContext) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        TelemetryLogstashAppender telemetryLogstashAppender = new TelemetryLogstashAppender(rootContext);
        lc.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(telemetryLogstashAppender);
    }

    @Override
    public void preShutdown() {

        // TODO: REMOVE this when the telemetry is ready to users
        Config config = context.get(Config.class);
        TelemetryConfig telemetryConfig = config.getTelemetryConfig();
        telemetryConfig.setTelemetryEnabled(false);
        telemetryConfig.setErrorReportingEnabled(false);
    }

    @Override
    public void shutdown() {

        // shutdown emitter
        TelemetryEmitter telemetryEmitter = (TelemetryEmitter) emitter;
        telemetryEmitter.close();
    }
}
