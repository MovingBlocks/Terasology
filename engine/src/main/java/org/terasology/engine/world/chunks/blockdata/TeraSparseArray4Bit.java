// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.blockdata;

import com.google.common.base.Preconditions;
import org.terasology.engine.world.chunks.deflate.TeraVisitingDeflator;

import java.util.Arrays;


/**
 * TeraSparseArray4Bit implements a sparse array with elements of 4 bit size.
 * Its elements are in the range 0 - 15 and it increases memory efficiency by storing two elements per byte.
 * It can further reduce memory consumption through sparse memory allocation.
 *
 */
public final class TeraSparseArray4Bit extends TeraSparseArrayByte {

    public TeraSparseArray4Bit() {
        super();
    }

    public TeraSparseArray4Bit(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ);
    }

    public TeraSparseArray4Bit(int sizeX, int sizeY, int sizeZ, byte[][] inflated, byte[] deflated) {
        super(sizeX, sizeY, sizeZ, inflated, deflated);
    }

    public TeraSparseArray4Bit(int sizeX, int sizeY, int sizeZ, byte fill) {
        super(sizeX, sizeY, sizeZ, fill);
        Preconditions.checkArgument(fill >= 0 && fill <= 15, "Parameter 'fill' has to be in the range 0 - 15 (" + fill + ")");
    }

    @Override
    protected TeraArray createSparse(byte defaultFill) {
        return new TeraSparseArray4Bit(getSizeX(), getSizeY(), getSizeZ(), defaultFill);
    }

    @Override
    protected TeraArray createSparse(byte[][] inflatedData, byte[] deflatedData) {
        return new TeraSparseArray4Bit(getSizeX(), getSizeY(), getSizeZ(), inflatedData, deflatedData);
    }

    @Override
    protected int rowSize() {
        return getSizeXZHalf();
    }

    private int rowGet(int pos, byte value) {
        if (pos < getSizeXZHalf()) {
            return TeraArrayUtils.getHi(value);
        }
        return TeraArrayUtils.getLo(value);
    }

    private int rowGet(byte[] row, int pos) {
        if (pos < getSizeXZHalf()) {
            return TeraArrayUtils.getHi(row[pos]);
        }
        return TeraArrayUtils.getLo(row[pos - getSizeXZHalf()]);
    }

    private void rowSet(byte[] row, int pos, int value) {
        if (pos < getSizeXZHalf()) {
            byte raw = row[pos];
            row[pos] = TeraArrayUtils.setHi(raw, value);
            return;
        }
        int rowPos = pos - getSizeXZHalf();
        byte raw = row[rowPos];
        row[rowPos] = TeraArrayUtils.setLo(raw, value);
    }

    private int rowSetGetOld(byte[] row, int pos, int value) {
        if (pos < getSizeXZHalf()) {
            byte raw = row[pos];
            byte old = TeraArrayUtils.getHi(raw);
            row[pos] = TeraArrayUtils.setHi(raw, value);
            return old;
        }
        int rowPos = pos - getSizeXZHalf();
        byte raw = row[rowPos];
        byte old = TeraArrayUtils.getLo(raw);
        row[rowPos] = TeraArrayUtils.setLo(raw, value);
        return old;
    }

    @Override
    public TeraArray deflate(TeraVisitingDeflator deflator) {
        return Preconditions.checkNotNull(deflator).deflateSparseArray4Bit(inflated, deflated, fill, rowSize(), getSizeX(), getSizeY(), getSizeZ());
    }

    @Override
    public int getElementSizeInBits() {
        return 4;
    }

    @Override
    public int get(int x, int y, int z) {
        int pos = pos(x, z);
        if (inflated == null) {
            return rowGet(pos, fill);
        }
        byte[] row = inflated[y];
        if (row != null) {
            return rowGet(row, pos);
        }
        return rowGet(pos, deflated[y]);
    }

    @Override
    public int set(int x, int y, int z, int value) {
        int pos = pos(x, z);
        if (inflated == null) {
            int old = rowGet(pos, fill);
            if (old == value) {
                return old;
            } else {
                this.inflated = new byte[getSizeY()][];
                this.deflated = new byte[getSizeY()];
                Arrays.fill(deflated, fill);
            }
        }
        byte[] row = inflated[y];
        if (row != null) {
            return rowSetGetOld(row, pos, value);
        }
        int old = rowGet(pos, deflated[y]);
        if (old == value) {
            return old;
        }
        inflated[y] = new byte[rowSize()];
        Arrays.fill(inflated[y], deflated[y]);
        return rowSetGetOld(inflated[y], pos, value);
    }

    @Override
    public boolean set(int x, int y, int z, int value, int expected) {
        if (value == expected) {
            return true;
        }
        int pos = pos(x, z);
        if (inflated == null) {
            int old = rowGet(pos, fill);
            if (old == value) {
                return true;
            } else {
                this.inflated = new byte[getSizeY()][];
                this.deflated = new byte[getSizeY()];
                Arrays.fill(deflated, fill);
            }
        }
        byte[] row = inflated[y];
        if (row != null) {
            int old = rowGet(row, pos);
            if (old == expected) {
                rowSet(row, pos, value);
                return true;
            }
            return false;
        }
        int old = rowGet(pos, deflated[y]);
        if (old == expected) {
            inflated[y] = new byte[rowSize()];
            Arrays.fill(inflated[y], deflated[y]);
            rowSet(inflated[y], pos, value);
            return true;
        }
        return false;
    }

    public static final class SerializationHandler extends TeraSparseArrayByte.SerializationHandler<TeraSparseArray4Bit> {

        @Override
        public boolean canHandle(Class<?> clazz) {
            return TeraSparseArray4Bit.class.equals(clazz);
        }

        @Override
        protected TeraSparseArray4Bit createArray(int sizeX, int sizeY, int sizeZ) {
            return new TeraSparseArray4Bit(sizeX, sizeY, sizeZ);
        }
    }

    public static class Factory implements TeraArray.Factory<TeraSparseArray4Bit> {

        @Override
        public Class<TeraSparseArray4Bit> getArrayClass() {
            return TeraSparseArray4Bit.class;
        }

        @Override
        public SerializationHandler createSerializationHandler() {
            return new SerializationHandler();
        }

        @Override
        public TeraSparseArray4Bit create() {
            return new TeraSparseArray4Bit();
        }

        @Override
        public TeraSparseArray4Bit create(int sizeX, int sizeY, int sizeZ) {
            return new TeraSparseArray4Bit(sizeX, sizeY, sizeZ);
        }
    }
}
