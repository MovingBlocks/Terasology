/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.biomesAPI;

import org.terasology.rendering.nui.layers.ingame.metrics.MetricsMode;

/**
 * A MetricsMode implementation to display Current Biome info in the debug section
 * Current biome is set when OnBiomeChanged event is triggered
 * biomeName is polled whenever MetricsMode values are updated
 */
class BiomesMetricsMode extends MetricsMode {

    private String biomeName;

    BiomesMetricsMode() {
        super("\n- Biome Info -");
    }

    @Override
    public String getMetrics() {
        return getName()+"\n"+biomeName;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isPerformanceManagerMode() {
        return false;
    }

    public void setBiome(String biomeName) {
        this.biomeName = biomeName;
    }
}
