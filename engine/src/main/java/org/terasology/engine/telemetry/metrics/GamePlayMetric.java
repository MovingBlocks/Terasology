// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.telemetry.metrics;

import com.snowplowanalytics.snowplow.tracker.events.Unstructured;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.telemetry.GamePlayStatsComponent;
import org.terasology.engine.telemetry.TelemetryCategory;
import org.terasology.engine.telemetry.TelemetryField;

import java.util.Map;
import java.util.Optional;

/**
 * A game play metric tracking metric such as distance traveled, play time, etc.
 * The stats begin at 0 when a new game starts.
 */
@TelemetryCategory(id = "gameplay",
        displayName = "${engine:menu#telemetry-game-play}",
        isOneMapMetric = false
)
public final class GamePlayMetric extends Metric {

    public static final String SCHEMA_GAMEPLAY = "iglu:org.terasology/gamePlay/jsonschema/1-0-0";

    private Map<String, Boolean> bindingMap;

    private LocalPlayer localPlayer;

    @TelemetryField
    private float distanceTraveled;

    @TelemetryField
    private long playTimeMinute;

    public GamePlayMetric(Context context) {
        bindingMap = context.get(Config.class).getTelemetryConfig().getMetricsUserPermissionConfig().getBindingMap();
    }

    @Override
    public Optional<Unstructured> getUnstructuredMetric() {
        createTelemetryFieldToValue();
        Map<String, Object> filteredMetricMap = filterMetricMap(bindingMap);
        return getUnstructuredMetric(SCHEMA_GAMEPLAY, filteredMetricMap);
    }

    @Override
    public Map<String, ?> createTelemetryFieldToValue() {
        localPlayer = CoreRegistry.get(LocalPlayer.class);
        EntityRef playerEntity = localPlayer.getCharacterEntity();
        if (playerEntity.hasComponent(GamePlayStatsComponent.class)) {
            GamePlayStatsComponent gamePlayStatsComponent = playerEntity.getComponent(GamePlayStatsComponent.class);
            distanceTraveled = gamePlayStatsComponent.distanceTraveled;
            playTimeMinute = (long) gamePlayStatsComponent.playTimeMinute;
            return super.createTelemetryFieldToValue();
        } else {
            return super.createTelemetryFieldToValue();
        }
    }
}
