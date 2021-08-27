// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.layers.ingame.metrics;

import org.terasology.engine.monitoring.ThreadMonitor;
import org.terasology.engine.monitoring.impl.SingleThreadMonitor;

final class RunningThreadsMode extends MetricsMode {

    RunningThreadsMode() {
        super("\n- Running Threads -");
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
