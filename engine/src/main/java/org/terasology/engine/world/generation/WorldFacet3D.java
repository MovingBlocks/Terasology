// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import org.terasology.engine.math.Region3i;

/**
 */
public interface WorldFacet3D extends WorldFacet {

    /**
     * @return The region of the world covered by this facet
     */
    Region3i getWorldRegion();

    /**
     * @return The region covered by this facet, relative to the target region
     */
    Region3i getRelativeRegion();
}
