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

import org.joml.AABBf;
import org.joml.AABBi;
import org.joml.LineSegmentf;
import org.joml.Rayf;
import org.joml.Spheref;
import org.joml.Vector2f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.Test;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegions;

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
        List<Vector3i> expectedSize = Arrays.asList(new Vector3i(1, 1, 1), new Vector3i(3, 3, 3), new Vector3i(8, 5,
                2));
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
        List<Vector3i> vec1 = Arrays.asList(new Vector3i(-2, 4, -16), new Vector3i(4, 4, -16), new Vector3i(-2, 107,
                        -16), new Vector3i(-2, 4, 0),
                new Vector3i(4, 107, -16), new Vector3i(4, 4, 0), new Vector3i(-2, 107, 0), new Vector3i(4, 107, 0));
        List<Vector3i> vec2 = Arrays.asList(new Vector3i(4, 107, 0), new Vector3i(-2, 107, 0), new Vector3i(4, 4, 0),
                new Vector3i(4, 107, -16),
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
        BlockRegion region = new BlockRegion(min, max);

        Set<Vector3ic> expected = Sets.newHashSet();
        for (int x = min.x; x <= max.x; ++x) {
            for (int y = min.y; y <= max.y; ++y) {
                for (int z = min.z; z <= max.z; ++z) {
                    expected.add(new Vector3i(x, y, z));
                }
            }
        }


        for (Vector3ic pos : BlockRegions.iterableInPlace(region)) {
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

    @Test
    public void testCorrectBoundsFlip() {
        Vector3i min = new Vector3i(0, 0, 0);
        Vector3i max = new Vector3i(1, 1, 1);
        BlockRegion region = BlockRegions.createFromMinAndMax(max, min);
        region.correctBounds();

        assertEquals(min, region.getMin(new Vector3i()));
        assertEquals(max, region.getMax(new Vector3i()));
    }

    @Test
    public void testCorrectBoundsMixed() {
        Vector3i min = new Vector3i(0, 0, 0);
        Vector3i max = new Vector3i(1, 1, 1);
        BlockRegion region = BlockRegions.createFromMinAndMax(1,0,1, 0,1,0);
        region.correctBounds();

        assertEquals(min, region.getMin(new Vector3i()));
        assertEquals(max, region.getMax(new Vector3i()));
    }
    
    @Test
    public void testContainsPoint() {
        BlockRegion a = new BlockRegion(0, 0, 0, 1, 1, 1);
        
        assertTrue(a.containsPoint(1.0f, 1.0f, 1.0f));
        
        assertTrue(a.containsPoint(1.2f, 0f, 0f));
        assertTrue(a.containsPoint(1.2f, 0f, 1.2f));
        assertFalse(a.containsPoint(1.2f, 0f, -1.2f));
        
        assertTrue(a.containsPoint(0f, 1.2f, 0f));
        assertTrue(a.containsPoint(0f, 1.2f, 1.2f));
        assertFalse(a.containsPoint(0f, 1.2f, -1.2f));
        
        assertTrue(a.containsPoint(1.2f, 1.2f, 0f));
        assertTrue(a.containsPoint(1.2f, 1.2f, 1.2f));
        assertFalse(a.containsPoint(1.2f, 1.2f, -1.2f));
        
        assertFalse(a.containsPoint(-1.2f, 0f, 0f));
        assertFalse(a.containsPoint(-1.2f, 0f, 1.2f));
        assertFalse(a.containsPoint(-1.2f, 0f, -1.2f));
        
        assertFalse(a.containsPoint(0f, -1.2f, 0f));
        assertFalse(a.containsPoint(0f, -1.2f, 1.2f));
        assertFalse(a.containsPoint(0f, -1.2f, -1.2f));
        
        assertFalse(a.containsPoint(-1.2f, 1.2f, 0f));
        assertFalse(a.containsPoint(-1.2f, 1.2f, 1.2f));
        assertFalse(a.containsPoint(-1.2f, 1.2f, -1.2f));
        
        assertFalse(a.containsPoint(1.2f, -1.2f, 0f));
        assertFalse(a.containsPoint(1.2f, -1.2f, 1.2f));
        assertFalse(a.containsPoint(1.2f, -1.2f, -1.2f));
        
        assertFalse(a.containsPoint(-1.2f, -1.2f, 0f));
        assertFalse(a.containsPoint(-1.2f, -1.2f, 1.2f));
        assertFalse(a.containsPoint(-1.2f, -1.2f, -1.2f));
        
    }
    
    @Test
    public void testIntersectionPlane() {
    	BlockRegion a = new BlockRegion(0, 0, 0, 1, 1, 1);
    	assertTrue(a.intersectsPlane(1, 1, 1, 1));
        assertFalse(a.intersectsPlane(1, 1, 1, 2));
    }
    
    @Test
    public void testIntersectionBlockRegion() {
    	BlockRegion a = new BlockRegion(0, 0, 0, 1, 1, 1);
    	BlockRegion b = new BlockRegion(1, 1, 1, 4, 4, 4);
    	BlockRegion c = new BlockRegion(3, 3, 3, 4, 4, 4);
    	
    	assertTrue(a.intersectsBlockRegion(b));
    	assertFalse(a.intersectsBlockRegion(c));
    }

@Test
    public void testIntersectionAABB() {
    	BlockRegion a = new BlockRegion(0, 0, 0, 1, 1, 1);
    	
    	AABBi aabbi1 = new AABBi(1, 1, 1, 2, 2, 2);
    	AABBi aabbi2 = new AABBi(2, 2, 2, 3, 3, 3);
    	assertTrue(a.intersectsAABB(aabbi1));
    	assertFalse(a.intersectsAABB(aabbi2));
    	
    	AABBf aabbf1 = new AABBf(1.2f, 1.5f, 1.2f, 2, 2, 2);
    	AABBf aabbf2 = new AABBf(2, 2, 2, 3, 3, 3);
    	assertTrue(a.intersectsAABB(aabbf1));
    	assertFalse(a.intersectsAABB(aabbf2));
    	
    }
    
    @Test
    public void testIntersectionSphere() {
    	BlockRegion a = new BlockRegion(0, 0, 0, 1, 1, 1);
    	Spheref s1 = new Spheref(0, 0, 1, 2);
    	Spheref s2 = new Spheref(3, 3, 3, 1);
    	
    	assertTrue(a.intersectsSphere(s1));
    	assertTrue(a.intersectsSphere(2, 2, 2, 1));
    	assertFalse(a.intersectsSphere(s2));
    	assertFalse(a.intersectsSphere(2, 2, 2, 0.25f));
    }
    
    @Test
    public void testIntersectionRay() {
    	BlockRegion a = new BlockRegion(0, 0, 0, 1, 1, 1);
    	Rayf r1 = new Rayf(0, 0, 3, 1, 1, -2);
    	Rayf r2 = new Rayf(0, 2, 2, 1, 0, 0);
    	
    	assertTrue(a.intersectsRay(r1));
    	assertFalse(a.intersectsRay(r2));
    	assertTrue(a.intersectsRay(1.2f, 0, 0, 1, 0, 0));
    	assertFalse(a.intersectsRay(0, 0, 3, 1, 1, -1));
    }
    
    @Test void testIntersectionLineSegment() {
    	BlockRegion a = new BlockRegion(0, 0, 0, 1, 1, 1);
    	
    	//no intersection
    	assertEquals(a.intersectLineSegment(3f, 3f, 3f, 2f, 3f, 3f, new Vector2f()), -1);
    	LineSegmentf l1 = new LineSegmentf(3f, 2f, 3f, 2f, 3f, 2f);
    	assertEquals(a.intersectLineSegment(l1, new Vector2f()), -1);
    	
    	//one intersection
    	assertEquals(a.intersectLineSegment(1.2f, 1.2f, 1.2f, 1.6f, 1.6f, 1.6f, new Vector2f()), 1);
    	LineSegmentf l2 = new LineSegmentf(-0.6f, 0f, 0f, -0.2f, 1.2f, 0f);
    	assertEquals(a.intersectLineSegment(l2, new Vector2f()), 1);
    	
    	//two intersections
    	assertEquals(a.intersectLineSegment(1.2f, 1.2f, 2f, -0.6f, 0f, -0.2f, new Vector2f()), 2);
    	LineSegmentf l3 = new LineSegmentf(2f, 2f, 2f, -0.6f, -2f, 0f);
    	assertEquals(a.intersectLineSegment(l3, new Vector2f()), 2);
    	
    	//segment inside the BlocRegion
    	assertEquals(a.intersectLineSegment(0f, 1f, 1.2f, 1f, -0.2f, 0.2f, new Vector2f()), 3);
    	LineSegmentf l4 = new LineSegmentf(1f, 1f, 1.2f, -0.2f, 0f, 1f);
    	assertEquals(a.intersectLineSegment(l4, new Vector2f()), 3);
    }
}
