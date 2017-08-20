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

import org.terasology.module.sandbox.API;
import org.terasology.world.generation.Region;

@API
public interface ZoneRegionFunction {

    /**
     * Calculates whether or not the given block is part of this layer.
     *
     * @param x the x position to check
     * @param y the y position to check
     * @param z the z position to check
     * @param region the Region in the area
     * @return true if the position is within this layer, false otherwise
     */
    boolean apply(int x, int y, int z, Region region);

    /**
     * Initialize this function with data from the parent zone.
     *
     * @param parent the zone this function is attached to
     */
    default void initialize(Zone parent) {}

}
