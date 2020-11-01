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

import gnu.trove.map.TObjectDoubleMap;
import org.terasology.monitoring.PerformanceMonitor;

/**
 */
final class AllocationsMode extends TimeMetricsMode {

    AllocationsMode() {
        super("\n- Memory Allocations -", 10, "bytes");
    }

    @Override
    protected TObjectDoubleMap<String> gatherMetrics() {
        return PerformanceMonitor.getAllocationMean();
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
