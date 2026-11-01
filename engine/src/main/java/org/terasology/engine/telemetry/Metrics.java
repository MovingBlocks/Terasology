// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.telemetry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.context.annotation.API;
import org.terasology.engine.telemetry.metrics.Metric;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Metrics class is similar to {@link org.terasology.engine.config.Config}, it stores the telemetry information.
 * Once a new metric is used, the new metric instance should be added in this class to show the metric value in ui.
 */
@API
public class Metrics {

    private static final Logger logger = LoggerFactory.getLogger(Metrics.class);

    private Map<String, Metric> classNameToMetric = new HashMap<>();

    public void initialise(Context context) {

        Set<Class> metricsClassSet = fetchMetricsClassFromEngineOnlyEnvironment(context);
        initializeMetrics(metricsClassSet, context);
    }

    private Set<Class> fetchMetricsClassFromEngineOnlyEnvironment(Context context) {
        ModuleManager moduleManager = context.get(ModuleManager.class);
        Set<Class> metricsClassSet = new HashSet<>();
        try (ModuleEnvironment environment = moduleManager.loadEnvironment(Collections.emptySet(), false)) {
            for (Class<?> holdingType : environment.getTypesAnnotatedWith(TelemetryCategory.class)) {
                metricsClassSet.add(holdingType);
            }
        }

        return metricsClassSet;
    }

    private void initializeMetrics(Set<Class> metricsClassSet, Context context) {

        for (Class clazz : metricsClassSet) {
            Constructor[] constructors = clazz.getConstructors();
            for (Constructor constructor : constructors) {
                Class[] parameters = constructor.getParameterTypes();
                try {
                    Metric metric;
                    if (parameters.length  == 0) {
                        metric = (Metric) constructor.newInstance();
                        classNameToMetric.put(metric.getClass().getName(), metric);
                    } else if (parameters.length == 1 && parameters[0].equals(Context.class)) {
                        metric = (Metric) constructor.newInstance(context);
                        classNameToMetric.put(metric.getClass().getName(), metric);
                    } else {
                        logger.warn("Can't initialize the Metric, please initialize it and add it to Metrics: {}", constructor);
                    }
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    logger.error("Fail to initialize the metric instance: {}", constructor);
                }
            }
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
     * Get the metric in the context {@link org.terasology.engine.telemetry.Metrics} class.
     * @param cl the class of the metric class.
     * @return the metric in the game context.
     */
    public Optional<Metric> getMetric(Class<?> cl) {
        return Optional.ofNullable(classNameToMetric.get(cl.getName()));
    }

    /**
     * Add a metric instance to Metrics. This method will only be used when a metric constructor needs specific
     * other than {@link org.terasology.engine.context.Context}
     * @param metric a new metric that constructor needs some arguments other than {@link org.terasology.engine.context.Context}
     */
    public void addMetric(Metric metric) {
        classNameToMetric.put(metric.getClass().getName(), metric);
    }
}
