// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joml.AABBf;
import org.joml.LineSegmentf;
import org.joml.Rayf;
import org.joml.Spheref;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class BlockRegionTest {

    @Test
    void testMinMax() {
        final Vector3i min = new Vector3i(1, 2, 3);
        final Vector3i max = new Vector3i(7, 8, 9);
        final BlockRegion region =
                BlockRegions.createFromMinAndMax(min, max);

        assertEquals(min, region.getMin(new Vector3i()));
        assertEquals(max, region.getMax(new Vector3i()));

        assertEquals(min.x, region.getMinX());
        assertEquals(region.getMinX(), region.minX());
        assertEquals(min.y, region.getMinY());
        assertEquals(region.getMinY(), region.minY());
        assertEquals(min.z, region.getMinZ());
        assertEquals(region.getMinZ(), region.minZ());

        assertEquals(max.x, region.getMaxX());
        assertEquals(region.getMaxX(), region.maxX());
        assertEquals(max.y, region.getMaxY());
        assertEquals(region.getMaxY(), region.maxY());
        assertEquals(max.z, region.getMaxZ());
        assertEquals(region.getMaxZ(), region.maxZ());
    }

    @Test
    void testCreateEmpty() {
        BlockRegion empty = new BlockRegion();

        final ArrayList<Vector3i> blockPositions = Lists.newArrayList(BlockRegions.iterable(empty));

        assertFalse(empty.isValid(), "empty region should be invalid");
        assertEquals(Collections.emptyList(), blockPositions, "empty region should contain no block positions");
        assertEquals(new Vector3i(0, 0, 0), empty.getSize(new Vector3i()), "empty region should have a size of 0");
    }

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

    private static Stream<Arguments> createFromMinAndMaxArgs() {
        return Stream.of(
                Arguments.of(new Vector3i(), new Vector3i(1, 1, 1), new Vector3i()),
                Arguments.of(new Vector3i(1, 1, 1), new Vector3i(3, 3, 3), new Vector3i(3, 3, 3)),
                Arguments.of(new Vector3i(3, 4, 5), new Vector3i(8, 5, 2), new Vector3i(10, 8, 6)),
                Arguments.of(new Vector3i(1, 1, 1), new Vector3i(0, 0, 0), new Vector3i(0, 0, 0)),
                Arguments.of(new Vector3i(0, 1, 0), new Vector3i(2, 0, 2), new Vector3i(1, 0, 1))
        );
    }

    @ParameterizedTest
    @MethodSource("createFromMinAndMaxArgs")
    public void createFromMinAndMax(Vector3i min, Vector3i expectedSize, Vector3i max) {
        BlockRegion region = BlockRegions.createFromMinAndMax(min, max);
        assertEquals(min, region.getMin(new Vector3i()), "min");
        assertEquals(max, region.getMax(new Vector3i()), "max");
        assertEquals(expectedSize, region.getSize(new Vector3i()), "size");
    }

    private static Stream<Arguments> createEncompassingArgs() {
        return Stream.of(
                Arguments.of(new Vector3i(1, 1, 1),
                        new Vector3i[]{new Vector3i(), new Vector3i()}),
                Arguments.of(new Vector3i(3, 3, 3),
                        new Vector3i[]{new Vector3i(1, 1, 1), new Vector3i(3, 3, 3)}),
                Arguments.of(new Vector3i(3, 3, 3),
                        new Vector3i[]{new Vector3i(3, 3, 3), new Vector3i(1, 1, 1)}),
                Arguments.of(new Vector3i(2, 2, 2),
                        new Vector3i[]{new Vector3i(0, 1, 0), new Vector3i(1, 0, 1)}),
                Arguments.of(new Vector3i(2, 3, 4),
                        new Vector3i[]{new Vector3i(0, 1, 0), new Vector3i(1, 0, 1), new Vector3i(0, -1, 3)})
        );
    }

    @ParameterizedTest
    @MethodSource("createEncompassingArgs")
    public void createEncompassingTest(Vector3i expectedSize, Vector3i[] positions) {
        Vector3i min = Arrays.stream(positions).reduce(new Vector3i(Integer.MAX_VALUE), Vector3i::min);
        Vector3i max = Arrays.stream(positions).reduce(new Vector3i(Integer.MIN_VALUE), Vector3i::max);

        BlockRegion region = BlockRegions.encompassing(positions);
        assertEquals(min, region.getMin(new Vector3i()), "min of " + region);
        assertEquals(max, region.getMax(new Vector3i()), "max of " + region);
        assertEquals(expectedSize, region.getSize(new Vector3i()), "size of " + region);
    }

    @Test
    public void testCreateRegionWithBounds() {
        BlockRegion expectedRegion = BlockRegions.createFromMinAndMax(new Vector3i(-2, 4, -16), new Vector3i(4, 107,
                0));
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
        BlockRegion region = BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(-1, 0, 0));
        assertFalse(region.isValid());
    }

    @Test
    public void testIterateRegion() {
        Vector3i min = new Vector3i(2, 5, 7);
        Vector3i max = new Vector3i(10, 11, 12);
        BlockRegion region = BlockRegions.createFromMinAndMax(min, max);

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
        BlockRegion region1 = BlockRegions.createFromMinAndMax(new Vector3i(), new Vector3i(32, 32, 32));
        BlockRegion region2 = BlockRegions.createFromMinAndMax(new Vector3i(1, 1, 1), new Vector3i(17, 17, 17));
        assertEquals(region2, region1.intersection(region2, new BlockRegion()));
    }

    @Test
    public void testNonTouchingIntersect() {
        BlockRegion region1 = BlockRegions.createFromMinAndMax(new Vector3i(), new Vector3i(32, 32, 32));
        BlockRegion region2 = BlockRegions.createFromMinAndMax(new Vector3i(103, 103, 103), new Vector3i(170, 170,
                170));
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
        BlockRegion region = BlockRegions.createFromMinAndMax(1, 0, 1, 0, 1, 0);
        region.correctBounds();

        assertEquals(min, region.getMin(new Vector3i()));
        assertEquals(max, region.getMax(new Vector3i()));
    }

    private static Stream<Arguments> testCenterArgs() {
        return Stream.of(
                Arguments.of(
                        new BlockRegion(),
                        new Vector3f(Float.NaN)
                ),
                // creating from min and max
                Arguments.of(
                        BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(0, 0, 0)),
                        new Vector3f(0f, 0f, 0f)
                ),
                Arguments.of(
                        BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(1, 1, 1)),
                        new Vector3f(.5f, .5f, .5f)
                ),
                Arguments.of(
                        BlockRegions.createFromMinAndMax(new Vector3i(-1, -1, -1), new Vector3i(1, 1, 1)),
                        new Vector3f(0f, 0f, 0f)
                ),
                Arguments.of(
                        BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(2, 2, 2)),
                        new Vector3f(1f, 1f, 1f)
                ),
                // creating from center and extents
                Arguments.of(
                        BlockRegions.createFromCenterAndExtents(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0)),
                        new Vector3f(0f, 0f, 0f)
                ),
                Arguments.of(
                        BlockRegions.createFromCenterAndExtents(new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0.5f,
                                0.5f, 0.5f)),
                        new Vector3f(.5f, .5f, .5f)
                ),
                Arguments.of(
                        BlockRegions.createFromCenterAndExtents(new Vector3f(0.49f, 0.49f, 0.49f), new Vector3f(0.5f,
                                0.5f, 0.5f)),
                        new Vector3f(0f, 0f, 0f)
                )

        );
    }

    @ParameterizedTest
    @MethodSource("testCenterArgs")
    public void testCenter(BlockRegion region, Vector3fc expectedCenter) {
        assertEquals(expectedCenter, region.center(new Vector3f()));
    }

    @Test
    public void testContainsPoint() {
        BlockRegion a = BlockRegions.createFromMinAndMax(0, 0, 0, 1, 1, 1);

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
        BlockRegion a = BlockRegions.createFromMinAndMax(0, 0, 0, 1, 1, 1);
        assertTrue(a.intersectsPlane(1, 1, 1, 1));
        assertFalse(a.intersectsPlane(1, 1, 1, 2));
    }

    @Test
    public void testIntersectionBlockRegion() {
        BlockRegion a = BlockRegions.createFromMinAndMax(0, 0, 0, 1, 1, 1);
        BlockRegion b = BlockRegions.createFromMinAndMax(1, 1, 1, 4, 4, 4);
        BlockRegion c = BlockRegions.createFromMinAndMax(3, 3, 3, 4, 4, 4);

        assertTrue(a.intersectsBlockRegion(b));
        assertFalse(a.intersectsBlockRegion(c));
    }


    static Stream<Arguments> testIntersectionAABB() {
        return Stream.of(
                Arguments.of(new AABBf(-.5f, -.5f, -.5f, 1.5f, 1.5f, 1.5f), true),
                Arguments.of(new AABBf(1.2f, 1.4999f, 1.2f, 2, 2, 2), true),
                Arguments.of(new AABBf(1.2f, 1.5f, 1.2f, 2, 2, 2), true),
                Arguments.of(new AABBf(1.2f, 1.50001f, 1.2f, 2, 2, 2), false),
                Arguments.of(new AABBf(2, 2, 2, 3, 3, 3), false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIntersectionAABB(AABBf aabb, boolean intersects) {
        BlockRegion region = BlockRegions.createFromMinAndMax(0, 0, 0, 1, 1, 1);
        assertEquals(intersects, region.intersectsAABB(aabb));
    }

    @Test
    public void testIntersectionSphere() {
        BlockRegion a = BlockRegions.createFromMinAndMax(0, 0, 0, 1, 1, 1);
        Spheref s1 = new Spheref(0, 0, 1, 2);
        Spheref s2 = new Spheref(3, 3, 3, 1);

        assertTrue(a.intersectsSphere(s1));
        assertTrue(a.intersectsSphere(2, 2, 2, 1));
        assertFalse(a.intersectsSphere(s2));
        assertFalse(a.intersectsSphere(2, 2, 2, 0.25f));
    }

    @Test
    public void testIntersectionRay() {
        BlockRegion a = BlockRegions.createFromMinAndMax(0, 0, 0, 1, 1, 1);
        Rayf r1 = new Rayf(0, 0, 3, 1, 1, -2);
        Rayf r2 = new Rayf(0, 2, 2, 1, 0, 0);

        assertTrue(a.intersectsRay(r1));
        assertFalse(a.intersectsRay(r2));
        assertTrue(a.intersectsRay(1.2f, 0, 0, 1, 0, 0));
        assertFalse(a.intersectsRay(0, 0, 3, 1, 1, -1));
    }

    @Test
    void testIntersectionLineSegment() {
        BlockRegion a = BlockRegions.createFromMinAndMax(0, 0, 0, 1, 1, 1);

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

    static Stream<Arguments> testAddExtentsfArgs() {
        return Stream.of(
                Arguments.of(.1f, .1f, .1f, BlockRegions.createFromMinAndMax(new Vector3i(), new Vector3i(3, 3, 3))),
                Arguments.of(-.1f, -.1f, -.1f, BlockRegions.createFromMinAndMax(new Vector3i(1, 1, 1), new Vector3i(2
                        , 2, 2))),
                Arguments.of(1f, 1f, 1f, BlockRegions.createFromMinAndMax(new Vector3i(-1, -1, -1), new Vector3i(4, 4
                        , 4))),
                Arguments.of(-1f, -1f, -1f, BlockRegions.createFromMinAndMax(new Vector3i(1, 1, 1), new Vector3i(2, 2
                        , 2))),
                Arguments.of(1.9f, 1.9f, 1.9f, BlockRegions.createFromMinAndMax(new Vector3i(-1, -1, -1),
                        new Vector3i(4, 4, 4)))
        );
    }

    @ParameterizedTest
    @MethodSource("testAddExtentsfArgs")
    void testAddExtentsf(float x, float y, float z, BlockRegion expected) {
        final BlockRegion region = BlockRegions.createFromMinAndMax(new Vector3i(), new Vector3i(3, 3, 3));
        assertEquals(expected, region.addExtents(x, y, z, region));
    }

    @Test
    void testGetBounds() {
        final BlockRegion region = BlockRegions.createFromMinAndMax(new Vector3i(1, 1, 1), new Vector3i(2, 3, 4));
        assertEquals(new AABBf(.5f, .5f, .5f, 2.5f, 3.5f, 4.5f), region.getBounds(new AABBf()));
    }
}
