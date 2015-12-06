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
 * Describes the liquid state of a single block
 *
 */
@API
public class LiquidData {
    public static final byte MAX_LIQUID_DEPTH = 0x07;
    private static final byte LIQUID_DEPTH_FILTER = 0x07;

    private LiquidType type;
    private byte depth;

    public LiquidData() {
        type = LiquidType.WATER;
        depth = (byte) 0;
    }

    public LiquidData(LiquidType type, byte depth) {
        this.type = type;
        this.depth = depth;
    }

    public LiquidData(LiquidType type, int depth) {
        this.type = type;
        this.depth = (byte) depth;
    }

    public LiquidData(byte rawData) {
        this.type = LiquidType.getTypeForByte(rawData);
        this.depth = (byte) (rawData & LIQUID_DEPTH_FILTER);
    }

    public LiquidType getType() {
        return type;
    }

    public byte getDepth() {
        return depth;
    }

    public byte toByte() {
        return type.convertToByte(depth);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof LiquidData) {
            LiquidData other = (LiquidData) o;
            if (depth == other.depth) {
                return (depth == 0 || type == other.type);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return depth;
    }

    @Override
    public String toString() {
        if (depth > 0) {
            return type + "(" + depth + ")";
        }
        return "DRY";
    }
}
