// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.monitoring;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepRegistryConfig;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class DisplayMetricsMonitor extends StepMeterRegistry {
    public static final DisplayMetricsMonitor metricRegistry = new DisplayMetricsMonitor();
    public static final Duration captureDuration = Duration.ofSeconds(1);

    public DisplayMetricsMonitor() {
        // micrometer doesn't provide concrete implementations of these Config classes.
        // We don't need to vary this configuration or load it from an external source,
        // so we define it using an anonymous class here.
        super(new StepRegistryConfig() {
            @Override
            public String prefix() {
                return "display";
            }

            @Override
            public String get(String key) {
                // This part of the API seems to only be for some config validation thing we don't use?
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
        // We don't need to push records anywhere. The view polls the current values; past steps are discarded.
    }

    @Override
    protected TimeUnit getBaseTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }
}
