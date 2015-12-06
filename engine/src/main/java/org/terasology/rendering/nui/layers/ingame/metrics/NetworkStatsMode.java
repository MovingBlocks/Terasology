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

import org.terasology.engine.Time;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;

/**
 */
final class NetworkStatsMode extends MetricsMode {
    private long lastTime;
    private Time time;
    private NetworkSystem networkSystem;
    private String lastMetric;


    public NetworkStatsMode() {
        super("Network");
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
