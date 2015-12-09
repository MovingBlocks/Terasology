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
import org.junit.Assert;
import org.junit.Test;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.BooleanFieldFacet3D;
import org.terasology.world.generation.facets.base.SparseBooleanFieldFacet3D;

import java.util.Map;

/**
 * Tests the {@link SparseBooleanFieldFacet3D} class.
 *
 */
public class SparseBooleanFacetTest extends BooleanFacetTest {

    private SparseBooleanFieldFacet3D facet;

    @Override
    protected BooleanFieldFacet3D createFacet(Region3i region, Border3D border) {
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

        Assert.assertEquals(expected, facet.getRelativeEntries());
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

        Assert.assertEquals(expected, facet.getWorldEntries());
    }
}
