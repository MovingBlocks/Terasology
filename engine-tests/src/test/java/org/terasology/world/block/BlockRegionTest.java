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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockRegionTest {

    @Test
    void getMinMax() {
        final Vector3i min = new Vector3i(1, 2, 3);
        final Vector3i max = new Vector3i(7, 8, 9);
        final BlockRegion region =
                BlockRegions.fromMinAndMax(min, max);

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
    void setMinMax() {
        BlockRegion region = new BlockRegion(new Vector3i());

        assertEquals(-1, region.minX(-1).minX());
        assertEquals(-2, region.minY(-2).minY());
        assertEquals(-3, region.minZ(-3).minZ());

        assertEquals(1, region.maxX(1).maxX());
        assertEquals(2, region.maxY(2).maxY());
        assertEquals(3, region.maxZ(3).maxZ());
    }

    @Test
    void setMinMaxInvalid() {
        BlockRegion region = new BlockRegion(new Vector3i());
        assertThrows(IllegalArgumentException.class, () -> region.minX(2));
        assertThrows(IllegalArgumentException.class, () -> region.minY(2));
        assertThrows(IllegalArgumentException.class, () -> region.minZ(2));

        assertThrows(IllegalArgumentException.class, () -> region.maxX(-1));
        assertThrows(IllegalArgumentException.class, () -> region.maxY(-1));
        assertThrows(IllegalArgumentException.class, () -> region.maxZ(-1));
    }

    // -- creation  --------------------------------------------------------------------------------------------------//

    @Test
    void createEmpty() {
        BlockRegion empty = new BlockRegion();

        final ArrayList<Vector3i> blockPositions = Lists.newArrayList(BlockRegions.iterable(empty));

        assertFalse(empty.isValid(), "empty region should be invalid");
        assertEquals(Collections.emptyList(), blockPositions, "empty region should contain no block positions");
    }

    static Stream<Arguments> fromMinAndSizeArgs() {
        return Stream.of(
                Arguments.of(new Vector3i(), new Vector3i(1, 1, 1), new Vector3i()),
                Arguments.of(new Vector3i(1, 1, 1), new Vector3i(3, 3, 3), new Vector3i(3, 3, 3)),
                Arguments.of(new Vector3i(3, 4, 5), new Vector3i(8, 5, 2), new Vector3i(10, 8, 6))
        );
    }

    @ParameterizedTest
    @MethodSource("fromMinAndSizeArgs")
    public void fromMinAndSize(Vector3i min, Vector3i size, Vector3i expectedMax) {
        BlockRegion region = BlockRegions.createFromMinAndSize(min, size);
        BlockRegion expected = new BlockRegion().setMin(min).setSize(size);

        assertEquals(min, region.getMin(new Vector3i()));
        assertEquals(size, region.getSize(new Vector3i()));
        assertEquals(expectedMax, region.getMax(new Vector3i()));
        assertEquals(expected, region);
    }

    @Test
    public void fromMinAndSizeInvalid() {
        assertThrows(IllegalArgumentException.class, () -> BlockRegions.createFromMinAndSize(new Vector3i(),
                new Vector3i(-1, 1, 1)));
        assertThrows(IllegalArgumentException.class, () -> BlockRegions.createFromMinAndSize(new Vector3i(),
                new Vector3i(1, -1, 1)));
        assertThrows(IllegalArgumentException.class, () -> BlockRegions.createFromMinAndSize(new Vector3i(),
                new Vector3i(1, 1, -1)));
    }

    private static Stream<Arguments> fromMinAndMaxArgs() {
        return Stream.of(
                Arguments.of(new Vector3i(), new Vector3i(1, 1, 1), new Vector3i()),
                Arguments.of(new Vector3i(1, 1, 1), new Vector3i(3, 3, 3), new Vector3i(3, 3, 3)),
                Arguments.of(new Vector3i(3, 4, 5), new Vector3i(8, 5, 2), new Vector3i(10, 8, 6))
        );
    }

    @ParameterizedTest
    @MethodSource("fromMinAndMaxArgs")
    public void fromMinAndMax(Vector3i min, Vector3i expectedSize, Vector3i max) {
        BlockRegion region = BlockRegions.fromMinAndMax(min, max);
        assertEquals(min, region.getMin(new Vector3i()), "min");
        assertEquals(max, region.getMax(new Vector3i()), "max");
        assertEquals(expectedSize, region.getSize(new Vector3i()), "size");
    }

    private static Stream<Arguments> createFromMinAndMaxInvalidArgs() {
        return Stream.of(
                Arguments.of(new Vector3i(1, 1, 1), new Vector3i(0, 0, 0)),
                Arguments.of(new Vector3i(0, 1, 0), new Vector3i(1, 0, 1)),
                Arguments.of(new Vector3i(0, 0, 0), new Vector3i(-1, 0, 0))
        );
    }

    @ParameterizedTest
    @MethodSource("createFromMinAndMaxInvalidArgs")
    public void fromMinAndMaxInvalid(Vector3i min, Vector3i max) {
        assertThrows(IllegalArgumentException.class, () -> BlockRegions.fromMinAndMax(min, max));
    }

    static Stream<Arguments> fromCenterAndExtentsInvalidArgs() {
        return Stream.of(
                Arguments.of(new Vector3i(-1, 1, 1)),
                Arguments.of(new Vector3i(1, -1, 1)),
                Arguments.of(new Vector3i(1, 1, -1))
        );
    }

    @ParameterizedTest
    @MethodSource("fromCenterAndExtentsInvalidArgs")
    public void fromCenterAndExtentsInvalid(Vector3i extents) {
        assertThrows(IllegalArgumentException.class, () -> BlockRegions.fromCenterAndExtents(new Vector3i(), extents));
    }

    private static Stream<Arguments> createEncompassingArgs() {
        return Stream.of(
                Arguments.of(new Vector3i(1, 1, 1),
                        Lists.newArrayList(new Vector3i(), new Vector3i())),
                Arguments.of(new Vector3i(3, 3, 3),
                        Lists.newArrayList(new Vector3i(1, 1, 1), new Vector3i(3, 3, 3))),
                Arguments.of(new Vector3i(3, 3, 3),
                        Lists.newArrayList(new Vector3i(3, 3, 3), new Vector3i(1, 1, 1))),
                Arguments.of(new Vector3i(2, 2, 2),
                        Lists.newArrayList(new Vector3i(0, 1, 0), new Vector3i(1, 0, 1))),
                Arguments.of(new Vector3i(2, 3, 4),
                        Lists.newArrayList(new Vector3i(0, 1, 0), new Vector3i(1, 0, 1), new Vector3i(0, -1, 3)))
        );
    }

    @ParameterizedTest
    @MethodSource("createEncompassingArgs")
    public void createEncompassing(Vector3i expectedSize, Collection<Vector3i> positions) {
        Vector3i min = positions.stream().reduce(new Vector3i(Integer.MAX_VALUE), Vector3i::min);
        Vector3i max = positions.stream().reduce(new Vector3i(Integer.MIN_VALUE), Vector3i::max);

        BlockRegion region = BlockRegions.encompassing(positions);
        assertEquals(min, region.getMin(new Vector3i()), "min of " + region);
        assertEquals(max, region.getMax(new Vector3i()), "max of " + region);
        assertEquals(expectedSize, region.getSize(new Vector3i()), "size of " + region);
    }

    // -- iterable  --------------------------------------------------------------------------------------------------//

    @Test
    public void testIterateRegion() {
        Vector3i min = new Vector3i(2, 5, 7);
        Vector3i max = new Vector3i(10, 11, 12);
        BlockRegion region = BlockRegions.fromMinAndMax(min, max);

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
        BlockRegion region1 = BlockRegions.fromMinAndMax(new Vector3i(), new Vector3i(32, 32, 32));
        BlockRegion region2 = BlockRegions.fromMinAndMax(new Vector3i(1, 1, 1), new Vector3i(17, 17, 17));
        assertEquals(region2, new BlockRegion(region1).intersect(region2).get());
    }

    @Test
    public void testNonTouchingIntersect() {
        BlockRegion region1 = BlockRegions.fromMinAndMax(new Vector3i(), new Vector3i(32, 32, 32));
        BlockRegion region2 = BlockRegions.fromMinAndMax(new Vector3i(103, 103, 103), new Vector3i(170, 170,
                170));
        assertEquals(Optional.empty(), new BlockRegion(region1).intersect(region2));
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

    private static Stream<Arguments> testCenterArgs() {
        return Stream.of(
                Arguments.of(
                        new BlockRegion(),
                        new Vector3f(Float.NaN)
                ),
                // creating from min and max
                Arguments.of(
                        BlockRegions.fromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(0, 0, 0)),
                        new Vector3f(0f, 0f, 0f)
                ),
                Arguments.of(
                        BlockRegions.fromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(1, 1, 1)),
                        new Vector3f(.5f, .5f, .5f)
                ),
                Arguments.of(
                        BlockRegions.fromMinAndMax(new Vector3i(-1, -1, -1), new Vector3i(1, 1, 1)),
                        new Vector3f(0f, 0f, 0f)
                ),
                Arguments.of(
                        BlockRegions.fromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(2, 2, 2)),
                        new Vector3f(1f, 1f, 1f)
                ),
                // creating from center and extents
                Arguments.of(
                        BlockRegions.fromCenterAndExtents(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0)),
                        new Vector3f(0f, 0f, 0f)
                ),
                Arguments.of(
                        BlockRegions.fromCenterAndExtents(new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0.5f,
                                0.5f, 0.5f)),
                        new Vector3f(.5f, .5f, .5f)
                ),
                Arguments.of(
                        BlockRegions.fromCenterAndExtents(new Vector3f(0.49f, 0.49f, 0.49f), new Vector3f(0.5f,
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

    // -- contains ---------------------------------------------------------------------------------------------------//

    static Stream<Arguments> containsPointArgs() {
        return Stream.of(
                // positive cases
                Arguments.of(new Vector3f(1.0f, 1.0f, 1.0f), true),
                Arguments.of(new Vector3f(1.2f, 0f, 0f), true),
                Arguments.of(new Vector3f(0f, 1.2f, 0f), true),
                Arguments.of(new Vector3f(0f, 1.2f, 1.2f), true),
                Arguments.of(new Vector3f(1.2f, 1.2f, 0f), true),
                Arguments.of(new Vector3f(1.2f, 1.2f, 1.2f), true),
                // negative cases
                Arguments.of(new Vector3f(1.2f, 0f, -1.2f), false),
                Arguments.of(new Vector3f(0f, 1.2f, -1.2f), false),
                Arguments.of(new Vector3f(1.2f, 1.2f, -1.2f), false),
                Arguments.of(new Vector3f(-1.2f, 0f, 0f), false),
                Arguments.of(new Vector3f(-1.2f, 0f, 1.2f), false),
                Arguments.of(new Vector3f(-1.2f, 0f, -1.2f), false),
                Arguments.of(new Vector3f(0f, -1.2f, 0f), false),
                Arguments.of(new Vector3f(0f, -1.2f, 1.2f), false),
                Arguments.of(new Vector3f(0f, -1.2f, -1.2f), false),
                Arguments.of(new Vector3f(-1.2f, 1.2f, 0f), false),
                Arguments.of(new Vector3f(-1.2f, 1.2f, 1.2f), false),
                Arguments.of(new Vector3f(-1.2f, 1.2f, -1.2f), false),
                Arguments.of(new Vector3f(1.2f, -1.2f, 0f), false),
                Arguments.of(new Vector3f(1.2f, -1.2f, 1.2f), false),
                Arguments.of(new Vector3f(1.2f, -1.2f, -1.2f), false),
                Arguments.of(new Vector3f(-1.2f, -1.2f, 0f), false),
                Arguments.of(new Vector3f(-1.2f, -1.2f, 1.2f), false),
                Arguments.of(new Vector3f(-1.2f, -1.2f, -1.2f), false)
        );
    }

    @ParameterizedTest
    @MethodSource("containsPointArgs")
    public void containsPointPositive(Vector3f point, boolean shouldBeContained) {
        BlockRegion region = BlockRegions.fromMinAndMax(0, 0, 0, 1, 1, 1);

        if (shouldBeContained) {
            assertTrue(region.containsPoint(point), "point should be within region");
        } else {
            assertFalse(region.containsPoint(point), "point should not be within region");
        }
    }

    // ---------------------------------------------------------------------------------------------------------------//

    @Test
    public void testIntersectionPlane() {
        BlockRegion a = BlockRegions.fromMinAndMax(0, 0, 0, 1, 1, 1);
        assertTrue(a.intersectsPlane(1, 1, 1, 1));
        assertFalse(a.intersectsPlane(1, 1, 1, 2));
    }

    @Test
    public void testIntersectionBlockRegion() {
        BlockRegion a = BlockRegions.fromMinAndMax(0, 0, 0, 1, 1, 1);
        BlockRegion b = BlockRegions.fromMinAndMax(1, 1, 1, 4, 4, 4);
        BlockRegion c = BlockRegions.fromMinAndMax(3, 3, 3, 4, 4, 4);

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
        BlockRegion region = BlockRegions.fromMinAndMax(0, 0, 0, 1, 1, 1);
        assertEquals(intersects, region.intersectsAABB(aabb));
    }

    @Test
    public void testIntersectionSphere() {
        BlockRegion a = BlockRegions.fromMinAndMax(0, 0, 0, 1, 1, 1);
        Spheref s1 = new Spheref(0, 0, 1, 2);
        Spheref s2 = new Spheref(3, 3, 3, 1);

        assertTrue(a.intersectsSphere(s1));
        assertTrue(a.intersectsSphere(2, 2, 2, 1));
        assertFalse(a.intersectsSphere(s2));
        assertFalse(a.intersectsSphere(2, 2, 2, 0.25f));
    }

    @Test
    public void testIntersectionRay() {
        BlockRegion a = BlockRegions.fromMinAndMax(0, 0, 0, 1, 1, 1);
        Rayf r1 = new Rayf(0, 0, 3, 1, 1, -2);
        Rayf r2 = new Rayf(0, 2, 2, 1, 0, 0);

        assertTrue(a.intersectsRay(r1));
        assertFalse(a.intersectsRay(r2));
        assertTrue(a.intersectsRay(1.2f, 0, 0, 1, 0, 0));
        assertFalse(a.intersectsRay(0, 0, 3, 1, 1, -1));
    }

    @Test
    void testIntersectionLineSegment() {
        BlockRegion a = BlockRegions.fromMinAndMax(0, 0, 0, 1, 1, 1);

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

    static Stream<Arguments> getBoundsArgs() {
        return Stream.of(
                Arguments.of(
                        BlockRegions.fromMinAndMax(new Vector3i(1, 1, 1), new Vector3i(2, 3, 4)),
                        new AABBf(.5f, .5f, .5f, 2.5f, 3.5f, 4.5f)
                ),
                Arguments.of(
                        BlockRegions.fromMinAndMax(-1, -1, -1, 1, 1, 1),
                        new AABBf(-1.5f, -1.5f, -1.5f, 1.5f, 1.5f, 1.5f)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getBoundsArgs")
    void getBounds(BlockRegion region, AABBf bounds) {
        assertEquals(bounds, region.getBounds(new AABBf()));
    }

    static Stream<Arguments> copyRegionArgs() {
        return Stream.of(
                Arguments.of((Function<BlockRegion, BlockRegion>) region -> new BlockRegion(region)),
                Arguments.of((Function<BlockRegion, BlockRegion>) region -> region.copy()),
                Arguments.of((Function<BlockRegion, BlockRegion>) region -> new BlockRegion(0, 0, 0).set(region))
        );
    }

    @ParameterizedTest
    @MethodSource("copyRegionArgs")
    void copyRegion(Function<BlockRegion, BlockRegion> copyFn) {
        BlockRegion original = BlockRegions.encompassing(new Vector3i(1, 1, 1), new Vector3i(2, 2, 2));

        BlockRegion source = BlockRegions.encompassing(new Vector3i(1, 1, 1), new Vector3i(2, 2, 2));
        BlockRegion copy = copyFn.apply(source);

        assertEquals(original, copy);

        copy.setMax(2, 3, 4);
        assertEquals(original, source, "source should not be modified");
        assertEquals(new Vector3i(2, 3, 4), copy.getMax(new Vector3i()));
    }

    // -- extend -----------------------------------------------------------------------------------------------------//

    static Stream<Arguments> extendFloatArgs() {
        return Stream.of(
                Arguments.of(.1f, .1f, .1f, BlockRegions.fromMinAndMax(new Vector3i(), new Vector3i(3, 3, 3))),
                Arguments.of(-.1f, -.1f, -.1f, BlockRegions.fromMinAndMax(new Vector3i(1, 1, 1), new Vector3i(2
                        , 2, 2))),
                Arguments.of(1f, 1f, 1f, BlockRegions.fromMinAndMax(new Vector3i(-1, -1, -1), new Vector3i(4, 4
                        , 4))),
                Arguments.of(-1f, -1f, -1f, BlockRegions.fromMinAndMax(new Vector3i(1, 1, 1), new Vector3i(2, 2
                        , 2))),
                Arguments.of(1.9f, 1.9f, 1.9f, BlockRegions.fromMinAndMax(new Vector3i(-1, -1, -1),
                        new Vector3i(4, 4, 4)))
        );
    }

    @ParameterizedTest
    @MethodSource("extendFloatArgs")
    void extendFloat(float x, float y, float z, BlockRegion expected) {
        final BlockRegion region = BlockRegions.fromMinAndMax(new Vector3i(), new Vector3i(3, 3, 3));
        assertEquals(expected, region.copy().extend(x, y, z));
    }

    void extend(int x, int y, int z) {
        BlockRegion region = new BlockRegion(0, 0, 0, 1, 1, 1);
    }

    static Stream<Arguments> extendInvalidArgs() {
        return Stream.of(
                Arguments.of(new Vector3i(-1, 0, 0)),
                Arguments.of(new Vector3i(0, -1, 0)),
                Arguments.of(new Vector3i(0, 0, -1)),
                Arguments.of(new Vector3i(-1, -1, -1))
        );
    }

    @ParameterizedTest
    @MethodSource("extendInvalidArgs")
    void extendInvalid(Vector3i extents) {
        BlockRegion region = new BlockRegion(0, 0, 0, 1, 1, 1);

        assertThrows(IllegalArgumentException.class, () -> region.extend(extents));
        assertThrows(IllegalArgumentException.class, () -> region.extend(extents.x(), extents.y(), extents.z()));
    }

    // -- union ------------------------------------------------------------------------------------------------------//

    static Stream<Arguments> unionArgs() {
        return Stream.of(
                Arguments.of(new Vector3i(-2, 4, -16), new Vector3i(4, 107, 0)),
                Arguments.of(new Vector3i(4, 4, -16), new Vector3i(-2, 107, 0)),
                Arguments.of(new Vector3i(-2, 107, -16), new Vector3i(4, 4, 0)),
                Arguments.of(new Vector3i(-2, 4, 0), new Vector3i(4, 107, -16)),
                Arguments.of(new Vector3i(4, 107, -16), new Vector3i(-2, 4, 0)),
                Arguments.of(new Vector3i(4, 4, 0), new Vector3i(-2, 107, -16)),
                Arguments.of(new Vector3i(-2, 107, 0), new Vector3i(4, 4, -16)),
                Arguments.of(new Vector3i(4, 107, 0), new Vector3i(-2, 4, -16))
        );
    }

    @ParameterizedTest
    @MethodSource("unionArgs")
    public void union(Vector3i vec1, Vector3i vec2) {
        BlockRegion expected =
                BlockRegions.fromMinAndMax(new Vector3i(-2, 4, -16), new Vector3i(4, 107, 0));

        assertEquals(expected, new BlockRegion(vec1).union(vec2));
        assertEquals(expected, new BlockRegion(vec2).union(vec1));
    }

    static Stream<Arguments> unionWithRegionArgs() {
        return Stream.of(
                Arguments.of(
                        new BlockRegion(0, 0, 0),
                        new BlockRegion(0, 0, 0),
                        new BlockRegion(0, 0, 0)
                ),
                Arguments.of(
                        new BlockRegion(-1, -1, -1, 1, 1, 1),
                        new BlockRegion(2, 2, 2, 3, 4, 5),
                        new BlockRegion(-1, -1, -1, 3, 4, 5)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("unionWithRegionArgs")
    public void unionWithRegion(BlockRegion a, BlockRegion b, BlockRegion expected) {
        assertEquals(expected, a.copy().union(b));
        assertEquals(expected, b.copy().union(a));
    }

    @Test
    public void unionWithBlockEntity() {
        Vector3i pos = new Vector3i(1, 2, 3);
        final EntityRef entity = new PojoEntityManager().create(new BlockComponent(new Block(), pos));

        BlockRegion region = new BlockRegion().union(entity);
        assertTrue(region.containsBlock(pos));
        assertEquals(Collections.singletonList(pos), Lists.newArrayList(BlockRegions.iterable(region)));
    }

    @Test
    public void unionWithNonBlockEntity() {
        final EntityRef entity = new PojoEntityManager().create();
        BlockRegion region = new BlockRegion().union(entity);
        assertFalse(region.isValid());
    }

    // -- translate --------------------------------------------------------------------------------------------------//

    @Test
    public void translate() {
        BlockRegion region = new BlockRegion(0, 0, 0, 1, 1, 1);
        Vector3i translation = new Vector3i(1, 2, 3);

        assertEquals(new BlockRegion(1, 2, 3, 2, 3, 4), region.copy().translate(translation));
        assertEquals(region, region.copy().translate(translation).translate(translation.negate(new Vector3i())));
    }
}
