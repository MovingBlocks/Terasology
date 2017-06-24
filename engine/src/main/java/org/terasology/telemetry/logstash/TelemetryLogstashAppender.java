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


import ch.qos.logback.classic.filter.ThresholdFilter;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import org.terasology.config.Config;
import org.terasology.context.Context;


/**
 * This is a logback Logstash appender that enriches error logs and sent them to the Logstash in server.
 * The default configuration of this appender is in logback.xml.
 */
public class TelemetryLogstashAppender extends LogstashTcpSocketAppender {

    private Context gameContext;

    public void configure(Context gameContext) {

        this.gameContext = gameContext;

        // if the error reporting is disabled at the beginning, then turn off error reporting.
        Config config = gameContext.get(Config.class);
        if (!config.getTelemetryConfig().isErrorReportingEnabled()) {
            turnOffErrorReporting();
        }

    }

    public Context getGameContext() {
        return gameContext;
    }

    public void turnOffErrorReporting() {
        this.clearAllFilters();

        ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel("off");
        filter.start();
        this.addFilter(filter);
    }

    public void turnOnErrorReporting() {
        this.clearAllFilters();

        ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel("error");
        filter.start();
        this.addFilter(filter);
    }
}
