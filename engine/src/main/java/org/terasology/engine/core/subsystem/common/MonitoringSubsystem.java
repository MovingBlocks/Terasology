// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.monitoring.gui.AdvancedMonitor;

import java.time.Duration;

public class MonitoringSubsystem implements EngineSubsystem {

    public static final Duration JMX_INTERVAL = Duration.ofSeconds(5);

    private AdvancedMonitor advancedMonitor;

    @Override
    public String getName() {
        return "Monitoring";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        if (rootContext.get(SystemConfig.class).monitoringEnabled.get()) {
            advancedMonitor = new AdvancedMonitor();
            advancedMonitor.setVisible(true);
            initMicrometerMetrics(rootContext.get(Time.class));
        }
    }

    /**
     * Initialize Micrometer metrics and publishers.
     *
     * @see org.terasology.engine.core.GameScheduler GameScheduler for global Reactor metrics
     * @param time provides statistics
     *     <p>
     *     Note {@link org.terasology.engine.core.EngineTime EngineTime}
     *     does not serve the same role as a {@link Clock micrometer Clock}.
     *     (Not yet. Maybe it should?)
     */
    private void initMicrometerMetrics(Time time) {
        // Register metrics with the built-in global composite registry.
        // This makes them available to any agent(s) we add to it.
        MeterRegistry meterRegistry = Metrics.globalRegistry;

        Gauge.builder("terasology.fps", time::getFps)
                .description("framerate")
                .baseUnit("Hz")
                .register(meterRegistry);

        // Publish the global metrics registry on a JMX server.
        MeterRegistry jmxMeterRegistry = new JmxMeterRegistry(new JmxConfig() {
            @Override
            public String get(String key) {
                return null;  // only needed if we have runtime configuration changes
            }

            @Override
            public Duration step() {
                return JMX_INTERVAL;  // default was 1 minute
            }
        }, Clock.SYSTEM);
        Metrics.addRegistry(jmxMeterRegistry);

        // If we want to make global metrics available to our custom view,
        // we add our custom registry to the global composite:
        //
        // Metrics.addRegistry(DebugOverlay.meterRegistry);
        //
        // If we want to see JVM metrics there as well:
        //
        // initAllJvmMetrics(DebugOverlay.meterRegistry);
    }

    /**
     * Installs all the micrometer built-in metrics.
     * <p>
     * Don't add these if you only intend to view them through a JMX registry, because they are
     * already available from the default JMX server even without micrometer. Add them if you
     * have a different agent you want them published through.
     */
    @SuppressWarnings("unused")
    void initAllJvmMetrics(MeterRegistry registry) {
        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
    }

    @Override
    public void shutdown() {
        if (advancedMonitor != null) {
            advancedMonitor.close();
        }
    }
}
