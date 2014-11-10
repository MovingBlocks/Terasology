/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.utilities.tree;

import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.*;

public class SpaceTreeTest {
    @Test
    public void test2DTreeErrors() {
        SpaceTree<Object> tree = new SpaceTree<>(2);

        try {
            tree.add(null, new Object());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exp) {
            // Expected
        }

        try {
            tree.add(new float[1], new Object());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exp) {
            // Expected
        }

        try {
            tree.add(new float[3], new Object());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exp) {
            // Expected
        }

        try {
            tree.add(new float[2], null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exp) {
            // Expected
        }

        try {
            tree.remove(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exp) {
            // Expected
        }

        try {
            tree.remove(new float[1]);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exp) {
            // Expected
        }

        try {
            tree.remove(new float[3]);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exp) {
            // Expected
        }
    }

    @Test
    public void test2DTreeValues() {
        SpaceTree<Object> tree = new SpaceTree<>(2);

        assertNull(tree.remove(new float[2]));

        Object obj1 = new Object();
        Object obj2 = new Object();

        assertNull(tree.add(new float[2], obj1));
        assertSame(obj1, tree.add(new float[2], obj2));
        assertSame(obj2, tree.remove(new float[2]));

        assertNull(tree.remove(new float[2]));
    }

    @Test
    public void test3DTreeBasicProximity() {
        SpaceTree<Object> tree = new SpaceTree<>(3);

        Object obj1 = new Object();
        Object obj2 = new Object();

        float[] location1 = {0f, 0f, 0f};
        float[] location2 = {10f, 10f, 10f};

        tree.add(location1, obj1);
        tree.add(location2, obj2);

        assertSame(obj1, tree.findNearest(new float[]{3f, 3f, 3f}).value);
        assertSame(obj2, tree.findNearest(new float[]{100f, 100f, 0f}).value);

        float[] location3 = {5f, 5f, 5f};
        Object obj3 = new Object();
        tree.add(location3, obj3);

        assertSame(obj3, tree.findNearest(new float[]{3f, 3f, 3f}).value);
        assertSame(obj2, tree.findNearest(new float[]{100f, 100f, 0f}).value);
    }

    @Test
    public void test3DProximityTest() {
        SpaceTree<Object> tree = new SpaceTree<>(3);

        Object[] objects = {new Object(), new Object(), new Object(), new Object(), new Object(), new Object()};
        float[][] locations = {{0f, 0f, 0f}, {0f, 0f, 1f}, {0f, 0f, 2f}, {0f, 0f, 3f}, {0f, 0f, 4f}, {0f, 0f, 5f}};

        for (int i = 0; i < objects.length; i++) {
            tree.add(locations[i], objects[i]);
        }

        float delta = 0.0000001f;

        for (int i = 0; i < objects.length; i++) {
            DimensionalMap.Entry<Object> nearest = tree.findNearest(locations[i]);
            assertSame(objects[i], nearest.value);
            assertEquals(0f, nearest.distance, delta);
        }

        DimensionalMap.Entry<Object> nearestOne = tree.findNearest(new float[]{0f, 0f, -1f});
        assertSame(objects[0], nearestOne.value);
        assertEquals(1f, nearestOne.distance, delta);

        DimensionalMap.Entry<Object> nearestRoot = tree.findNearest(new float[]{0f, 1f, 6f});
        assertSame(objects[5], nearestRoot.value);
        assertEquals((float) Math.sqrt(2), nearestRoot.distance, delta);

        assertNull(tree.findNearest(new float[]{0f, 0f, -1f}, 0.5f));

        Collection<DimensionalMap.Entry<Object>> nearestTwo = tree.findNearest(new float[]{0f, 0f, 0f}, 2);

        assertEquals(2, nearestTwo.size());
        Iterator<DimensionalMap.Entry<Object>> nearestTwoIterator = nearestTwo.iterator();
        DimensionalMap.Entry<Object> firstNearest = nearestTwoIterator.next();
        DimensionalMap.Entry<Object> secondNearest = nearestTwoIterator.next();

        assertSame(objects[0], firstNearest.value);
        assertSame(objects[1], secondNearest.value);
        assertEquals(0, firstNearest.distance, delta);
        assertEquals(1, secondNearest.distance, delta);

        Collection<DimensionalMap.Entry<Object>> nearestThree = tree.findNearest(new float[]{0f, 0f, 5f}, 3, 1f);

        assertEquals(2, nearestThree.size());

        Iterator<DimensionalMap.Entry<Object>> nearestThreeIterator = nearestThree.iterator();
        firstNearest = nearestThreeIterator.next();
        secondNearest = nearestThreeIterator.next();

        assertSame(objects[5], firstNearest.value);
        assertSame(objects[4], secondNearest.value);
        assertEquals(0, firstNearest.distance, delta);
        assertEquals(1, secondNearest.distance, delta);
    }

    @Test
    public void testSearchDuplicationByDistance() {
        SpaceTree<Object> tree = new SpaceTree<>(3);

        Object[] objects = {new Object(), new Object(), new Object(), new Object(), new Object(), new Object()};
        float[][] locations = {{0f, 0f, 0f}, {0f, 0f, 1f}, {0f, 0f, 2f}, {0f, 0f, 3f}, {0f, 0f, 4f}, {0f, 0f, 5f}};

        for (int i = 0; i < objects.length; i++) {
            tree.add(locations[i], objects[i]);
        }

        float delta = 0.0000001f;

        Collection<DimensionalMap.Entry<Object>> nearest = tree.findNearest(locations[2], 3);
        assertEquals(3, nearest.size());

        Iterator<DimensionalMap.Entry<Object>> nearestIterator = nearest.iterator();

        DimensionalMap.Entry<Object> firstNearest = nearestIterator.next();
        DimensionalMap.Entry<Object> secondNearest = nearestIterator.next();
        DimensionalMap.Entry<Object> thirdNearest = nearestIterator.next();

        assertSame(objects[2], firstNearest.value);
        assertTrue(objects[1] == secondNearest.value || objects[3] == secondNearest.value);
        assertTrue(objects[1] == thirdNearest.value || objects[3] == thirdNearest.value);
        assertNotSame(secondNearest.value, thirdNearest.value);

        assertEquals(0, firstNearest.distance, delta);
        assertEquals(1, secondNearest.distance, delta);
        assertEquals(1, thirdNearest.distance, delta);
    }


}
