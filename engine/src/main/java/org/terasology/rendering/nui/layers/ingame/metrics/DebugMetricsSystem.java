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


import com.google.common.base.Preconditions;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.Share;

import java.util.ArrayList;


/**
 * Manages an ordered set of MetricsMode instances.
 *
 *
 * A number of default metrics modes is instantiated in the initialize() method and becomes immediately available by
 * iterating through the set repeatedly invoking the toggle() method. The getCurrentMode() method can also be used to
 * obtain the mode currently pointed at. Further modes can be added to the set via the register method, while registered
 * modes can be removed via the unregister method. A convenience method unregisterAll() removes all registered modes at
 * once. MetricsModes are currently used by the DebugOverlay instance, displaying runtime statistics on screen.
 * unregister and unregisterAll functions.
 */
@RegisterSystem
@Share(DebugMetricsSystem.class)
public class DebugMetricsSystem extends BaseComponentSystem {

    private final MetricsMode defaultMode = new NullMetricsMode();
    private ArrayList<MetricsMode> modes;
    private MetricsMode currentMode;
    private int cursor;

    @Override
    public void initialise() {
        modes = new ArrayList<>();

        register(defaultMode);
        register(new RunningMeansMode());
        register(new SpikesMode());
        register(new AllocationsMode());
        register(new RunningThreadsMode());
        register(new WorldRendererMode());
        register(new RenderingExecTimeMeansMode("\n- Rendering - Execution Time: Running Means - Sorted Alphabetically -"));
        currentMode = defaultMode;
    }


    /**
     * Adds a MetricsMode instance to the set. Use the toggle() and getCurrentMode() methods to iterate over the set and
     * obtain the MetricsMode instances.
     * @param mode a MetricsMode instance
     */
    public boolean register(MetricsMode mode) {
        Preconditions.checkNotNull(mode, "Illegal argument passed, mode is null.");

        if (!modes.contains(mode)) {
            return modes.add(mode);
        } else {
            return false;
        }
    }

    /**
     * Returns current mode, initializes a default {@link NullMetricsMode}, if currentMode is null
     * @return the current MetricsMode instance
     */
    public MetricsMode getCurrentMode() {
        return currentMode;
    }

    /**
     * Iterates through the set of registered MetricsMode and returns the first instance whose method isAvailable()
     * returns true. Notice that this could be the MetricsMode that was current at the time this method was invoked.
     * @returns a MetricsMode instance
     */
    public MetricsMode toggle() {
        do {
            cursor = (cursor + 1) % modes.size();
            currentMode = modes.get(cursor);
        } while (!currentMode.isAvailable());

        return currentMode;
    }


    /**
     * Removes from the set the MetricsMode instance provided as input.
     *
     * If the MetricsMode instance is the mode currently pointed at by the iterator, toggles the iterator forward. Unregistering
     * defaultMode is not allowed, therefore throws {@link IllegalArgumentException}.
     *
     * @return True if the MetricsMode instance was in the set. False otherwise.
     */
    public boolean unregister(MetricsMode mode) {
        Preconditions.checkNotNull(mode, "Illegal argument passed, mode is null.");
        if (mode == defaultMode) {
            throw new IllegalArgumentException("Removing defaultMode is not allowed.");
        }

        if (mode == currentMode) {
            toggle();
        }

        return modes.remove(mode);
    }

    /**
     * Removes all registered metrics modes except an instance of NullMetricsMode, which is guaranteed to be always available.
     */
    public void unregisterAll() {
        modes.clear();
        cursor = 0;
        register(defaultMode);
        currentMode = defaultMode;
    }

    /**
     * @return the number of registered modes including defaultMode
     */
    public int getNumberOfModes() {
        return modes.size();
    }
}
