// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.monitoring;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import io.micrometer.core.instrument.step.StepMeterRegistry;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class DisplayMetricsMonitor extends StepMeterRegistry {
    public static final DisplayMetricsMonitor metricRegistry = new DisplayMetricsMonitor();
    public static final Duration captureDuration = Duration.ofSeconds(1);

    public DisplayMetricsMonitor() {
        super(new LoggingRegistryConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public Duration step() {
                return captureDuration;
            }
        }, Clock.SYSTEM);
    }

    @Override
    protected void publish() {

    }

    @Override
    protected TimeUnit getBaseTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }
}
