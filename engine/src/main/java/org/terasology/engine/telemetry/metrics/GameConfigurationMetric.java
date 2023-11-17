// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.telemetry.metrics;

import com.snowplowanalytics.snowplow.tracker.events.Unstructured;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.PlayerConfig;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.telemetry.TelemetryCategory;
import org.terasology.engine.telemetry.TelemetryField;
import org.terasology.engine.world.generator.WorldGenerator;

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

    @TelemetryField
    private String worldGenerator;

    @TelemetryField
    private String networkMode;

    @TelemetryField
    private String language;

    @TelemetryField
    private float playerHeight;

    @TelemetryField
    private float playerEyeHeight;

    private Context context;

    public GameConfigurationMetric(Context context) {
        this.context = context;
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
        WorldGenerator generator = CoreRegistry.get(WorldGenerator.class);
        if (generator != null) {
            worldGenerator = generator.getUri().toString();
        }
    }

    private void fetchNetworkMode() {
        NetworkSystem networkSystem = context.get(NetworkSystem.class);
        networkMode = networkSystem.getMode().toString();
    }

    private void fetchConfig() {
        SystemConfig systemConfig = context.get(SystemConfig.class);
        language = systemConfig.locale.get().toString();

        PlayerConfig playerConfig = context.get(PlayerConfig.class);
        playerHeight = playerConfig.height.get();
        playerEyeHeight = playerConfig.eyeHeight.get();
    }
}
