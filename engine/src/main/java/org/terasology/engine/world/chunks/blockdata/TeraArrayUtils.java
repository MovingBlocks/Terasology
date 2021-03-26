// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.blockdata;

/**
 * TeraArrayUtils contains some methods used in some TeraArray implementations.
 *
 */
public final class TeraArrayUtils {

    private TeraArrayUtils() {
    }

    public static byte getLo(int value) {
        return (byte) (value & 0x0F);
    }

    public static byte getHi(int value) {
        return (byte) ((value & 0xF0) >> 4);
    }

    public static byte setHi(int value, int hi) {
        return makeByte(hi, getLo(value));
    }

    public static byte setLo(int value, int lo) {
        return makeByte(getHi(value), lo);
    }

    public static byte makeByte(int hi, int lo) {
        return (byte) ((hi << 4) | (lo));
    }
}
