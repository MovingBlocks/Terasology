// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.viewer.color;



/**
 * Blends RGBA colors using the RGBA color model
 */
class ColorBlenderRgba implements ColorBlender {
    @Override
    public int get(int src) {
        return src;
    }

    @Override
    public int add(int src, int dst) {
        int mix = 0x000000FF;
        mix |= Math.min(0x0000FF00, (dst & 0x0000FF00) + (src & 0x0000FF00));
        mix |= Math.min(0x00FF0000, (dst & 0x00FF0000) + (src & 0x00FF0000));

        // casting to long avoids an integer overflow for the red channel
        mix |= Math.min(0xFF000000L, (dst & 0xFF000000L) + (src & 0xFF000000L));

        return mix;
    }

    /**
     * @param src in RGBA
     * @param dst in RGBA
     * @return the blended color in RGBx
     */
    @Override
    public int blend(int src, int dst) {
        int sr = (src >> 24) & 0xFF;
        int sg = (src >> 16) & 0xFF;
        int sb = (src >> 8) & 0xFF;
        int a = src & 0xFF;

        int dr = (dst >> 24) & 0xFF;
        int dg = (dst >> 16) & 0xFF;
        int db = (dst >> 8) & 0xFF;

        int mb = (a * sb + (0xFF - a) * db) / 0xFF;
        int mg = (a * sg + (0xFF - a) * dg) / 0xFF;
        int mr = (a * sr + (0xFF - a) * dr) / 0xFF;

        return (mb << 8) | (mg << 16) | (mr << 24) | 0xFF;
    }
}
