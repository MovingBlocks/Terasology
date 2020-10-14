// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.telemetry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ResolutionResult;
import org.terasology.module.predicates.FromModule;
import org.terasology.module.sandbox.API;
import org.terasology.naming.Name;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;
import org.terasology.telemetry.metrics.Metric;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Metrics class is similar to {@link org.terasology.config.Config}, it stores the telemetry information. Once a new
 * metric is used, the new metric instance should be added in this class to show the metric value in ui.
 */
@API
public class Metrics {

    private static final Logger logger = LoggerFactory.getLogger(Metrics.class);

    @In
    private ModuleManager moduleManager;

    @In
    private ContextAwareClassFactory classFactory;

    private final Map<String, Metric> classNameToMetric = new HashMap<>();

    public void initialise() {
        Set<Class> metricsClassSet = fetchMetricsClassFromEnvironemnt();
        initializeMetrics(metricsClassSet);
    }

    private Set<Class> fetchMetricsClassFromEnvironemnt() {
        Set<Class> metricsClassSet = new HashSet<>();
        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleId);
            if (module.isCodeModule()) {
                ResolutionResult result = resolver.resolve(moduleId);
                if (result.isSuccess()) {
                    try (ModuleEnvironment environment = moduleManager.loadEnvironment(result.getModules(), false)) {
                        for (Class<?> holdingType : environment.getTypesAnnotatedWith(TelemetryCategory.class,
                                new FromModule(environment, moduleId))) {
                            metricsClassSet.add(holdingType);
                        }
                    }
                }
            }
        }
        return metricsClassSet;
    }

    private void initializeMetrics(Set<Class> metricsClassSet) {
        for (Class clazz : metricsClassSet) {
            classFactory.createInjectableInstance(clazz);
        }
    }

    public void refreshAllMetrics() {
        for (Metric metric: classNameToMetric.values()) {
            metric.createTelemetryFieldToValue();
        }
    }

    public Map<String, Metric> getClassNameToMetric() {
        return classNameToMetric;
    }

    /**
     * Get the metric in the context {@link org.terasology.telemetry.Metrics} class.
     * @param cl the class of the metric class.
     * @return the metric in the game context.
     */
    public Optional<Metric> getMetric(Class<?> cl) {
        return Optional.ofNullable(classNameToMetric.get(cl.getName()));
    }

    /**
     * Add a metric instance to Metrics. This method will only be used when a metric constructor needs specific other than {@link org.terasology.context.Context}
     * @param metric a new metric that constructor needs some arguments other than {@link org.terasology.context.Context}
     */
    public void addMetric(Metric metric) {
        classNameToMetric.put(metric.getClass().getName(), metric);
    }
}
