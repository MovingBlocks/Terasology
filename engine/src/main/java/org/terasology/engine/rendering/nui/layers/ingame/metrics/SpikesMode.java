// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.layers.ingame.metrics;

import gnu.trove.map.TObjectDoubleMap;
import org.terasology.engine.monitoring.PerformanceMonitor;

final class SpikesMode extends TimeMetricsMode {

     SpikesMode() {
        super("\n- Spikes -", 10);
    }

    @Override
    protected TObjectDoubleMap<String> gatherMetrics() {
        return PerformanceMonitor.getDecayingSpikes();
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
