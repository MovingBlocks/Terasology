// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.layers.ingame.metrics;

import org.terasology.engine.core.Time;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.CoreRegistry;

final class NetworkStatsMode extends MetricsMode {
    private long lastTime;
    private Time time;
    private NetworkSystem networkSystem;
    private String lastMetric;


    NetworkStatsMode() {
        super("\n- Network -");
        lastMetric = getName();
        time = CoreRegistry.get(Time.class);
        networkSystem = CoreRegistry.get(NetworkSystem.class);
    }

    @Override
    public String getMetrics() {
        // only update the metric a minimum once a second, cache the result
        long currentTime = time.getGameTimeInMs();
        long timeDifference = currentTime - lastTime;
        if (timeDifference >= 1000) {
            StringBuilder builder = new StringBuilder();
            builder.append(getName());
            builder.append("\n");
            builder.append(String.format("Elapsed: %dms%n", timeDifference));
            builder.append(String.format("In Msg: %d%n", networkSystem.getIncomingMessagesDelta()));
            builder.append(String.format("In Bytes: %d%n", networkSystem.getIncomingBytesDelta()));
            builder.append(String.format("Out Msg: %d%n", networkSystem.getOutgoingMessagesDelta()));
            builder.append(String.format("Out Bytes: %d%n", networkSystem.getOutgoingBytesDelta()));
            if (lastTime != 0) {
                // ignore the first update as it will not have useful data
                lastMetric = builder.toString();
            }
            lastTime = currentTime;

        }
        return lastMetric;
    }

    @Override
    public boolean isAvailable() {
        return networkSystem.getMode() != NetworkMode.NONE;
    }

    @Override
    public boolean isPerformanceManagerMode() {
        return false;
    }
}
