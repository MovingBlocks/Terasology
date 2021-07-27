// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.math;

import org.junit.jupiter.api.Test;
import org.terasology.engine.math.Direction;
import org.terasology.engine.math.Side;

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
}
