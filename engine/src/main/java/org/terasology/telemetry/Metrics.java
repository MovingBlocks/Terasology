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

import org.terasology.context.Context;
import org.terasology.telemetry.metrics.Metric;
import org.terasology.telemetry.metrics.ModulesMetric;
import org.terasology.telemetry.metrics.SystemContextMetric;

import java.util.HashMap;
import java.util.Map;

/**
 * Metrics class is similar to {@link org.terasology.config.Config}, it stores the telemetry information.
 * Once a new metric is used, the new metric instance should be added in this class to show the metric value in ui.
 */
public class Metrics {

    private SystemContextMetric systemContextMetric;

    private ModulesMetric modulesMetric;

    public Metrics() {

    }

    public void initialise(Context context) {

        systemContextMetric = new SystemContextMetric();

        modulesMetric = new ModulesMetric(context);
    }

    public SystemContextMetric getSystemContextMetric() {
        return systemContextMetric;
    }

    public ModulesMetric getModulesMetric() {
        return modulesMetric;
    }

    public Map<Class,Metric> getMap() {
        Map<Class,Metric> map = new HashMap();
        map.put(SystemContextMetric.class, systemContextMetric);
        map.put(ModulesMetric.class, modulesMetric);

        return map;
    }
}
