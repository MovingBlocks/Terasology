/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.layers.ingame.metrics;

import com.google.common.collect.Lists;
import org.terasology.config.Config;
import org.terasology.engine.GameEngine;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Locale;

/**
 * @author Immortius
 */
public class DebugOverlay extends CoreScreenLayer {

    @In
    private Config config;

    @In
    private GameEngine engine;

    @In
    private CameraTargetSystem cameraTarget;

    @In
    private Time time;

    @In
    private EntityManager entityManager;

    @In
    private LocalPlayer localPlayer;

    @In
    private WorldProvider worldProvider;

    private List<MetricsMode> metricsModes = Lists.newArrayList(new NullMetricsMode(), new RunningMeansMode(), new SpikesMode(), new RunningThreadsMode());
    private int currentMode;
    private UILabel metricsLabel;


    @Override
    public void initialise() {
        bindVisible(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return config.getSystem().isDebugEnabled();
            }
        });

        UILabel debugLine1 = find("debugLine1", UILabel.class);
        if (debugLine1 != null) {
            debugLine1.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    double memoryUsage = ((double) Runtime.getRuntime().totalMemory() - (double) Runtime.getRuntime().freeMemory()) / 1048576.0;
                    return String.format("fps: %.2f, mem usage: %.2f MB, total mem: %.2f, max mem: %.2f",
                            time.getFps(), memoryUsage, Runtime.getRuntime().totalMemory() / 1048576.0, Runtime.getRuntime().maxMemory() / 1048576.0);
                }
            });
        }

        UILabel debugLine2 = find("debugLine2", UILabel.class);
        if (debugLine2 != null) {
            debugLine2.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return String.format("Active Entities: %s, Current Target: %s", entityManager.getActiveEntityCount(), cameraTarget.toString());
                }
            });
        }

        UILabel debugLine3 = find("debugLine3", UILabel.class);
        if (debugLine3 != null) {
            debugLine3.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    Vector3f pos = localPlayer.getPosition();
                    CharacterComponent character = localPlayer.getCharacterEntity().getComponent(CharacterComponent.class);
                    float yaw = (character != null) ? character.yaw : 0;
                    return String.format(Locale.US, "Pos (%.2f, %.2f, %.2f), Yaw %.2f", pos.x, pos.y, pos.z, yaw);
                }
            });
        }

        UILabel debugLine4 = find("debugLine4", UILabel.class);
        if (debugLine4 != null) {
            debugLine4.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return String.format("total vus: %s | active threads: %s | worldTime: %.2f",
                            ChunkTessellator.getVertexArrayUpdateCount(), engine.getActiveTaskCount(),
                            worldProvider.getTime().getDays());
                }
            });
        }

        metricsLabel = find("metrics", UILabel.class);
    }

    @Override
    public void update(float delta) {
        if (metricsLabel != null) {
            metricsLabel.setText(metricsModes.get(currentMode).getMetrics());
        }
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    public boolean isEscapeToCloseAllowed() {
        return false;
    }

    public void toggleMetricsMode() {
        currentMode = (currentMode + 1) % metricsModes.size();
        while (!metricsModes.get(currentMode).isAvailable()) {
            currentMode = (currentMode + 1) % metricsModes.size();
        }
        PerformanceMonitor.setEnabled(metricsModes.get(currentMode).isPerformanceManagerMode());
    }
}
