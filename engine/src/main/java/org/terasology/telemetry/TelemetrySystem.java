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

import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.events.Unstructured;
import org.terasology.config.Config;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.telemetry.metrics.BlockDestroyedMetric;
import org.terasology.telemetry.metrics.BlockPlacedMetric;
import org.terasology.telemetry.metrics.GameConfigurationMetric;
import org.terasology.telemetry.metrics.GamePlayMetric;
import org.terasology.telemetry.metrics.ModulesMetric;
import org.terasology.telemetry.metrics.MonsterKilledMetric;
import org.terasology.telemetry.metrics.SystemContextMetric;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * This component system is used to track metrics in game.
 */
@RegisterSystem
public class TelemetrySystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private final String trackerNamespace = this.getClass().toString();

    private GamePlayStatsComponent gamePlayStatsComponent;

    private Instant lastRecordTime;

    private Instant timeForRefreshMetric;

    private Vector3f previousPos;

    private Map<String, Boolean> bindingMap;

    @In
    private Emitter emitter;

    @In
    private Metrics metrics;

    @In
    private Config config;

    @In
    private EntityManager entityManager;

    @In
    private LocalPlayer localPlayer;

    public void update(float delta) {
        if (localPlayer.isValid()) {

            // the first frame when the local player is valid
            if (previousPos == null) {
                setGamePlayStatsComponent();
                previousPos = localPlayer.getPosition();
            } else {
                recordDistanceTraveled();
                recordPlayTime();
            }
        }

        refreshMetricsPeriodic();
    }

    private void refreshMetricsPeriodic() {
        if (Duration.between(timeForRefreshMetric, Instant.now()).getSeconds() > 5) {
            metrics.refreshAllMetrics();
            timeForRefreshMetric = Instant.now();
        }
    }

    private void setGamePlayStatsComponent() {
        EntityRef localPlayerEntity = localPlayer.getCharacterEntity();
        if (!localPlayerEntity.hasComponent(GamePlayStatsComponent.class)) {
            gamePlayStatsComponent = new GamePlayStatsComponent();
            localPlayerEntity.addOrSaveComponent(gamePlayStatsComponent);
        } else {
            gamePlayStatsComponent = localPlayerEntity.getComponent(GamePlayStatsComponent.class);
        }
    }

    private void recordDistanceTraveled() {
        Vector3f position = localPlayer.getPosition();
        float distanceMoved = position.distance(previousPos);
        gamePlayStatsComponent.distanceTraveled += distanceMoved;
        previousPos = position;
        localPlayer.getCharacterEntity().addOrSaveComponent(gamePlayStatsComponent);
    }

    private void recordPlayTime() {
        float playTime = Duration.between(lastRecordTime, Instant.now()).toMillis() / 1000f / 60;
        gamePlayStatsComponent.playTimeMinute += playTime;
        localPlayer.getCharacterEntity().addOrSaveComponent(gamePlayStatsComponent);
        lastRecordTime = Instant.now();
    }

    @Override
    public void initialise() {
        timeForRefreshMetric = Instant.now();
    }

    @Override
    public void postBegin() {
        if (config.getTelemetryConfig().isTelemetryEnabled()) {
            bindingMap = config.getTelemetryConfig().getMetricsUserPermissionConfig().getBindingMap();
            sendModuleMetric();
            sendSystemContextMetric();
        }
        lastRecordTime = Instant.now();
    }

    private void sendModuleMetric() {
        ModulesMetric modulesMetric = metrics.getModulesMetric();
        Unstructured unstructuredMetric = modulesMetric.getUnstructuredMetric();
        TelemetryUtils.trackMetric(emitter, trackerNamespace, unstructuredMetric, modulesMetric, bindingMap);
    }

    private void sendSystemContextMetric() {
        SystemContextMetric systemContextMetric = metrics.getSystemContextMetric();
        Unstructured unstructured = systemContextMetric.getUnstructuredMetric();
        TelemetryUtils.trackMetric(emitter, trackerNamespace, unstructured, systemContextMetric, bindingMap);
    }

    @Override
    public void shutdown() {
        if (config.getTelemetryConfig().isTelemetryEnabled()) {
            BlockDestroyedMetric blockDestroyedMetric = metrics.getBlockDestroyedMetric();
            Unstructured unstructuredBlockDestroyed = blockDestroyedMetric.getUnstructuredMetric();
            TelemetryUtils.trackMetric(emitter, trackerNamespace, unstructuredBlockDestroyed, blockDestroyedMetric, bindingMap);

            BlockPlacedMetric blockPlacedMetric = metrics.getBlockPlacedMetric();
            Unstructured unstructuredBlockPlaced = blockPlacedMetric.getUnstructuredMetric();
            TelemetryUtils.trackMetric(emitter, trackerNamespace, unstructuredBlockPlaced, blockPlacedMetric, bindingMap);

            GameConfigurationMetric gameConfigurationMetric = metrics.getGameConfigurationMetric();
            Unstructured unstructuredGameConfiguration = gameConfigurationMetric.getUnstructuredMetric();
            TelemetryUtils.trackMetric(emitter, trackerNamespace, unstructuredGameConfiguration, gameConfigurationMetric, bindingMap);

            GamePlayMetric gamePlayMetric = metrics.getGamePlayMetric();
            Unstructured unstructuredGamePlay = gamePlayMetric.getUnstructuredMetric();
            TelemetryUtils.trackMetric(emitter, trackerNamespace, unstructuredGamePlay, gamePlayMetric, bindingMap);

            MonsterKilledMetric monsterKilledMetric = metrics.getMonsterKilledMetric();
            Unstructured unstructuredMonsterKilled = monsterKilledMetric.getUnstructuredMetric();
            TelemetryUtils.trackMetric(emitter, trackerNamespace, unstructuredMonsterKilled, monsterKilledMetric, bindingMap);
        }
    }
}
