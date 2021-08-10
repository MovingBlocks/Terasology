// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.layers.ingame.metrics;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.search.Search;

import java.util.concurrent.TimeUnit;

final class RunningThreadsMode extends MetricsMode implements AutoCloseable {

     RunningThreadsMode() {
        super("\n- Running Threads -");
    }

    @Override
    public String getMetrics() {

        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append("\n");

        Search.in(Metrics.globalRegistry)
                .tag("monitor","display-metric")
                .timers().forEach(k -> {
            builder.append(k.getId().getName());
            builder.append(" : ");
            builder.append(k.mean(TimeUnit.MILLISECONDS));
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

    @Override
    public void close() throws Exception {

    }
}
