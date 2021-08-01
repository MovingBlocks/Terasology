// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.layers.ingame.metrics;

import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.search.Search;
import org.terasology.engine.monitoring.DisplayMetricsMonitor;

import java.util.concurrent.TimeUnit;

final class RunningThreadsMode extends MetricsMode {

    RunningThreadsMode() {
        super("\n- Task Metrics -");
    }

    @Override
    public String getMetrics() {

        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append("\n");

        Search.in(DisplayMetricsMonitor.metricRegistry)
                .tag("monitor", "display-metric")
                .timers().forEach(k -> {

            HistogramSnapshot snapshot = k.takeSnapshot();
            long count = snapshot.count();

            if (count > 0) {
                double throughput = (count / (double) DisplayMetricsMonitor.captureDuration.toMillis());
                builder.append(k.getId().getName());
                builder.append(" : ");
                builder.append(" throughput: ");
                builder.append(Double.isNaN(throughput) ? "NAN" : String.format("%,10.4f", throughput)).append(" /s");
                builder.append(" mean: ").append(String.format("%,10.4f", k.mean(TimeUnit.MILLISECONDS))).append(" ms");
                builder.append(" max: ").append(String.format("%,10.4f", k.max(TimeUnit.MILLISECONDS))).append(" ms");
                builder.append("\n");
            }
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
