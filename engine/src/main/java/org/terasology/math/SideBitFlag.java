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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gnu.trove.map.TObjectByteMap;
import gnu.trove.map.hash.TObjectByteHashMap;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for representing a set of sides as a byte
 *
 */
public final class SideBitFlag {
    private static TObjectByteMap<Side> sideBits = new TObjectByteHashMap<>();

    static {
        sideBits.put(Side.TOP, (byte) 0b000001);
        sideBits.put(Side.LEFT, (byte) 0b000010);
        sideBits.put(Side.FRONT, (byte) 0b000100);
        sideBits.put(Side.BOTTOM, (byte) 0b001000);
        sideBits.put(Side.RIGHT, (byte) 0b010000);
        sideBits.put(Side.BACK, (byte) 0b100000);
    }

    private SideBitFlag() {
    }

    public static byte getReverse(byte sides) {
        return (byte) ((sides / 8) + ((sides % 8) * 8));
    }

    public static byte getSides(Set<Side> sides) {
        byte result = 0;
        for (Side side : sides) {
            result += sideBits.get(side);
        }
        return result;
    }

    public static byte getSides(Side... sides) {
        byte result = 0;
        for (Side side : sides) {
            final byte sideBit = sideBits.get(side);
            if ((result & sideBit) > 0) {
                throw new IllegalArgumentException("Cannot have multiples of the same side");
            }
            result += sideBit;
        }
        return result;
    }

    public static byte getSide(Side side) {
        return sideBits.get(side);
    }

    public static EnumSet<Side> getSides(final byte sidesBit) {
        final List<Side> result = Lists.newArrayList();
        sideBits.forEachEntry(
                (a, b) -> {
                    if ((b & sidesBit) > 0) {
                        result.add(a);
                    }

                    return true;
                });
        return Sets.newEnumSet(result, Side.class);
    }

    public static boolean hasSide(byte sideBit, Side side) {
        return (sideBit & sideBits.get(side)) > 0;
    }

    public static byte addSide(byte sideBit, Side... sides) {
        byte result = sideBit;
        for (Side side : sides) {
            result |= sideBits.get(side);
        }

        return result;
    }

}
