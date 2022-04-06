// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.math;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for representing a set of sides as a byte
 *
 */
public final class SideBitFlag {

    private SideBitFlag() {
    }

    /**
     * @deprecated Use {@link Side#reversSides(byte)} instead
     */
    @Deprecated
    public static byte getReverse(byte sides) {
        return (byte) ((sides / 8) + ((sides % 8) * 8));
    }

    /**
     * @deprecated Use {@link Side#toFlags(Set)} instead
     */
    @Deprecated
    public static byte getSides(Set<Side> sides) {
        byte result = 0;
        for (Side side : sides) {
            result |= side.getFlag();
        }
        return result;
    }

    /**
     * @deprecated Use {@link Side#toFlags(Side...)} instead
     */
    @Deprecated
    public static byte getSides(Side... sides) {
        byte result = 0;
        for (Side side : sides) {
            result |= side.getFlag();
        }
        return result;
    }

    /**
     * @deprecated Use {@link Side#getFlag()} instead
     */
    @Deprecated
    public static byte getSide(Side side) {
        return side.getFlag();
    }

    /**
     * @deprecated Use {@link Side#getSides(byte)} instead
     */
    @Deprecated
    public static EnumSet<Side> getSides(final byte sidesBit) {
        final List<Side> result = new ArrayList<>(Side.allSides().size());
        for (Side side : Side.allSides()) {
            if ((side.getFlag() & sidesBit) > 0) {
                result.add(side);
            }
        }
        return Sets.newEnumSet(result, Side.class);
    }

    /**
     * @deprecated Use {@link Side#hasFlag(byte)} instead
     */
    @Deprecated
    public static boolean hasSide(byte sideBit, Side side) {
        return  (side.getFlag() & sideBit) > 0;
    }

    /**
     * @deprecated Use {@link Side#setFlags(byte, Side...)} instead
     */
    @Deprecated
    public static byte addSide(byte sideBit, Side... sides) {
        byte result = sideBit;
        for (Side side : sides) {
            result |= side.getFlag();
        }
        return result;
    }

}
