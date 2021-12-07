// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SideBitTest {

    @Test
    public void testSideToBits() {
        Assertions.assertEquals(SideBitFlag.getSides(Side.TOP), (byte) 0b000001);
        Assertions.assertEquals(SideBitFlag.getSides(Side.LEFT), (byte) 0b000010);
        Assertions.assertEquals(SideBitFlag.getSides(Side.FRONT), (byte) 0b000100);
        Assertions.assertEquals(SideBitFlag.getSides(Side.BOTTOM), (byte) 0b001000);
        Assertions.assertEquals(SideBitFlag.getSides(Side.RIGHT), (byte) 0b010000);
        Assertions.assertEquals(SideBitFlag.getSides(Side.BACK), (byte) 0b100000);

        Assertions.assertEquals(SideBitFlag.getSides(Side.BACK, Side.BOTTOM), (byte) (0b100000 | 0b001000));
        Assertions.assertEquals(SideBitFlag.getSides(Side.BACK, Side.RIGHT), (byte) (0b100000 | 0b010000));

        Assertions.assertEquals(SideBitFlag.getSides(Side.BACK, Side.RIGHT, Side.TOP, Side.TOP), (byte) (0b100000 | 0b010000 | 0b000001));
    }

    @Test
    public void testReverseBits() {
        Assertions.assertEquals(SideBitFlag.getReverse(SideBitFlag.getSides(Side.TOP)), SideBitFlag.getSide(Side.BOTTOM));
        Assertions.assertEquals(SideBitFlag.getReverse(SideBitFlag.getSides(Side.LEFT)), SideBitFlag.getSide(Side.RIGHT));
        Assertions.assertEquals(SideBitFlag.getReverse(SideBitFlag.getSides(Side.FRONT)), SideBitFlag.getSide(Side.BACK));
        Assertions.assertEquals(SideBitFlag.getReverse(SideBitFlag.getSides(Side.BOTTOM)), SideBitFlag.getSide(Side.TOP));
        Assertions.assertEquals(SideBitFlag.getReverse(SideBitFlag.getSides(Side.RIGHT)), SideBitFlag.getSide(Side.LEFT));
        Assertions.assertEquals(SideBitFlag.getReverse(SideBitFlag.getSides(Side.BACK)), SideBitFlag.getSide(Side.FRONT));

        Assertions.assertEquals(SideBitFlag.getReverse(SideBitFlag.getSides(Side.TOP, Side.BOTTOM)), SideBitFlag.getSides(Side.BOTTOM,
                Side.TOP));
        Assertions.assertEquals(SideBitFlag.getReverse(SideBitFlag.getSides(Side.LEFT, Side.RIGHT)), SideBitFlag.getSides(Side.LEFT,
                Side.RIGHT));
        Assertions.assertEquals(SideBitFlag.getReverse(SideBitFlag.getSides(Side.FRONT, Side.BACK)), SideBitFlag.getSides(Side.FRONT,
                Side.BACK));
        Assertions.assertEquals(SideBitFlag.getReverse(SideBitFlag.getSides(Side.BOTTOM, Side.TOP)), SideBitFlag.getSides(Side.BOTTOM,
                Side.TOP));
        Assertions.assertEquals(SideBitFlag.getReverse(SideBitFlag.getSides(Side.RIGHT, Side.LEFT)), SideBitFlag.getSides(Side.RIGHT,
                Side.LEFT));
        Assertions.assertEquals(SideBitFlag.getReverse(SideBitFlag.getSides(Side.BACK, Side.FRONT)), SideBitFlag.getSides(Side.BACK,
                Side.FRONT));
    }

    @Test
    public void testAddSide() {
        Assertions.assertEquals(SideBitFlag.addSide(SideBitFlag.getSides(Side.BACK, Side.BOTTOM), Side.TOP),
                SideBitFlag.getSides(Side.BACK, Side.BOTTOM, Side.TOP));
        Assertions.assertEquals(SideBitFlag.addSide(SideBitFlag.getSides(Side.BACK, Side.BOTTOM), Side.RIGHT),
                SideBitFlag.getSides(Side.BACK, Side.BOTTOM, Side.RIGHT));
        Assertions.assertEquals(SideBitFlag.addSide(SideBitFlag.getSides(Side.BACK, Side.BOTTOM, Side.TOP), Side.TOP),
                SideBitFlag.getSides(Side.BACK, Side.BOTTOM, Side.TOP));
    }
}
