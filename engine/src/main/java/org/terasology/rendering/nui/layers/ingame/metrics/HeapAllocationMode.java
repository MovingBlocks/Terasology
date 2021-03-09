// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.layers.ingame.metrics;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;

import static org.terasology.rendering.nui.layers.ingame.metrics.DebugOverlay.MB_SIZE;

public class HeapAllocationMode extends MetricsMode {
    private StringBuilder builder = new StringBuilder();

    /**
     * Create a new metrics mode with the given name.
     */
    public HeapAllocationMode() {
        super("\n- Heap Allocation -");
    }

    @Override
    public String getMetrics() {
        //Memory stats without using Runtime.getRuntime() for client side
        builder.setLength(0);
        builder.append(getName());
        builder.append("\n");
        for (MemoryPoolMXBean mpBean : ManagementFactory.getMemoryPoolMXBeans()) {
            if (mpBean.getType() == MemoryType.HEAP) {
                MemoryUsage usage = mpBean.getUsage();
                builder.append(String.format("Memory Heap: %s - Memory Usage: %.2f MB, Max Memory: %.2f MB \n", mpBean.getName(), usage.getUsed() / MB_SIZE, usage.getMax() / MB_SIZE));
            }
        }
        return builder.toString();
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
