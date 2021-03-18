// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.blockdata;

import com.google.common.base.Preconditions;
import org.terasology.engine.world.chunks.deflate.TeraVisitingDeflator;

import java.util.Arrays;


/**
 * TeraSparseArray8Bit implements a sparse array with elements of 8 bit size.
 * Its elements are in the range -128 through +127 and it stores one element per byte.
 * It can reduce memory consumption through sparse memory allocation.
 *
 */
public final class TeraSparseArray8Bit extends TeraSparseArrayByte {

    public TeraSparseArray8Bit() {
        super();
    }

    public TeraSparseArray8Bit(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ);
    }

    public TeraSparseArray8Bit(int sizeX, int sizeY, int sizeZ, byte[][] inflated, byte[] deflated) {
        super(sizeX, sizeY, sizeZ, inflated, deflated);
    }

    public TeraSparseArray8Bit(int sizeX, int sizeY, int sizeZ, byte fill) {
        super(sizeX, sizeY, sizeZ, fill);
    }

    @Override
    protected TeraArray createSparse(byte defaultFill) {
        return new TeraSparseArray8Bit(getSizeX(), getSizeY(), getSizeZ(), defaultFill);
    }

    @Override
    protected TeraArray createSparse(byte[][] inflatedData, byte[] deflatedData) {
        return new TeraSparseArray8Bit(getSizeX(), getSizeY(), getSizeZ(), inflatedData, deflatedData);
    }

    @Override
    protected int rowSize() {
        return getSizeXZ();
    }

    @Override
    public TeraArray deflate(TeraVisitingDeflator deflator) {
        return Preconditions.checkNotNull(deflator).deflateSparseArray8Bit(inflated, deflated, fill, rowSize(), getSizeX(), getSizeY(), getSizeZ());
    }

    @Override
    public int getElementSizeInBits() {
        return 8;
    }

    @Override
    public int get(int x, int y, int z) {
        if (inflated == null) {
            return fill;
        }
        byte[] row = inflated[y];
        if (row != null) {
            return row[pos(x, z)];
        }
        return deflated[y];
    }

    @Override
    public int set(int x, int y, int z, int value) {
        if (inflated == null) {
            int old = fill;
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
            int pos = pos(x, z);
            int old = row[pos];
            row[pos] = (byte) value;
            return old;
        }
        int old = deflated[y];
        if (old == value) {
            return old;
        }
        inflated[y] = new byte[rowSize()];
        Arrays.fill(inflated[y], deflated[y]);
        int pos = pos(x, z);
        inflated[y][pos] = (byte) value;
        return deflated[y];
    }

    @Override
    public boolean set(int x, int y, int z, int value, int expected) {
        if (value == expected) {
            return true;
        }
        if (inflated == null) {
            int old = fill;
            if (old == value) {
                return true;
            } else {
                this.inflated = new byte[getSizeY()][];
                this.deflated = new byte[getSizeY()];
                Arrays.fill(deflated, fill);
            }
        }
        int pos = pos(x, z);
        byte[] row = inflated[y];
        if (row != null) {
            int old = row[pos];
            if (old == expected) {
                row[pos] = (byte) value;
                return true;
            }
            return false;
        }
        int old = deflated[y];
        if (old == expected) {
            inflated[y] = new byte[rowSize()];
            Arrays.fill(inflated[y], deflated[y]);
            inflated[y][pos] = (byte) value;
            return true;
        }
        return false;
    }

    public static final class SerializationHandler extends TeraSparseArrayByte.SerializationHandler<TeraSparseArray8Bit> {

        @Override
        public boolean canHandle(Class<?> clazz) {
            return TeraSparseArray8Bit.class.equals(clazz);
        }

        @Override
        protected TeraSparseArray8Bit createArray(int sizeX, int sizeY, int sizeZ) {
            return new TeraSparseArray8Bit(sizeX, sizeY, sizeZ);
        }
    }

    public static class Factory implements TeraArray.Factory<TeraSparseArray8Bit> {

        @Override
        public Class<TeraSparseArray8Bit> getArrayClass() {
            return TeraSparseArray8Bit.class;
        }

        @Override
        public SerializationHandler createSerializationHandler() {
            return new SerializationHandler();
        }

        @Override
        public TeraSparseArray8Bit create() {
            return new TeraSparseArray8Bit();
        }

        @Override
        public TeraSparseArray8Bit create(int sizeX, int sizeY, int sizeZ) {
            return new TeraSparseArray8Bit(sizeX, sizeY, sizeZ);
        }
    }
}
