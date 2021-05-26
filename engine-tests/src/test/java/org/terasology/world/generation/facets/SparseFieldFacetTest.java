// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.generation.facets;

import com.google.common.collect.ImmutableMap;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.FieldFacet3D;
import org.terasology.engine.world.generation.facets.base.SparseFieldFacet3D;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link SparseFieldFacet3D} class.
 *
 */
public class SparseFieldFacetTest extends FieldFacetTest {

    private SparseFieldFacet3D facet;

    @Override
    protected FieldFacet3D createFacet(BlockRegion region, Border3D border) {
        facet = new SparseFieldFacet3D(region, border) {
            // this class is abstract, but we don't want specific implementations
        };
        return facet;
    }


    @Test
    public void testBoxedGetSetSparse() {
        facet.set(0, 1, 3, Integer.valueOf(64));
        assertEquals(64.0f, facet.get(0, 1, 3), 0.0);
    }

    @Test
    public void testGetRelativeMap() {

        facet.set(0, 1, 2, 4f);
        facet.set(0, 1, 3, 3);
        facet.set(9, 3, 1, Math.PI);

        facet.setWorld(13, 28, 34, 2);
        facet.setWorld(10, 21, 35, 1);

        Map<Vector3i, Number> expected = ImmutableMap.<Vector3i, Number>of(
                new Vector3i(0, 1, 2), 4.0f,
                new Vector3i(0, 1, 3), 3.0f,
                new Vector3i(3, 8, 4), 2.0f,
                new Vector3i(0, 1, 5), 1.0f,
                new Vector3i(9, 3, 1), Math.PI);

        assertEquals(expected, facet.getRelativeEntries());
    }

    @Test
    public void testGetWorldMap() {

        facet.set(0, 1, 2, 4f);
        facet.set(0, 1, 3, 3);
        facet.set(9, 3, 1, Math.PI);

        facet.setWorld(13, 28, 34, 2);
        facet.setWorld(10, 21, 35, 1);

        Map<Vector3i, Number> expected = ImmutableMap.<Vector3i, Number>of(
                new Vector3i(10, 21, 32), 4.0f,
                new Vector3i(10, 21, 33), 3.0f,
                new Vector3i(13, 28, 34), 2.0f,
                new Vector3i(10, 21, 35), 1.0f,
                new Vector3i(19, 23, 31), Math.PI);

        assertEquals(expected, facet.getWorldEntries());
    }
}
