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

    private static final Logger logger = LoggerFactory.getLogger(Metrics.class);

    private Map<String, Metric> metricsMap = new HashMap<>();

    public Metrics() {

    }

    public void initialise(Context context) {

        SystemContextMetric systemContextMetric = new SystemContextMetric(context);
        ModulesMetric modulesMetric = new ModulesMetric(context);
        GameConfigurationMetric gameConfigurationMetric = new GameConfigurationMetric(context);
        BlockDestroyedMetric blockDestroyedMetric = new BlockDestroyedMetric();
        BlockPlacedMetric blockPlacedMetric = new BlockPlacedMetric();
        GamePlayMetric gamePlayMetric = new GamePlayMetric(context);
        MonsterKilledMetric monsterKilledMetric = new MonsterKilledMetric();

        metricsMap.put(SystemContextMetric.class.getName(), systemContextMetric);
        metricsMap.put(ModulesMetric.class.getName(), modulesMetric);
        metricsMap.put(GameConfigurationMetric.class.getName(), gameConfigurationMetric);
        metricsMap.put(BlockDestroyedMetric.class.getName(), blockDestroyedMetric);
        metricsMap.put(BlockPlacedMetric.class.getName(), blockPlacedMetric);
        metricsMap.put(GamePlayMetric.class.getName(), gamePlayMetric);
        metricsMap.put(MonsterKilledMetric.class.getName(), monsterKilledMetric);
    }

    public void refreshAllMetrics() {
        for (Metric metric: metricsMap.values()) {
            metric.getFieldValueMap();
        }
    }

    public Optional<Metric> getMetric(Class<?> cl) {
        return Optional.ofNullable(metricsMap.get(cl.getName()));
    }

    public void addMetric(Metric metric) {
        metricsMap.put(metric.getClass().getName(), metric);
    }
}
