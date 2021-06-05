// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.generation.facets;

import com.google.common.collect.ImmutableMap;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BooleanFieldFacet3D;
import org.terasology.engine.world.generation.facets.base.SparseBooleanFieldFacet3D;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link SparseBooleanFieldFacet3D} class.
 *
 */
public class SparseBooleanFacetTest extends BooleanFacetTest {

    private SparseBooleanFieldFacet3D facet;

    @Override
    protected BooleanFieldFacet3D createFacet(BlockRegion region, Border3D border) {
        facet = new SparseBooleanFieldFacet3D(region, border) {
            // this class is abstract, but we don't want specific implementations
        };
        return facet;
    }

    @Test
    public void testGetRelativeMap() {

        facet.set(0, 1, 2, true);
        facet.set(0, 1, 3, true);
        facet.set(9, 3, 1, true);

        facet.setWorld(13, 28, 34, true);
        facet.setWorld(10, 21, 35, true);

        Map<Vector3i, Boolean> expected = ImmutableMap.of(
                new Vector3i(0, 1, 2), true,
                new Vector3i(0, 1, 3), true,
                new Vector3i(3, 8, 4), true,
                new Vector3i(0, 1, 5), true,
                new Vector3i(9, 3, 1), true);

        assertEquals(expected, facet.getRelativeEntries());
    }

    @Test
    public void testGetWorldMap() {

        facet.set(0, 1, 2, true);
        facet.set(0, 1, 3, true);
        facet.set(9, 3, 1, true);

        facet.setWorld(13, 28, 34, true);
        facet.setWorld(10, 21, 35, true);

        Map<Vector3i, Boolean> expected = ImmutableMap.of(
                new Vector3i(10, 21, 32), true,
                new Vector3i(10, 21, 33), true,
                new Vector3i(13, 28, 34), true,
                new Vector3i(10, 21, 35), true,
                new Vector3i(19, 23, 31), true);

        assertEquals(expected, facet.getWorldEntries());
    }
}
