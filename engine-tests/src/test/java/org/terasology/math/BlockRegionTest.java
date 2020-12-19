// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.math;

import com.google.common.collect.Sets;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegions;

import java.util.Arrays;
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
    public void createFromMinAndMaxArgs(Vector3i min, Vector3i expectedSize, Vector3i max) {
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
        BlockRegion region1 = BlockRegions.createFromMinAndMax(new Vector3i(), new Vector3i(32, 32, 32));
        BlockRegion region2 = BlockRegions.createFromMinAndMax(new Vector3i(1, 1, 1), new Vector3i(17, 17, 17));
        assertEquals(region2, region1.intersection(region2, new BlockRegion()));
    }

    @Test
    public void testNonTouchingIntersect() {
        BlockRegion region1 = BlockRegions.createFromMinAndMax(new Vector3i(), new Vector3i(32, 32, 32));
        BlockRegion region2 = BlockRegions.createFromMinAndMax(new Vector3i(103, 103, 103), new Vector3i(170, 170, 170));
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
    public void testFace() {
        BlockRegion region = BlockRegions.createFromMinAndMax(new Vector3i(), new Vector3i(32, 32, 32));
        assertEquals(BlockRegions.createFromMinAndMax(new Vector3i(),
            new Vector3i(0, 32, 32)), region.blockFace(Side.LEFT, new BlockRegion()));
        assertEquals(BlockRegions.createFromMinAndMax(new Vector3i(32, 0,
            0), new Vector3i(32, 32, 32)), region.blockFace(Side.RIGHT, new BlockRegion()));
        assertEquals(BlockRegions.createFromMinAndMax(new Vector3i(0, 0,
            0), new Vector3i(32, 32, 0)), region.blockFace(Side.FRONT, new BlockRegion()));
        assertEquals(BlockRegions.createFromMinAndMax(new Vector3i(0, 0,
            32), new Vector3i(32, 32, 32)), region.blockFace(Side.BACK, new BlockRegion()));
        assertEquals(BlockRegions.createFromMinAndMax(new Vector3i(),
            new Vector3i(32, 0, 32)), region.blockFace(Side.BOTTOM, new BlockRegion()));
        assertEquals(BlockRegions.createFromMinAndMax(new Vector3i(0, 32,
            0), new Vector3i(32, 32, 32)), region.blockFace(Side.TOP, new BlockRegion()));
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
                        new Vector3f(0.5f, 0.5f, 0.5f)
                ),
                Arguments.of(
                        BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(1, 1, 1)),
                        new Vector3f(1f, 1f, 1f)
                ),
                Arguments.of(
                        BlockRegions.createFromMinAndMax(new Vector3i(-1, -1, -1), new Vector3i(1, 1, 1)),
                        new Vector3f(0.5f, 0.5f, 0.5f)
                ),
                Arguments.of(
                        BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(2, 2, 2)),
                        new Vector3f(1.5f, 1.5f, 1.5f)
                ),
                // creating from center and extents
                Arguments.of(
                        BlockRegions.createFromCenterAndExtents(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0)),
                        new Vector3f(0.5f, 0.5f, 0.5f)
                ),
                Arguments.of(
                        BlockRegions.createFromCenterAndExtents(new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0.5f,
                                0.5f, 0.5f)),
                        new Vector3f(1f, 1f, 1f)
                ),
                Arguments.of(
                        BlockRegions.createFromCenterAndExtents(new Vector3f(0.49f, 0.49f, 0.49f), new Vector3f(0.5f,
                                0.5f, 0.5f)),
                        new Vector3f(.5f, .5f, .5f)
                )

        );
    }

    @ParameterizedTest
    @MethodSource("testCenterArgs")
    public void testCenter(BlockRegion region, Vector3fc expectedCenter) {
        assertEquals(expectedCenter, region.center(new Vector3f()));
    }
}
