// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.generation.facets;

import org.terasology.math.Region3i;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionc;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.BaseFieldFacet2D;

/**
 * This facet represents the approximate height of the conceptual surface of the ground at each position.
 * It may exclude local features such as caves. It is intended to be used to determine the altitude relative
 * to the ground of features that are not placed directly on the surface, such as placing ores at a certain
 * depth below ground. Things that require more precise details of where all the surfaces are should use the
 * {@link SurfacesFacet} instead.
 */
public class ElevationFacet extends BaseFieldFacet2D {

    public ElevationFacet(BlockRegionc targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
