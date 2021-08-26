// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.layers.ingame.metrics;

import gnu.trove.map.TObjectDoubleMap;
import org.terasology.engine.monitoring.PerformanceMonitor;

final class RunningMeansMode extends TimeMetricsMode {

     RunningMeansMode() {
        super("\n- Means -", 10);
    }

    @Override
    protected TObjectDoubleMap<String> gatherMetrics() {
        return PerformanceMonitor.getRunningMean();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isPerformanceManagerMode() {
        return true;
    }
}
