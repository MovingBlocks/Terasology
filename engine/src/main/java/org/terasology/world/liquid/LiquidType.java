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

import org.terasology.module.sandbox.API;

/**
 */
@API
public enum LiquidType {
    WATER(0),
    LAVA(1);

    private static LiquidType[] liquidLookup = new LiquidType[2];
    private byte typeValue;


    static {
        liquidLookup = new LiquidType[LiquidType.values().length];
        for (LiquidType liquid : LiquidType.values()) {
            liquidLookup[liquid.getTypeValue()] = liquid;
        }
    }

    private LiquidType(int typeVal) {
        typeValue = (byte) typeVal;
    }

    public byte getTypeValue() {
        return typeValue;
    }

    public byte convertToByte(byte depth) {
        return (byte) ((typeValue << 3) + depth);
    }

    public static LiquidType getTypeForByte(byte liquidByte) {
        return liquidLookup[(0x8 & liquidByte) >> 3];
    }
}
