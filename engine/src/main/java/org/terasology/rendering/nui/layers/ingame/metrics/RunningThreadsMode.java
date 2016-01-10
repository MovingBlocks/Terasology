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

import org.terasology.monitoring.ThreadMonitor;
import org.terasology.monitoring.impl.SingleThreadMonitor;

/**
 */
final class RunningThreadsMode extends MetricsMode {

    public RunningThreadsMode() {
        super("Running Threads");
    }

    @Override
    public String getMetrics() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append("\n");
        ThreadMonitor.getThreadMonitors(true).stream().filter(SingleThreadMonitor::isActive).forEach(threads -> {
            builder.append(threads.getName());
            builder.append(" - ");
            builder.append(threads.getLastTask());
            builder.append("\n");
        });
        return builder.toString();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isPerformanceManagerMode() {
        return false;
    }
}
