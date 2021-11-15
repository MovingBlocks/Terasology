// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.monitoring.gui.AdvancedMonitor;
import reactor.core.publisher.Flux;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class MonitoringSubsystem implements EngineSubsystem {

    public static final Duration JMX_INTERVAL = Duration.ofSeconds(5);

    private static final Logger logger = LoggerFactory.getLogger(MonitoringSubsystem.class);

    protected MeterRegistry meterRegistry;
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
        }
        meterRegistry = initMeterRegistries();
    }

    @Override
    public void postInitialise(Context context) {
        initMeters(context);
    }

    /**
     * Initialize Micrometer metrics and publishers.
     *
     * @see org.terasology.engine.core.GameScheduler GameScheduler for global Reactor metrics
     */
    protected MeterRegistry initMeterRegistries() {
        // Register metrics with the built-in global composite registry.
        // This makes them available to any agent(s) we add to it.
        MeterRegistry registry = Metrics.globalRegistry;

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

        return registry;
        // If we want to make global metrics available to our custom view,
        // we add our custom registry to the global composite:
        //
        // Metrics.addRegistry(DebugOverlay.meterRegistry);
        //
        // If we want to see JVM metrics there as well:
        //
        // allJvmMetrics().forEach(m -> m.bindTo(DebugOverlay.meterRegistry));
    }

    /** Initialize meters for all the things in this Context. */
    protected void initMeters(Context context) {
        // We can build meters individually like this:
        var time = context.get(Time.class);
        Gauge.builder("terasology.fps", time::getFps)
                .description("framerate")
                .baseUnit("Hz")
                .register(meterRegistry);

        // But we'd like the code for meters to live closer to the implementation
        // of the thing they're monitoring.
        //
        // Somewhere we get a list of all the things that provide meters.
        // Maybe hardcoded, maybe a registry of some kind? Modules will want
        // to contribute as well.
        var meterMaps = List.of(
                org.terasology.engine.rendering.world.Meters.GAUGE_MAP
        );

        meterMaps.forEach(gaugeMap -> registerForContext(context, gaugeMap));
    }

    protected void registerForContext(Context context, Iterable<GaugeMapEntry> gaugeMap) {
        Flux.fromIterable(gaugeMap)
            .map(entry -> Tuples.of(context.get(entry.iface), entry.gaugeSpecs))
            .filter(TupleUtils.predicate((subject, specs) -> subject != null))
            .doOnDiscard(Tuple2.class, TupleUtils.consumer((iface, gaugeSpecs) ->
                    logger.debug("Not building gauges for {}, none was in {}", iface, context)))
            .subscribe(TupleUtils.consumer(this::registerAll));
    }

    protected <T> void registerAll(T subject, Set<GaugeSpec<? extends T>> gaugeSpecs) {
        Flux.fromIterable(gaugeSpecs)
            .filter(spec -> spec.isInstanceOfType(subject))
            // Make sure the gauge is right for the specific type.
            .map(spec -> spec.binderAfterCasting(subject))
            .subscribe(this::registerMeter);
    }

    public void registerMeter(MeterBinder meterBinder) {
        meterBinder.bindTo(meterRegistry);
    }

    /**
     * Installs all the micrometer built-in metrics.
     * <p>
     * Don't add these if you only intend to view them through a JMX registry, because they are
     * already available from the default JMX server even without micrometer. Add them if you
     * have a different agent you want them published through.
     */
    @SuppressWarnings("unused")
    List<MeterBinder> allJvmMetrics() {
        return List.of(
            new ClassLoaderMetrics(),
            new JvmMemoryMetrics(),
            new JvmGcMetrics(),
            new JvmThreadMetrics(),
            new ProcessorMetrics()
        );
    }

    @Override
    public void shutdown() {
        if (advancedMonitor != null) {
            advancedMonitor.close();
        }
    }
}
