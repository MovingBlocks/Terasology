// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.telemetry.metrics;

import com.snowplowanalytics.snowplow.tracker.events.Unstructured;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.telemetry.GamePlayStatsComponent;
import org.terasology.engine.telemetry.TelemetryCategory;
import org.terasology.engine.telemetry.TelemetryField;

import java.util.Map;
import java.util.Optional;

/**
 * A players statistic metric for blocks placed.
 */
@TelemetryCategory(id = "blockPlaced",
        displayName = "${engine:menu#telemetry-block-placed}",
        isOneMapMetric = true
)
public final class BlockPlacedMetric extends Metric {

    public static final String SCHEMA_BLOCK_PLACED = "iglu:org.terasology/blockPlaced/jsonschema/1-0-0";

    // The telemetry field is not actually used here, it's for documentation.
    @TelemetryField
    private Map blockPlacedMap;

    private LocalPlayer localPlayer;

    @Override
    public Optional<Unstructured> getUnstructuredMetric() {
        createTelemetryFieldToValue();
        return getUnstructuredMetric(SCHEMA_BLOCK_PLACED, telemetryFieldToValue);
    }

    @Override
    public Map<String, ?> createTelemetryFieldToValue() {
        localPlayer = CoreRegistry.get(LocalPlayer.class);
        EntityRef playerEntity = localPlayer.getCharacterEntity();
        if (playerEntity.hasComponent(GamePlayStatsComponent.class)) {
            GamePlayStatsComponent gamePlayStatsComponent = playerEntity.getComponent(GamePlayStatsComponent.class);
            telemetryFieldToValue.clear();
            telemetryFieldToValue.putAll(gamePlayStatsComponent.blockPlacedMap);
            return telemetryFieldToValue;
        } else {
            return telemetryFieldToValue;
        }
    }
}
