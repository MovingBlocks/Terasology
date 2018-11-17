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
package org.terasology.telemetry.metrics;

import com.snowplowanalytics.snowplow.tracker.events.Unstructured;
import org.terasology.config.Config;
import org.terasology.config.PlayerConfig;
import org.terasology.context.Context;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.telemetry.TelemetryCategory;
import org.terasology.telemetry.TelemetryField;
import org.terasology.world.generator.WorldGenerator;

import java.util.Map;
import java.util.Optional;

/**
 * A metric tracking game configuration such as world generator, network mode,etc.
 */
@TelemetryCategory(id = "gameConfiguration",
        displayName = "${engine:menu#telemetry-game-configuration}",
        isOneMapMetric = false
)
public final class GameConfigurationMetric extends Metric {

    public static final String SCHEMA_GAME_CONFIGURATION = "iglu:org.terasology/gameConfiguration/jsonschema/1-0-0";

    private Map<String, Boolean> bindingMap;


    public GameConfigurationMetric(Context context) {
        bindingMap = context.get(Config.class).getTelemetryConfig().getMetricsUserPermissionConfig().getBindingMap();
    }

    @Override
    public Optional<Unstructured> getUnstructuredMetric() {
        createTelemetryFieldToValue();
        Map<String, Object> filteredMetricMap = filterMetricMap(bindingMap);
        return getUnstructuredMetric(SCHEMA_GAME_CONFIGURATION, filteredMetricMap);
    }

    @Override
    public Map<String, ?> createTelemetryFieldToValue() {
        fetchWorldGenerator();
        fetchNetworkMode();
        fetchConfig();

        return super.createTelemetryFieldToValue();
    }

    private void fetchWorldGenerator() {
    }

    private void fetchNetworkMode() {
    }

    private void fetchConfig() {

    }
}
