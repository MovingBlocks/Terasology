// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.math;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions;

public class SideTest {

    @Test
    public void testGetFlag() {
        Assertions.assertEquals(Side.TOP.getFlag(), 0x01);
        Assertions.assertEquals(Side.LEFT.getFlag(), 0x02);
        Assertions.assertEquals(Side.FRONT.getFlag(), 0x04);
        Assertions.assertEquals(Side.BOTTOM.getFlag(), 0x08);
        Assertions.assertEquals(Side.RIGHT.getFlag(), 0x10);
    }


    @Test
    public void testToFlags() {
        Assertions.assertEquals(Side.toFlags(Side.TOP, Side.LEFT), Side.TOP.getFlag() | Side.LEFT.getFlag());
        Assertions.assertEquals(Side.toFlags(Side.TOP, Side.RIGHT), Side.TOP.getFlag() | Side.RIGHT.getFlag());
        Assertions.assertEquals(Side.toFlags(Side.TOP, Side.TOP), Side.TOP.getFlag());
        Assertions.assertEquals(Side.toFlags(Side.BACK, Side.BOTTOM), Side.BACK.getFlag() | Side.BOTTOM.getFlag());

        Assertions.assertEquals(Side.toFlags(Side.TOP, Side.LEFT, Side.FRONT, Side.BOTTOM, Side.RIGHT, Side.BACK), Side.TOP.getFlag() | Side.LEFT.getFlag() | Side.FRONT.getFlag() | Side.BOTTOM.getFlag() | Side.RIGHT.getFlag() | Side.BACK.getFlag());
    }
}
