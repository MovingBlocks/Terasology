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

import org.terasology.gestalt.module.sandbox.API;

/**
 * This function is used to determine the thickness of a {@link LayeredZoneRegionFunction} at each point on the layer.
 */
@API
@FunctionalInterface
public interface LayerThickness {

    /**
     * @param x the world x coordinate
     * @param z the world z coordinate
     * @return the layer's thickness at the given co-ordinates
     */
    int get(int x, int z);

    /**
     * Initialize this with information about the parent region function, if needed.
     *
     * @param parent the layer this is attached to
     */
    default void initialize(LayeredZoneRegionFunction parent) {}
}
