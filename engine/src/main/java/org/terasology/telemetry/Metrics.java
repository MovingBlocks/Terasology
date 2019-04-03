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
package org.terasology.telemetry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ResolutionResult;
import org.terasology.module.predicates.FromModule;
import org.terasology.module.sandbox.API;
import org.terasology.naming.Name;
import org.terasology.telemetry.metrics.Metric;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Metrics class is similar to {@link org.terasology.config.Config}, it stores the telemetry information.
 * Once a new metric is used, the new metric instance should be added in this class to show the metric value in ui.
 */
@API
public class Metrics {

    private static final Logger logger = LoggerFactory.getLogger(Metrics.class);

    private Map<String, Metric> classNameToMetric = new HashMap<>();

    public void initialise(Context context) {

        Set<Class> metricsClassSet = fetchMetricsClassFromEnvironemnt(context);
        initializeMetrics(metricsClassSet, context);
    }

    private Set<Class> fetchMetricsClassFromEnvironemnt(Context context) {

        ModuleManager moduleManager = context.get(ModuleManager.class);
        Set<Class> metricsClassSet = new HashSet<>();
        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleId);
            if (module.isCodeModule()) {
                ResolutionResult result = resolver.resolve(moduleId);
                if (result.isSuccess()) {
                    try (ModuleEnvironment environment = moduleManager.loadEnvironment(result.getModules(), false)) {
                        for (Class<?> holdingType : environment.getTypesAnnotatedWith(TelemetryCategory.class, new FromModule(environment, moduleId))) {
                            metricsClassSet.add(holdingType);
                        }
                    }
                }
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
