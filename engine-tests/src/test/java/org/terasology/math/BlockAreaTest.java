// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.math;

import org.joml.Vector2i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreas;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockAreaTest {

    @Test
    public void testContainsEmpty() {
        BlockArea a = BlockAreas.fromMinAndMax(0, 0, 0, 0);
        assertTrue(a.containsPoint(0, 0));

        assertFalse(a.containsPoint(1, 0));
        assertFalse(a.containsPoint(0, 1));
        assertFalse(a.containsPoint(1, 1));
        assertFalse(a.containsPoint(-1, 0));
        assertFalse(a.containsPoint(0, -1));
        assertFalse(a.containsPoint(-1, 1));
        assertFalse(a.containsPoint(1, -1));
        assertFalse(a.containsPoint(-1, -1));
    }

    @Test
    public void testContainsPoint() {
        BlockArea a = BlockAreas.fromMinAndMax(1, 2, 3, 4);
        assertTrue(a.containsPoint(1, 2));
        assertTrue(a.containsPoint(2, 3));
        assertTrue(a.containsPoint(2, 4));
        assertFalse(a.containsPoint(4, 3));
        assertFalse(a.containsPoint(2, 5));
        assertFalse(a.containsPoint(3, 5));
    }

    @Test
    public void testContainsBlockRegion() {
        BlockArea a = BlockAreas.fromMinAndMax(1, 2, 10, 20);

        assertTrue(a.containsArea(BlockAreas.fromMinAndMax(5, 5, 5, 5)));
        assertFalse(a.containsArea(BlockAreas.fromMinAndMax(11, 5, 35, 5)));
        assertFalse(a.containsArea(BlockAreas.fromMinAndMax(1, 21, 5, 95)));

        assertTrue(a.containsArea(BlockAreas.fromMinAndMax(1, 2, 3, 3)));
        assertTrue(a.containsArea(BlockAreas.fromMinAndMax(4, 2, 8, 8)));
        assertTrue(a.containsArea(BlockAreas.fromMinAndMax(1, 4, 8, 8)));
        assertTrue(a.containsArea(BlockAreas.fromMinAndMax(5, 12, 9, 19)));
        assertTrue(a.containsArea(BlockAreas.fromMinAndMax(5, 12, 10, 20)));
        assertFalse(a.containsArea(BlockAreas.fromMinAndMax(5, 12, 10, 21)));
        assertFalse(a.containsArea(BlockAreas.fromMinAndMax(5, 12, 11, 20)));
    }

    @Test
    public void textExtents() {
        BlockArea area = BlockAreas.fromMinAndMax(2, 1, 10, 20).addExtents(3, 4);
        assertEquals(new Vector2i(-1, -3), area.getMin(new Vector2i()));
        assertEquals(new Vector2i(13, 24), area.getMax(new Vector2i()));
    }

    @Test
    public void testInvalidExtents() {
        final BlockArea area = BlockAreas.fromMinAndMax(0, 0, 3, 3);
        assertThrows(IllegalArgumentException.class, () -> area.addExtents(-2, 1));
        assertThrows(IllegalArgumentException.class, () -> area.addExtents(1, -2));
        assertThrows(IllegalArgumentException.class, () -> area.addExtents(-2, -2));
    }

    @Test
    public void testInvalidMinMax() {
        assertThrows(IllegalArgumentException.class, () -> new BlockArea().setMin(0, 3).setMax(3, 0));
        assertThrows(IllegalArgumentException.class, () -> new BlockArea().setMax(3, 0).setMin(0, 3));
        assertThrows(IllegalArgumentException.class, () -> BlockAreas.fromMinAndMax(0, 3, 3, 0));
    }

    static Stream<Arguments> testIntersectionArgs() {
        return Stream.of(
                Arguments.of(
                        BlockAreas.fromMinAndMax(0, 0, 2, 2),
                        BlockAreas.fromMinAndMax(0, 0, 2, 2),
                        BlockAreas.fromMinAndMax(0, 0, 2, 2)
                ),
                Arguments.of(
                        BlockAreas.fromMinAndMax(0, 0, 2, 2),
                        BlockAreas.fromMinAndMax(1, 1, 3, 3),
                        BlockAreas.fromMinAndMax(1, 1, 2, 2)
                ),
                Arguments.of(
                        BlockAreas.fromMinAndMax(0, 0, 2, 2),
                        BlockAreas.fromMinAndMax(0, 2, 3, 3),
                        BlockAreas.fromMinAndMax(0, 2, 2, 2)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testIntersectionArgs")
    public void testIntersection(BlockArea a, BlockArea b, BlockArea expected) {
        assertEquals(expected, a.intersection(b, new BlockArea()));
    }
}
