// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import org.terasology.math.geom.Rect2i;

/**
 */
public interface WorldFacet2D extends WorldFacet {

    /**
     * @return The region of the world covered by this facet
     */
    Rect2i getWorldRegion();

    /**
     * @return The region covered by this facet, relative to the target region
     */
    Rect2i getRelativeRegion();
}
