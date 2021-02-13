// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.layers.ingame.metrics;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.config.Config;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Direction;
import org.terasology.math.Orientation;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UILabel;
import org.terasology.persistence.StorageManager;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.Chunks;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.Locale;

/**
 * Displays the content of the MetricsMode instances provided by the {@link DebugMetricsSystem}.
 * <p>
 * Only a single {@link MetricsMode} is displayed on the screen.
 * <p>
 * See the {@link #toggleMetricsMode()} method to iterate through the MetricsMode instances available for display.
 */
public class DebugOverlay extends CoreScreenLayer {

    private static double MB_SIZE = 1048576.0;

    @In
    private Config config;

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

    @In
    private DebugMetricsSystem debugMetricsSystem;

    @In
    private StorageManager storageManager;

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
                    //Memory stats without using Runtime.getRuntime() for client side
                    StringBuilder clientTracking = new StringBuilder();
                    for (MemoryPoolMXBean mpBean : ManagementFactory.getMemoryPoolMXBeans()) {
                        if (mpBean.getType() == MemoryType.HEAP) {
                            MemoryUsage usage = mpBean.getUsage();
                            clientTracking.append(String.format("Memory Heap: %s - Memory Usage: %.2f MB, Max Memory: %.2f MB \n", mpBean.getName(), usage.getUsed() / MB_SIZE, usage.getMax() / MB_SIZE));
                        }
                    }
                    double memoryUsage = ((double) Runtime.getRuntime().totalMemory() - (double) Runtime.getRuntime().freeMemory()) / MB_SIZE;
                    return String.format("FPS: %.2f, Memory Usage: %.2f MB, Total Memory: %.2f MB, Max Memory: %.2f MB \n%s",
                            time.getFps(), memoryUsage, Runtime.getRuntime().totalMemory() / MB_SIZE, Runtime.getRuntime().maxMemory() / MB_SIZE,
                            clientTracking.toString());
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
                    Vector3f pos = localPlayer.getPosition(new Vector3f());
                    Vector3i chunkPos = Chunks.toChunkPos(pos, new Vector3i());
                    Vector3f rotation = localPlayer.getViewDirection(new Vector3f());
                    Vector3f cameraPos = localPlayer.getViewPosition(new Vector3f());
                    String orientation = "";
                    switch (Orientation.fromDirection(rotation.x, rotation.z)) {
                        case NORTH:
                            orientation = "N";
                            break;
                        case EAST:
                            orientation = "E";
                            break;
                        case SOUTH:
                            orientation = "S";
                            break;
                        case WEST:
                            orientation = "W";
                            break;
                        case NORTHEAST:
                            orientation = "NE";
                            break;
                        case SOUTHEAST:
                            orientation = "SE";
                            break;
                        case SOUTHWEST:
                            orientation = "SW";
                            break;
                        case NORTHWEST:
                            orientation = "NW";
                            break;
                    }
                    return String.format(Locale.US, "Position: (%.2f, %.2f, %.2f), Chunk (%d, %d, %d), Eye (%.2f, %.2f, %.2f), Rot (%.2f, %.2f, %.2f) %s", pos.x, pos.y, pos.z,
                            chunkPos.x, chunkPos.y, chunkPos.z,
                            cameraPos.x, cameraPos.y, cameraPos.z,
                            rotation.x, rotation.y, rotation.z, orientation);
                }
            });
        }

        UILabel debugLine4 = find("debugLine4", UILabel.class);
        if (debugLine4 != null) {
            debugLine4.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return String.format("Total VUs: %s, World Time: %.3f, Time Dilation: %.1f",
                            ChunkTessellator.getVertexArrayUpdateCount(),
                            worldProvider.getTime().getDays() - 0.0005f,    // use floor instead of rounding up
                            time.getGameTimeDilation());
                }
            });
        }

        UILabel debugInfo = find("debugInfo", UILabel.class);
        if (debugInfo != null) {
            debugInfo.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return String.format("[H] : Debug Documentation");
                }
            });
        }

        UILabel saveStatusLabel = find("saveStatusLabel", UILabel.class);
        // clients do not have a storage manager
        if (saveStatusLabel != null && storageManager != null) {
            saveStatusLabel.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return "Saving... ";
                }
            });
            saveStatusLabel.bindVisible(
                    new ReadOnlyBinding<Boolean>() {
                        @Override
                        public Boolean get() {
                            return storageManager.isSaving();
                        }
                    }
            );
        }

        UILabel wireframeMode = find("wireframeMode", UILabel.class);

        if (wireframeMode != null) {
            wireframeMode.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return "WIREFRAME MODE";
                }
            });
            wireframeMode.bindVisible(
                    new ReadOnlyBinding<Boolean>() {
                        @Override
                        public Boolean get() {
                            return config.getRendering().getDebug().isWireframe();
                        }
                    }
            );
        }

        UILabel chunkRenderMode = find("chunkBBRenderMode", UILabel.class);

        if (chunkRenderMode != null) {
            chunkRenderMode.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return "CHUNK BOUNDING BOX RENDER MODE";
                }
            });
            chunkRenderMode.bindVisible(
                    new ReadOnlyBinding<Boolean>() {
                        @Override
                        public Boolean get() {
                            return config.getRendering().getDebug().isRenderChunkBoundingBoxes();
                        }
                    }
            );
        }

        metricsLabel = find("metrics", UILabel.class);
    }

    @Override
    public void update(float delta) {
        metricsLabel.setText(debugMetricsSystem.getCurrentMode().getMetrics());
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    protected boolean isEscapeToCloseAllowed() {
        return false;
    }

    /**
     * Moves forward through the MetricsMode instances and displays the content of the next available one.
     */
    public void toggleMetricsMode() {
        MetricsMode mode = debugMetricsSystem.toggle();
        PerformanceMonitor.setEnabled(mode.isPerformanceManagerMode());
    }
}
