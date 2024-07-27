// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.zones;

import org.terasology.context.annotation.API;

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
    default void initialize(LayeredZoneRegionFunction parent) { }
}
