// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import org.terasology.engine.world.block.BlockRegionc;

public interface WorldFacet3D extends WorldFacet {

    /**
     * @return The region of the world covered by this facet
     */
    BlockRegionc getWorldRegion();

    /**
     * @return The region covered by this facet, relative to the target region
     */
    BlockRegionc getRelativeRegion();
}
