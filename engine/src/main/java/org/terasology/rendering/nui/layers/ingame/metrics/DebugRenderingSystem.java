/*
 * Copyright 2016 MovingBlocks
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


import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.Share;

import java.util.Iterator;
import java.util.LinkedHashSet;

@RegisterSystem
@Share(DebugRenderingSystem.class)
public class DebugRenderingSystem extends BaseComponentSystem {

    private LinkedHashSet<MetricsMode> metricsModes;
    private Iterator<MetricsMode> modeIterator;
    private MetricsMode currentMode;

    @Override
    public void initialise() {
        metricsModes = Sets.newLinkedHashSet();
        modeIterator = Iterators.cycle(metricsModes);
        currentMode = register(new NullMetricsMode());
        register(new RunningMeansMode());
        register(new SpikesMode());
        register(new AllocationsMode());
        register(new RunningThreadsMode());
        register(new WorldRendererMode());
        register(new NetworkStatsMode());
        register(new RenderingExecTimeMeansMode("Rendering - Execution Time: Running Means - Sorted Alphabetically"));
    }

    public MetricsMode register(MetricsMode mode) {
        metricsModes.add(mode);
        return mode;
    }

    public MetricsMode getCurrentMode() {
        return currentMode;
    }

    public void toggle() {
        do {
            currentMode = modeIterator.next();
        } while (!getCurrentMode().isAvailable());
    }

    public boolean isMetricModesAvailable() {
        return !metricsModes.isEmpty();
    }

    public void unregister(MetricsMode mode) {
        metricsModes.remove(mode);
    }

    public void unregisterAll() {
        metricsModes.clear();
    }


}
