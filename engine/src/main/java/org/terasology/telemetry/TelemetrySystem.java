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
import org.terasology.telemetry.metrics.Metric;
import org.terasology.telemetry.metrics.ModulesMetric;
import org.terasology.telemetry.metrics.CreatureKilledMetric;
import org.terasology.telemetry.metrics.SystemContextMetric;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
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

    /**
     * Refresh all the metric value and the bindingMap who notes user's authorization.
     */
    private void refreshMetricsPeriodic() {
        if (Duration.between(timeForRefreshMetric, Instant.now()).getSeconds() > 5) {
            metrics.refreshAllMetrics();
            timeForRefreshMetric = Instant.now();
            if (bindingMap != null) {
                refreshBindingMap();
            }
        }
    }

    /**
     * Refresh the bindingMap who notes user's authorization.
     * If a new map is added during the game, the authorization functionality could also be used.
     */
    private void refreshBindingMap() {
        Map<String, Metric> classNameToMetricMap = metrics.getClassNameToMetric();
        for (Metric metric : classNameToMetricMap.values()) {
            TelemetryCategory telemetryCategory = metric.getClass().getAnnotation(TelemetryCategory.class);
            if (!bindingMap.containsKey(telemetryCategory.id())) {
                bindingMap.put(telemetryCategory.id(), config.getTelemetryConfig().isTelemetryEnabled());
            }
            List<String> fields = metric.createTelemetryFieldList();
            for (String telemetryField : fields) {
                if (!bindingMap.containsKey(telemetryField)) {
                    bindingMap.put(telemetryField, config.getTelemetryConfig().isTelemetryEnabled());
                }
            }
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
        bindingMap = config.getTelemetryConfig().getMetricsUserPermissionConfig().getBindingMap();
        if (bindingMap != null) {
            refreshBindingMap();
        }
    }

    @Override
    public void postBegin() {
        if (config.getTelemetryConfig().isTelemetryEnabled()) {
            sendModuleMetric();
            sendSystemContextMetric();
        }
        lastRecordTime = Instant.now();
    }

    private void sendModuleMetric() {
        if (config.getTelemetryConfig().isTelemetryEnabled()) {
            TelemetryUtils.fetchMetricAndSend(metrics, ModulesMetric.class, emitter, trackerNamespace, bindingMap);
        }
    }

    private void sendSystemContextMetric() {
        if (config.getTelemetryConfig().isTelemetryEnabled()) {
            TelemetryUtils.fetchMetricAndSend(metrics, SystemContextMetric.class, emitter, trackerNamespace, bindingMap);
        }
    }

    @Override
    public void shutdown() {
        if (config.getTelemetryConfig().isTelemetryEnabled()) {
            TelemetryUtils.fetchMetricAndSend(metrics, BlockPlacedMetric.class, emitter, trackerNamespace, bindingMap);
            TelemetryUtils.fetchMetricAndSend(metrics, BlockDestroyedMetric.class, emitter, trackerNamespace, bindingMap);
            TelemetryUtils.fetchMetricAndSend(metrics, GameConfigurationMetric.class, emitter, trackerNamespace, bindingMap);
            TelemetryUtils.fetchMetricAndSend(metrics, CreatureKilledMetric.class, emitter, trackerNamespace, bindingMap);
            TelemetryUtils.fetchMetricAndSend(metrics, GamePlayMetric.class, emitter, trackerNamespace, bindingMap);
        }
    }
}
