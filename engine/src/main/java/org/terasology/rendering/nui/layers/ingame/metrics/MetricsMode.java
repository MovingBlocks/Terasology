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

import org.terasology.module.sandbox.API;

/**
 * A metrics mode is a named entry in the {@link DebugOverlay}.
 *
 * It is intended to show development information that is not intended for a player. It consists of a name and a metrics
 * string to display. The metrics mode will be queried by the {@link DebugMetricsSystem} for the current metrics string
 * (poll-based approach).
 */
@API
public abstract class MetricsMode {

    private String name;

    /**
     * Create a new metrics mode with the given name.
     *
     * @param name the name of this metrics mode
     */
    public MetricsMode(String name) {
        this.name = name;
    }

    /**
     * The string to be displayed in the debug metrics overlay view.
     *
     * All formatting needs to be done by the metrics mode itself, as the debug overlay system will just print the
     * string on screen. Use multi-line strings to spread the metrics over multiple lines.
     *
     * @return the full metrics string to be rendered in the debug overlay
     */
    public abstract String getMetrics();

    /**
     * Whether the metric mode is currently available.
     *
     * This property may change over time. Using this flag a metrics mode can be registered with the debug overlay
     * system without immediately showing it.
     *
     * @return whether the metrics mode is currently available (should be displayed)
     */
    public abstract boolean isAvailable();

    public abstract boolean isPerformanceManagerMode();

    /**
     * A (human readable) name for the metrics mode.
     */
    public String getName() {
        return name;
    }
}
