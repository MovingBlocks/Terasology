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
 * A player statistic metric for blocks destroyed in a game.
 */
@TelemetryCategory(id = "blockDestroyed",
        displayName = "${engine:menu#telemetry-block-destroyed}",
        isOneMapMetric = true
)
public final class BlockDestroyedMetric extends Metric {

    public static final String SCHEMA_BLOCK_DESTROYED = "iglu:org.terasology/blockDestroyed/jsonschema/1-0-0";

    private LocalPlayer localPlayer;

    // The telemetry field is not actually used here, it's for documentation.
    @TelemetryField
    private Map blockDestroyedMap;

    @Override
    public Optional<Unstructured> getUnstructuredMetric() {
        createTelemetryFieldToValue();
        return getUnstructuredMetric(SCHEMA_BLOCK_DESTROYED, telemetryFieldToValue);
    }

    @Override
    public Map<String, ?> createTelemetryFieldToValue() {
        localPlayer = CoreRegistry.get(LocalPlayer.class);
        EntityRef playerEntity = localPlayer.getCharacterEntity();
        if (playerEntity.hasComponent(GamePlayStatsComponent.class)) {
            GamePlayStatsComponent gamePlayStatsComponent = playerEntity.getComponent(GamePlayStatsComponent.class);
            telemetryFieldToValue.clear();
            telemetryFieldToValue.putAll(gamePlayStatsComponent.blockDestroyedMap);
            return telemetryFieldToValue;
        } else {
            return telemetryFieldToValue;
        }
    }
}
