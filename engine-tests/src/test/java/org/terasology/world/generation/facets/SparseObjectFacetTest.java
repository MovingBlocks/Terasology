/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.world.generation.facets;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.ObjectFacet3D;
import org.terasology.world.generation.facets.base.SparseObjectFacet3D;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link SparseObjectFacet3D} class.
 *
 */
public class SparseObjectFacetTest extends ObjectFacetTest {

    private SparseObjectFacet3D<Integer> facet;

    @Override
    protected ObjectFacet3D<Integer> createFacet(BlockRegion region, Border3D border) {
        facet = new SparseObjectFacet3D<Integer>(region, border) {
            // this class is abstract, but we don't want specific implementations
        };
        return facet;
    }


    @Test
    public void testBoxedGetSetSparse() {
        facet.set(0, 1, 3, 64);
        assertEquals(64.0f, facet.get(0, 1, 3), 0.0);
    }

    @Test
    public void testGetRelativeMap() {

        facet.set(0, 1, 2, 1);
        facet.set(0, 1, 3, 2);
        facet.set(9, 3, 1, 3);

        facet.setWorld(13, 28, 34, 2);
        facet.setWorld(10, 21, 35, 1);

        Map<Vector3i, Number> expected = ImmutableMap.<Vector3i, Number>of(
                new Vector3i(0, 1, 2), 1,
                new Vector3i(0, 1, 3), 2,
                new Vector3i(3, 8, 4), 2,
                new Vector3i(0, 1, 5), 1,
                new Vector3i(9, 3, 1), 3);

        assertEquals(expected, facet.getRelativeEntries());
    }

    @Test
    public void testGetWorldMap() {

        facet.set(0, 1, 2, 4);
        facet.set(0, 1, 3, 3);
        facet.set(9, 3, 1, 2);

        facet.setWorld(13, 28, 34, 2);
        facet.setWorld(10, 21, 35, 1);

        Map<Vector3i, Number> expected = ImmutableMap.<Vector3i, Number>of(
                new Vector3i(10, 21, 32), 4,
                new Vector3i(10, 21, 33), 3,
                new Vector3i(13, 28, 34), 2,
                new Vector3i(10, 21, 35), 1,
                new Vector3i(19, 23, 31), 2);

        assertEquals(expected, facet.getWorldEntries());
    }
}
