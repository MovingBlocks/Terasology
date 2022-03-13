// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.deflate;

import org.terasology.engine.world.chunks.blockdata.TeraArray;
import org.terasology.engine.world.chunks.blockdata.TeraSparseArray16Bit;
import org.terasology.engine.world.chunks.blockdata.TeraSparseArray4Bit;
import org.terasology.engine.world.chunks.blockdata.TeraSparseArray8Bit;

/**
 * TeraStandardDeflator implements a simple deflation algorithm for 4, 8 and 16-bit dense and sparse arrays.<br>
 * <b>NOTE:</b> Currently it is optimized for chunks of size 16x256x16 blocks.<br>
 * TODO: Implement deflation for sparse array 4bit.
 */
public class TeraStandardDeflator extends TeraVisitingDeflator {

    /*
     *  16-bit variant
     *  ==============
     *
     *  dense chunk  : 4 + 12 + (65536 * 2)                                                   = 131088
     *  sparse chunk : (4 + 12 + (256 * 2)) + (4 + 12 + (256 x 4)) + ((12 + (256 * 2)) x 256) = 135712
     *  difference   : 135712 - 131088                                                        =   4624
     *  min. deflate : 4624 / (12 + (256 * 2))                                                =      8.8
     *
     *
     *  8-bit variant
     *  =============
     *
     *  dense chunk  : 4 + 12 + 65536                                                   = 65552
     *  sparse chunk : (4 + 12 + 256) + (4 + 12 + (256 x 4)) + ((12 + 256) x 256)       = 69920
     *  difference   : 69920 - 65552                                                    =  4368
     *  min. deflate : 4368 / (12 + 256)                                                =    16.3
     *
     *
     *  4-bit variant
     *  =============
     *
     *  dense chunk  : 4 + 12 + (65536 / 2)                                             = 32784
     *  sparse chunk : (4 + 12 + 256) + (4 + 12 + (256 x 4)) + ((12 + (256 / 2)) x 256) = 37152
     *  difference   : 37152 - 32784                                                    =  4368
     *  min. deflate : 4368 / (12 + (256 / 2))                                          =    31.2
     *
     */

    // TODO dynamically calculate DEFLATE_MINIMUM_*, they only work for chunks with dimension 16x256x16
    protected static final int DEFLATE_MINIMUM_16BIT = 8;
    protected static final int DEFLATE_MINIMUM_8BIT = 16;
    protected static final int DEFLATE_MINIMUM_4BIT = 31;

    public TeraStandardDeflator() {
    }

    @Override
    public TeraArray deflateDenseArray16Bit(short[] data,
                                            int rowSize,
                                            int sizeX,
                                            int sizeY,
                                            int sizeZ) {
        final short[][] inflated = new short[sizeY][];
        final short[] deflated = new short[sizeY];
        int packed = 0;
        for (int y = 0; y < sizeY; y++) {
            final int start = y * rowSize;
            final short first = data[start];
            boolean packable = true;
            for (int i = 1; i < rowSize; i++) {
                if (data[start + i] != first) {
                    packable = false;
                    break;
                }
            }
            if (packable) {
                deflated[y] = first;
                ++packed;
            } else {
                short[] tmp = new short[rowSize];
                System.arraycopy(data, start, tmp, 0, rowSize);
                inflated[y] = tmp;
            }
        }
        if (packed == sizeY) {
            final short first = deflated[0];
            boolean packable = true;
            for (int i = 1; i < sizeY; i++) {
                if (deflated[i] != first) {
                    packable = false;
                    break;
                }
            }
            if (packable) {
                return new TeraSparseArray16Bit(sizeX, sizeY, sizeZ, first);
            }
        }
        if (packed > DEFLATE_MINIMUM_16BIT) {
            return new TeraSparseArray16Bit(sizeX, sizeY, sizeZ, inflated, deflated);
        }
        return null;
    }

    @Override
    public TeraArray deflateDenseArray8Bit(final byte[] data,
                                           final int rowSize,
                                           final int sizeX,
                                           final int sizeY,
                                           final int sizeZ) {
        final byte[][] inflated = new byte[sizeY][];
        final byte[] deflated = new byte[sizeY];
        int packed = 0;
        for (int y = 0; y < sizeY; y++) {
            final int start = y * rowSize;
            final byte first = data[start];
            boolean packable = true;
            for (int i = 1; i < rowSize; i++) {
                if (data[start + i] != first) {
                    packable = false;
                    break;
                }
            }
            if (packable) {
                deflated[y] = first;
                ++packed;
            } else {
                byte[] tmp = new byte[rowSize];
                System.arraycopy(data, start, tmp, 0, rowSize);
                inflated[y] = tmp;
            }
        }
        if (packed == sizeY) {
            final byte first = deflated[0];
            boolean packable = true;
            for (int i = 1; i < sizeY; i++) {
                if (deflated[i] != first) {
                    packable = false;
                    break;
                }
            }
            if (packable) {
                return new TeraSparseArray8Bit(sizeX, sizeY, sizeZ, first);
            }
        }
        if (packed > DEFLATE_MINIMUM_8BIT) {
            return new TeraSparseArray8Bit(sizeX, sizeY, sizeZ, inflated, deflated);
        }
        return null;
    }

    @Override
    public TeraArray deflateDenseArray4Bit(final byte[] data,
                                           final int rowSize,
                                           final int sizeX,
                                           final int sizeY,
                                           final int sizeZ) {
        final byte[][] inflated = new byte[sizeY][];
        final byte[] deflated = new byte[sizeY];
        int packed = 0;
        for (int y = 0; y < sizeY; y++) {
            final int start = y * rowSize;
            final byte first = data[start];
            boolean packable = true;
            for (int i = 1; i < rowSize; i++) {
                if (data[start + i] != first) {
                    packable = false;
                    break;
                }
            }
            if (packable) {
                deflated[y] = first;
                ++packed;
            } else {
                byte[] tmp = new byte[rowSize];
                System.arraycopy(data, start, tmp, 0, rowSize);
                inflated[y] = tmp;
            }
        }
        if (packed == sizeY) {
            final byte first = deflated[0];
            boolean packable = true;
            for (int i = 1; i < sizeY; i++) {
                if (deflated[i] != first) {
                    packable = false;
                    break;
                }
            }
            if (packable) {
                return new TeraSparseArray4Bit(sizeX, sizeY, sizeZ, first);
            }
        }
        if (packed > DEFLATE_MINIMUM_4BIT) {
            return new TeraSparseArray4Bit(sizeX, sizeY, sizeZ, inflated, deflated);
        }
        return null;
    }

    @Override
    public TeraArray deflateSparseArray16Bit(short[][] inflated,
                                             short[] deflated,
                                             short fill,
                                             int rowSize,
                                             int sizeX,
                                             int sizeY,
                                             int sizeZ) {
        if (inflated == null && deflated == null) {
            return new TeraSparseArray16Bit(sizeX, sizeY, sizeZ, fill);
        }
        if (inflated == null) {
            return new TeraSparseArray16Bit(sizeX, sizeY, sizeZ, inflated, deflated);
        }

        short[] packed = new short[sizeY];
        short[][] newInflated = new short[sizeY][];
        for (int y = 0; y < sizeY; y++) {
            short[] planeXY = inflated[y];
            if (planeXY != null) {
                short first = planeXY[0];
                boolean packing = true;
                for (int i = 1; i < rowSize; i++) {
                    if (first != planeXY[i]) {
                        packing = false;
                        break;
                    }
                }
                if (packing) {
                    packed[y] = first;
                } else {
                    short[] xyvalues = new short[rowSize];
                    System.arraycopy(inflated[y], 0, xyvalues, 0, rowSize);
                    newInflated[y] = xyvalues;
                }
            } else {
                if (deflated != null) {
                    packed[y] = deflated[y];
                }
            }
        }
        return new TeraSparseArray16Bit(sizeX, sizeY, sizeZ, newInflated, packed);
    }

    @Override
    public TeraArray deflateSparseArray8Bit(final byte[][] inflated,
                                            final byte[] deflated,
                                            final byte fill,
                                            final int rowSize,
                                            final int sizeX,
                                            final int sizeY,
                                            final int sizeZ) {
        if (inflated == null && deflated == null) {
            return new TeraSparseArray8Bit(sizeX, sizeY, sizeZ, fill);
        }
        if (inflated == null) {
            return new TeraSparseArray8Bit(sizeX, sizeY, sizeZ, inflated, deflated);
        }

        byte[] packed = new byte[sizeY];
        byte[][] newInflated = new byte[sizeY][];
        for (int y = 0; y < sizeY; y++) {
            byte[] planeXY = inflated[y];
            if (planeXY != null) {
                byte first = planeXY[0];
                boolean packing = true;
                for (int i = 1; i < rowSize; i++) {
                    if (first != planeXY[i]) {
                        packing = false;
                        break;
                    }
                }
                if (packing) {
                    packed[y] = first;
                } else {
                    byte[] xyvalues = new byte[rowSize];
                    System.arraycopy(inflated[y], 0, xyvalues, 0, rowSize);
                    newInflated[y] = xyvalues;
                }
            } else {
                if (deflated != null) {
                    packed[y] = deflated[y];
                }
            }
        }
        return new TeraSparseArray8Bit(sizeX, sizeY, sizeZ, newInflated, packed);
    }

    @Override
    public TeraArray deflateSparseArray4Bit(final byte[][] inflated,
                                            final byte[] deflated,
                                            final byte fill,
                                            final int rowSize,
                                            final int sizeX,
                                            final int sizeY,
                                            final int sizeZ) {
        return null;
    }

}
