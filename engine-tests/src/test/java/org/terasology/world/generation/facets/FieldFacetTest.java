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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.FieldFacet3D;

/**
 * Tests different implementations of {@link FieldFacet3D}.
 *
 */
public abstract class FieldFacetTest {

    private FieldFacet3D facet;

    @Before
    public void setup() {
        Border3D border = new Border3D(0, 0, 0).extendBy(0, 15, 10);
        Vector3i min = new Vector3i(10, 20, 30);
        Vector3i size = new Vector3i(40, 50, 60);
        Region3i region = Region3i.createFromMinAndSize(min, size);
        facet = createFacet(region, border);
        // facet = [worldMin=(0, 5, 20), relativeMin=(-10, -15, -10), size=(60, 65, 80)]
    }

    protected abstract FieldFacet3D createFacet(Region3i region, Border3D extendBy);

    /**
     * Check unset values
     */
    @Test
    public void testUnset() {
        Assert.assertEquals(0.0f, facet.get(0, 0, 0), 0.0);
        Assert.assertEquals(0.0f, facet.getWorld(10, 20, 30), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelBounds() {
        facet.set(-15, -15, -15, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWorldBounds() {
        facet.setWorld(0, 0, 0, 1);
    }

    // Powers of 2 can be represented as float without rounding errors !

    @Test
    public void testPrimitiveGetSet() {
        facet.set(0, 1, 2, 2.0f);
        Assert.assertEquals(2.0f, facet.get(0, 1, 2), 0.0);
    }

    @Test
    public void testBoxedGetSet() {
        facet.set(0, 1, 3, 4f);
        Assert.assertEquals(4.0f, facet.get(0, 1, 3), 0.0);
    }

    @Test
    public void testBoxedWorldGetSet() {
        facet.set(0, 1, 4, 8f);
        Assert.assertEquals(8.0f, facet.get(0, 1, 4), 0.0);
    }

    @Test
    public void testMixedGetSet1() {
        facet.set(0, 1, 5, 16f);
        Assert.assertEquals(16.0f, facet.getWorld(10, 21, 35), 0.0);
    }

    @Test
    public void testMixedGetSet2() {
        facet.setWorld(24, 35, 46, 32f);
        Assert.assertEquals(32.0f, facet.get(14, 15, 16), 0.0);
    }

    @Test
    public void testMixedOnBorder() {
        facet.set(-5, -6, -7, 64f);
        Assert.assertEquals(64.0f, facet.getWorld(5, 14, 23), 0.0);
    }

}
