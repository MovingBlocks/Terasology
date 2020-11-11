// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.generation.facets;

import org.terasology.math.Region3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.BaseFieldFacet2D;

/**
 * This facet represents the approximate height of the conceptual surface of the ground at each position.
 * It may exclude local features such as caves. It is intended to be used to determine the altitude relative
 * to the ground of features that are not placed directly on the surface, such as placing ores at a certain
 * depth below ground.
 *
 * The combination of the {@link SurfacesFacet} and the ElevationFacet is a more flexible alternative to the {@link SurfaceHeightFacet}.
 */
public class ElevationFacet extends BaseFieldFacet2D {

    public ElevationFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
