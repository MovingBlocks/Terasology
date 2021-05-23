// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.math;

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
    private static final TObjectByteMap<Side> sideBits = new TObjectByteHashMap<>();

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
