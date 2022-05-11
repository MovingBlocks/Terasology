// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SideTest {

    @Test
    public void testSideInDirection() {
        for (Side side : Side.values()) {
            assertEquals(side, Side.inDirection(side.direction().x(), side.direction().y(), side.direction().z()));
        }
    }

    @Test
    public void testRelativeSides() {
        Side side = Side.FRONT;
        assertEquals(Side.LEFT, side.getRelativeSide(Direction.LEFT));
        assertEquals(Side.RIGHT, side.getRelativeSide(Direction.RIGHT));
        assertEquals(Side.TOP, side.getRelativeSide(Direction.UP));
        assertEquals(Side.BOTTOM, side.getRelativeSide(Direction.DOWN));
        assertEquals(Side.FRONT, side.getRelativeSide(Direction.FORWARD));
        assertEquals(Side.BACK, side.getRelativeSide(Direction.BACKWARD));
    }

    @Test
    public void testGetFlag() {
        Assertions.assertEquals(0x01, Side.TOP.getFlag());
        Assertions.assertEquals(0x02, Side.LEFT.getFlag());
        Assertions.assertEquals(0x04, Side.FRONT.getFlag());
        Assertions.assertEquals(0x08, Side.BOTTOM.getFlag());
        Assertions.assertEquals(0x10, Side.RIGHT.getFlag());
        Assertions.assertEquals(0x20, Side.BACK.getFlag());
    }

    @Test
    public void testToFlags() {
        Assertions.assertEquals(Side.TOP.getFlag() | Side.LEFT.getFlag(), Side.toFlags(Side.TOP, Side.LEFT));
        Assertions.assertEquals(Side.TOP.getFlag() | Side.RIGHT.getFlag(), Side.toFlags(Side.TOP, Side.RIGHT));
        Assertions.assertEquals(Side.TOP.getFlag(), Side.toFlags(Side.TOP, Side.TOP));
        Assertions.assertEquals(Side.BACK.getFlag() | Side.BOTTOM.getFlag(), Side.toFlags(Side.BACK, Side.BOTTOM));

        Assertions.assertEquals(
                Side.TOP.getFlag() | Side.LEFT.getFlag() | Side.FRONT.getFlag() | Side.BOTTOM.getFlag() | Side.RIGHT.getFlag() | Side.BACK.getFlag(),
                Side.toFlags(Side.TOP, Side.LEFT, Side.FRONT, Side.BOTTOM, Side.RIGHT, Side.BACK));
    }
}
