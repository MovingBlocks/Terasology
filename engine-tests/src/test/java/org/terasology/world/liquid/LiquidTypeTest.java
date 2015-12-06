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

package org.terasology.world.liquid;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 */
public class LiquidTypeTest {

    @Test
    public void byteToLiquidType() {
        for (int i = 0; i < 8; i++) {
            assertEquals(LiquidType.WATER, LiquidType.getTypeForByte((byte) i));
        }
        for (int i = 8; i < 16; ++i) {
            assertEquals(LiquidType.LAVA, LiquidType.getTypeForByte((byte) i));
        }
    }

    @Test
    public void convertToByte() {
        assertEquals(0, LiquidType.WATER.convertToByte((byte) 0));
        assertEquals(4, LiquidType.WATER.convertToByte((byte) 4));
        assertEquals(7, LiquidType.WATER.convertToByte((byte) 7));
        assertEquals(9, LiquidType.LAVA.convertToByte((byte) 1));
        assertEquals(12, LiquidType.LAVA.convertToByte((byte) 4));
    }
}
