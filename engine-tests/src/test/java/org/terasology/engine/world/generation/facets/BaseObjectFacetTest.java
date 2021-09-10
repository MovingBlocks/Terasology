// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.generation.facets;

import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseObjectFacet3D;
import org.terasology.engine.world.generation.facets.base.ObjectFacet3D;

/**
 * Tests the {@link BaseObjectFacet3D} class.
 */
public class BaseObjectFacetTest extends ObjectFacetTest {

    @Override
    protected ObjectFacet3D<Integer> createFacet(BlockRegion region, Border3D border) {
        return new BaseObjectFacet3D<Integer>(region, border, Integer.class) {
            // this class is abstract, but we don't want specific implementations
        };
    }


}
