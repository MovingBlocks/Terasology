// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import org.joml.Vector2i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockAreaTest {

    @Test
    public void containsInvalid() {
        BlockArea a = new BlockArea(BlockArea.INVALID);
        assertFalse(a.contains(1, 0));
        assertFalse(a.contains(0, 1));
        assertFalse(a.contains(1, 1));
        assertFalse(a.contains(-1, 0));
        assertFalse(a.contains(0, -1));
        assertFalse(a.contains(-1, 1));
        assertFalse(a.contains(1, -1));
        assertFalse(a.contains(-1, -1));
    }

    @Test
    public void testContains() {
        BlockArea a = new BlockArea(1, 2, 3, 4);
        assertTrue(a.contains(1, 2));
        assertTrue(a.contains(2, 3));
        assertTrue(a.contains(2, 4));
        assertFalse(a.contains(4, 3));
        assertFalse(a.contains(2, 5));
        assertFalse(a.contains(3, 5));
    }

    @Test
    public void testContainsBlockRegion() {
        BlockArea a = new BlockArea(1, 2, 10, 20);

        assertTrue(a.contains(new BlockArea(5, 5, 5, 5)));
        assertFalse(a.contains(new BlockArea(11, 5, 35, 5)));
        assertFalse(a.contains(new BlockArea(1, 21, 5, 95)));

        assertTrue(a.contains(new BlockArea(1, 2, 3, 3)));
        assertTrue(a.contains(new BlockArea(4, 2, 8, 8)));
        assertTrue(a.contains(new BlockArea(1, 4, 8, 8)));
        assertTrue(a.contains(new BlockArea(5, 12, 9, 19)));
        assertTrue(a.contains(new BlockArea(5, 12, 10, 20)));
        assertFalse(a.contains(new BlockArea(5, 12, 10, 21)));
        assertFalse(a.contains(new BlockArea(5, 12, 11, 20)));
    }

    @Test
    public void textExtents() {
        BlockArea area = new BlockArea(2, 1, 10, 20).expand(3, 4);
        assertEquals(new Vector2i(-1, -3), area.getMin(new Vector2i()));
        assertEquals(new Vector2i(13, 24), area.getMax(new Vector2i()));
    }

    @Test
    public void testInvalidExtents() {
        final BlockArea area = new BlockArea(0, 0, 3, 3);
        assertThrows(IllegalArgumentException.class, () -> area.expand(-2, 1));
        assertThrows(IllegalArgumentException.class, () -> area.expand(1, -2));
        assertThrows(IllegalArgumentException.class, () -> area.expand(-2, -2));
    }

    @Test
    public void testInvalidMinMax() {
        assertThrows(IllegalArgumentException.class, () -> new BlockArea(0, 3).setMax(3, 0));
        assertThrows(IllegalArgumentException.class, () -> new BlockArea(3, 0).setMin(0, 3));
        assertThrows(IllegalArgumentException.class, () -> new BlockArea(0, 3, 3, 0));
    }

    static Stream<Arguments> testIntersectionWithAreaArgs() {
        return Stream.of(
                Arguments.of(
                        new BlockArea(0, 0, 2, 2),
                        new BlockArea(0, 0, 2, 2),
                        new BlockArea(0, 0, 2, 2)
                ),
                Arguments.of(
                        new BlockArea(0, 0, 2, 2),
                        new BlockArea(1, 1, 3, 3),
                        new BlockArea(1, 1, 2, 2)
                ),
                Arguments.of(
                        new BlockArea(0, 0, 2, 2),
                        new BlockArea(0, 2, 3, 3),
                        new BlockArea(0, 2, 2, 2)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testIntersectionWithAreaArgs")
    public void testIntersectionWithArea(BlockArea a, BlockArea b, BlockArea expected) {
        assertEquals(Optional.of(expected), a.intersect(b, new BlockArea()));
    }

    static Stream<Arguments> testIntersectsAreaArgs() {
        return Stream.of(
                // positive cases
                Arguments.of(new BlockArea(0, 0, 3, 3), true),
                Arguments.of(new BlockArea(-1, -1, 4, 4), true),
                Arguments.of(new BlockArea(-1, -1, 1, 1), true),
                Arguments.of(new BlockArea(1, 1, 2, 2), true),
                Arguments.of(new BlockArea(3, 1, 4, 2), true),
                Arguments.of(new BlockArea(1, 3, 2, 4), true),
                // negative cases
                Arguments.of(new BlockArea(5, 5, 6, 6), false),
                Arguments.of(new BlockArea(-2, -2, -1, -1), false),
                Arguments.of(new BlockArea(), false)
        );
    }

    @ParameterizedTest
    @MethodSource("testIntersectsAreaArgs")
    public void testIntersectsArea(BlockArea other, boolean intersecting) {
        BlockArea area = new BlockArea(0, 0, 3, 3);
        assertEquals(intersecting, area.intersectsBlockArea(other));
    }
}
