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
import org.terasology.module.sandbox.API;
import org.terasology.telemetry.metrics.BlockDestroyedMetric;
import org.terasology.telemetry.metrics.BlockPlacedMetric;
import org.terasology.telemetry.metrics.GameConfigurationMetric;
import org.terasology.telemetry.metrics.GamePlayMetric;
import org.terasology.telemetry.metrics.Metric;
import org.terasology.telemetry.metrics.ModulesMetric;
import org.terasology.telemetry.metrics.MonsterKilledMetric;
import org.terasology.telemetry.metrics.SystemContextMetric;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Metrics class is similar to {@link org.terasology.config.Config}, it stores the telemetry information.
 * Once a new metric is used, the new metric instance should be added in this class to show the metric value in ui.
 */
@API
public class Metrics {

    private Map<String, Metric> metricsMap = new HashMap<>();

    public void initialise(Context context) {

        new SystemContextMetric(context);
        new ModulesMetric(context);
        new GameConfigurationMetric(context);
        new BlockDestroyedMetric(context);
        new BlockPlacedMetric(context);
        new GamePlayMetric(context);
        new MonsterKilledMetric(context);
    }

    public void refreshAllMetrics() {
        for (Metric metric: metricsMap.values()) {
            metric.getFieldValueMap();
        }
    }

    public Map<String, Metric> getMetricsMap() {
        return metricsMap;
    }

    /**
     * Get the metric in the context {@link org.terasology.telemetry.Metrics} class.
     * @param cl the class of the metric class.
     * @return the metric in the game context.
     */
    public Optional<Metric> getMetric(Class<?> cl) {
        return Optional.ofNullable(metricsMap.get(cl.getName()));
    }

    public void addMetric(Metric metric) {
        metricsMap.put(metric.getClass().getName(), metric);
    }
}
