// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.viewer.color;



/**
 * Blends RGBA colors using the ARGB color model
 */
class ColorBlenderArgb implements ColorBlender {

    @Override
    public int get(int src) {
        // RGBA becomes ARGBA by a simple rotation
        return Integer.rotateRight(src, Byte.SIZE);
    }

    @Override
    public int add(int src1, int dst) {
        int src = src1 >>> Byte.SIZE; // we ignore alpha
        int mix = 0xFF000000;
        mix |= Math.min(0x0000FF, (dst & 0x0000FF) + (src & 0x0000FF));
        mix |= Math.min(0x00FF00, (dst & 0x00FF00) + (src & 0x00FF00));
        mix |= Math.min(0xFF0000, (dst & 0xFF0000) + (src & 0xFF0000));
        return mix;
    }

    @Override
    public int blend(int src, int dst) {
        int sr = (src >> 24) & 0xFF;
        int sg = (src >> 16) & 0xFF;
        int sb = (src >> 8) & 0xFF;
        int a = src & 0xFF;

        int dr = (dst >> 16) & 0xFF;
        int dg = (dst >> 8) & 0xFF;
        int db = dst & 0xFF;

        int mb = (a * sb + (0xFF - a) * db) / 0xFF;
        int mg = (a * sg + (0xFF - a) * dg) / 0xFF;
        int mr = (a * sr + (0xFF - a) * dr) / 0xFF;

        return 0xFF000000 | mb | (mg << 8) | (mr << 16);
    }
}
