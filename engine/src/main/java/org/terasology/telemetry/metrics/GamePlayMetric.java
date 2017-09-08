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
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.telemetry.GamePlayStatsComponent;
import org.terasology.telemetry.TelemetryCategory;
import org.terasology.telemetry.TelemetryField;

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
