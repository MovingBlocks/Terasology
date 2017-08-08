/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.world.zones;

import com.google.common.collect.ImmutableList;
import org.terasology.module.sandbox.API;

import java.util.ArrayList;
import java.util.List;

/**
 * A manager for a set of {@link LayeredZoneRegionFunction}s. The manager will split the layers into aboveground and
 * underground layers, and sort each set by increasing distance from the surface (in terms of their ordering), so that
 * each zone can work out where it is positioned.
 */
@API
public class LayeredZoneManager {

    private final List<LayeredZoneRegionFunction> abovegroundLayers = new ArrayList<>();
    private final List<LayeredZoneRegionFunction> undergroundLayers = new ArrayList<>();

    public void addAbovegroundLayer(LayeredZoneRegionFunction function) {
        abovegroundLayers.add(function);
        abovegroundLayers.sort((l1, l2) -> ((Integer) Math.abs(l1.getOrdering())).compareTo(Math.abs(l2.getOrdering())));
    }

    public ImmutableList<LayeredZoneRegionFunction> getAbovegroundLayers() {
        return ImmutableList.copyOf(abovegroundLayers);
    }

    public void addUndergroundLayer(LayeredZoneRegionFunction function) {
        undergroundLayers.add(function);
        undergroundLayers.sort((l1, l2) -> ((Integer) Math.abs(l1.getOrdering())).compareTo(Math.abs(l2.getOrdering())));
    }

    public ImmutableList<LayeredZoneRegionFunction> getUndergroundLayers() {
        return ImmutableList.copyOf(undergroundLayers);
    }

}
