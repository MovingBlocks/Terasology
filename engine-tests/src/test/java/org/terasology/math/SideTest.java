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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 */
public class SideTest {

    @Test
    public void testSideInDirection() {
        for (Side side : Side.getAllSides()) {
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
