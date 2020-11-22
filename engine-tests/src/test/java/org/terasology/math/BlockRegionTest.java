/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.math;

import com.google.common.collect.Sets;
import org.joml.AABBi;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.Test;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionIterable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class BlockRegionTest {


    @Test
    public void testCreateRegionWithMinAndSize() {
        List<Vector3i> mins = Arrays.asList(new Vector3i(), new Vector3i(1, 1, 1), new Vector3i(3, 4, 5));
        List<Vector3i> size = Arrays.asList(new Vector3i(1, 1, 1), new Vector3i(3, 3, 3), new Vector3i(8, 5, 2));
        List<Vector3i> expectedMax = Arrays.asList(new Vector3i(), new Vector3i(3, 3, 3), new Vector3i(10, 8, 6));

        for (int i = 0; i < mins.size(); ++i) {
            BlockRegion region = new BlockRegion().setMin(mins.get(i)).setSize(size.get(i));
            assertEquals(mins.get(i), region.getMin(new Vector3i()));
            assertEquals(size.get(i), region.getSize(new Vector3i()));
            assertEquals(expectedMax.get(i), region.getMax(new Vector3i()));
        }
    }

    @Test
    public void testCreateRegionWithMinMax() {
        List<Vector3i> mins = Arrays.asList(new Vector3i(), new Vector3i(1, 1, 1), new Vector3i(3, 4, 5));
        List<Vector3i> expectedSize = Arrays.asList(new Vector3i(1, 1, 1), new Vector3i(3, 3, 3), new Vector3i(8, 5, 2));
        List<Vector3i> max = Arrays.asList(new Vector3i(), new Vector3i(3, 3, 3), new Vector3i(10, 8, 6));
        for (int i = 0; i < mins.size(); ++i) {
            BlockRegion region = new BlockRegion(mins.get(i), max.get(i));
            assertEquals(mins.get(i), region.getMin(new Vector3i()));
            assertEquals(max.get(i), region.getMax(new Vector3i()));
            assertEquals(expectedSize.get(i), region.getSize(new Vector3i()));
        }
    }

    @Test
    public void testCreateRegionWithBounds() {
        BlockRegion expectedRegion = new BlockRegion(new Vector3i(-2, 4, -16), new Vector3i(4, 107, 0));
        List<Vector3i> vec1 = Arrays.asList(new Vector3i(-2, 4, -16), new Vector3i(4, 4, -16), new Vector3i(-2, 107, -16), new Vector3i(-2, 4, 0),
            new Vector3i(4, 107, -16), new Vector3i(4, 4, 0), new Vector3i(-2, 107, 0), new Vector3i(4, 107, 0));
        List<Vector3i> vec2 = Arrays.asList(new Vector3i(4, 107, 0), new Vector3i(-2, 107, 0), new Vector3i(4, 4, 0), new Vector3i(4, 107, -16),
            new Vector3i(-2, 4, 0), new Vector3i(-2, 107, -16), new Vector3i(4, 4, -16), new Vector3i(-2, 4, -16));
        for (int i = 0; i < vec1.size(); ++i) {
            BlockRegion target = new BlockRegion().union(vec1.get(i)).union(vec2.get(i));
            assertEquals(expectedRegion, target);
        }
    }

    @Test
    public void testRegionInvalidIfMaxLessThanMin() {
        BlockRegion region = new BlockRegion(new Vector3i(0, 0, 0), new Vector3i(-1, 0, 0));
        assertFalse(region.isValid());
    }

    @Test
    public void testIterateRegion() {
        Vector3i min = new Vector3i(2, 5, 7);
        Vector3i max = new Vector3i(10, 11, 12);
        BlockRegion region = new BlockRegion(min,max);

        Set<Vector3ic> expected = Sets.newHashSet();
        for (int x = min.x; x <= max.x; ++x) {
            for (int y = min.y; y <= max.y; ++y) {
                for (int z = min.z; z <= max.z; ++z) {
                    expected.add(new Vector3i(x, y, z));
                }
            }
        }


        for (Vector3ic pos : BlockRegionIterable.region(region).build()) {
            assertTrue(expected.contains(pos), "unexpected position: " + pos);
            expected.remove(pos);
        }

        assertEquals(0, expected.size(), "All vectors provided");
    }

    @Test
    public void testSimpleIntersect() {
        BlockRegion region1 = new BlockRegion(new Vector3i(), new Vector3i(32, 32, 32));
        BlockRegion region2 = new BlockRegion(new Vector3i(1, 1, 1), new Vector3i(17, 17, 17));
        assertEquals(region2, region1.intersection(region2, new BlockRegion()));
    }

    @Test
    public void testNonTouchingIntersect() {
        BlockRegion region1 = new BlockRegion(new Vector3i(), new Vector3i(32, 32, 32));
        BlockRegion region2 = new BlockRegion(new Vector3i(103, 103, 103), new Vector3i(170, 170, 170));
        assertFalse(region1.intersection(region2, new BlockRegion()).isValid());
    }

    @Test
    public void testEncompasses() {
        BlockRegion region = new BlockRegion().union(new Vector3i()).setSize(new Vector3i(1, 1, 1));
        assertTrue(region.containsBlock(0, 0, 0));

        assertFalse(region.containsBlock(1, 0, 0));
        assertFalse(region.containsBlock(1, 0, 1));
        assertFalse(region.containsBlock(0, 0, 1));
        assertFalse(region.containsBlock(-1, 0, -1));
        assertFalse(region.containsBlock(-1, 0, 0));
        assertFalse(region.containsBlock(-1, 0, -1));
        assertFalse(region.containsBlock(0, 0, -1));

        assertFalse(region.containsBlock(1, 1, 0));
        assertFalse(region.containsBlock(1, 1, 1));
        assertFalse(region.containsBlock(0, 1, 1));
        assertFalse(region.containsBlock(-1, 1, -1));
        assertFalse(region.containsBlock(-1, 1, 0));
        assertFalse(region.containsBlock(-1, 1, -1));
        assertFalse(region.containsBlock(0, 1, -1));

        assertFalse(region.containsBlock(1, -1, 0));
        assertFalse(region.containsBlock(1, -1, 1));
        assertFalse(region.containsBlock(0, -1, 1));
        assertFalse(region.containsBlock(-1, -1, -1));
        assertFalse(region.containsBlock(-1, -1, 0));
        assertFalse(region.containsBlock(-1, -1, -1));
        assertFalse(region.containsBlock(0, -1, -1));
    }
}
