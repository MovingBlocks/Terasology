package org.terasology.math;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gnu.trove.iterator.TObjectByteIterator;
import gnu.trove.map.TObjectByteMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import gnu.trove.procedure.TObjectByteProcedure;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class Sides {
    private static TObjectByteMap<Side> sideBits = new TObjectByteHashMap<Side>();

    static {
        sideBits.put(Side.TOP, (byte) 1);
        sideBits.put(Side.LEFT, (byte) 2);
        sideBits.put(Side.FRONT, (byte) 4);
        sideBits.put(Side.BOTTOM, (byte) 8);
        sideBits.put(Side.RIGHT, (byte) 16);
        sideBits.put(Side.BACK, (byte) 32);
    }

    public static byte getReverse(byte sides) {
        return (byte) ((sides / 8) + ((sides % 8) * 8));
    }

    public static byte getSides(Set<Side> sides) {
        byte result = 0;
        for (Side side : sides)
            result += sideBits.get(side);
        return result;
    }

    public static byte getSides(Side... sides) {
        byte result = 0;
        for (Side side : sides) {
            final byte sideBit = sideBits.get(side);
            if ((result & sideBit) > 0)
                throw new IllegalArgumentException("Cannot have multiples of the same side");
            result += sideBit;
        }
        return result;
    }

    public static byte getSide(Side side) {
        return sideBits.get(side);
    }

    public static Collection<Side> getSides(final byte sidesBit) {
        final Set<Side> result = Sets.newHashSet();
        sideBits.forEachEntry(
                new TObjectByteProcedure<Side>() {
                    @Override
                    public boolean execute(Side a, byte b) {
                        if ((b & sidesBit) > 0)
                            result.add(a);

                        return true;
                    }
                });

        return result;
    }

    public static boolean hasSide(byte sideBit, Side side) {
        return (sideBit & sideBits.get(side)) > 0;
    }

    public static byte addSide(byte sideBit, Side... sides) {
        for (Side side : sides) {
            sideBit |= sideBits.get(side);
        }

        return sideBit;
    }
}
