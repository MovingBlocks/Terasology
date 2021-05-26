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
 * A player statistic metric for creatures killed in a game.
 */
@TelemetryCategory(id = "creatureKilled",
        displayName = "${engine:menu#telemetry-creature-killed}",
        isOneMapMetric = true
)
public final class CreatureKilledMetric extends Metric {

    public static final String SCHEMA_CREATURE_KILLED = "iglu:org.terasology/creatureKilled/jsonschema/1-0-0";

    private LocalPlayer localPlayer;

    @TelemetryField
    private Map creatureKilledMap;

    @Override
    public Optional<Unstructured> getUnstructuredMetric() {
        createTelemetryFieldToValue();
        return getUnstructuredMetric(SCHEMA_CREATURE_KILLED, telemetryFieldToValue);
    }

    @Override
    public Map<String, ?> createTelemetryFieldToValue() {
        localPlayer = CoreRegistry.get(LocalPlayer.class);
        EntityRef playerEntity = localPlayer.getCharacterEntity();
        if (playerEntity.hasComponent(GamePlayStatsComponent.class)) {
            GamePlayStatsComponent gamePlayStatsComponent = playerEntity.getComponent(GamePlayStatsComponent.class);
            telemetryFieldToValue.clear();
            telemetryFieldToValue.putAll(gamePlayStatsComponent.creatureKilled);
            return telemetryFieldToValue;
        } else {
            return telemetryFieldToValue;
        }
    }
}
