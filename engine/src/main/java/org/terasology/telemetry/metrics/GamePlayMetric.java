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
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.telemetry.GamePlayStatsComponent;
import org.terasology.telemetry.TelemetryCategory;
import org.terasology.telemetry.TelemetryField;

import java.util.Map;

/**
 * A game play metric tracking metric such as distance traveled, play time, etc.
 * The stats begin at 0 when a new game starts.
 */
@TelemetryCategory(id = "gameplay",
        displayName = "${engine:menu#telemetry-game-play}"
)
public final class GamePlayMetric extends Metric {

    public static final String SCHEMA_GAMEPLAY = "iglu:org.terasology/gamePlay/jsonschema/1-0-0";

    private LocalPlayer localPlayer;

    @TelemetryField
    private float distanceTraveled;

    @TelemetryField
    private long playTimeMinute;

    public GamePlayMetric() {
    }

    @Override
    public Unstructured getUnstructuredMetric() {
        getFieldValueMap();
        SelfDescribingJson modulesData = new SelfDescribingJson(SCHEMA_GAMEPLAY, metricMap);

        return Unstructured.builder()
                .eventData(modulesData)
                .build();
    }

    @Override
    public Map<String, ?> getFieldValueMap() {
        localPlayer = CoreRegistry.get(LocalPlayer.class);
        EntityRef playerEntity = localPlayer.getCharacterEntity();
        if (playerEntity.hasComponent(GamePlayStatsComponent.class)) {
            GamePlayStatsComponent gamePlayStatsComponent = playerEntity.getComponent(GamePlayStatsComponent.class);
            distanceTraveled = gamePlayStatsComponent.distanceTraveled;
            playTimeMinute = (long) gamePlayStatsComponent.playTimeMinute;
            return super.getFieldValueMap();
        } else {
            return metricMap;
        }
    }
}
