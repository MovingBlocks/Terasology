// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.blockdata;

import com.google.common.base.Preconditions;
import org.terasology.engine.world.chunks.deflate.TeraVisitingDeflator;


/**
 * TeraDenseArray4Bit implements a dense array with elements of 4 bit size.
 * Its elements are in the range 0 - 15 and it increases memory efficiency by storing two elements per byte.
 *
 */
public final class TeraDenseArray4Bit extends TeraDenseArrayByte {

    public TeraDenseArray4Bit() {
        super();
    }

    public TeraDenseArray4Bit(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ);
    }

    public TeraDenseArray4Bit(int sizeX, int sizeY, int sizeZ, byte[] data) {
        super(sizeX, sizeY, sizeZ, data);
    }

    public TeraDenseArray4Bit(TeraArray in) {
        super(in);
    }

    @Override
    protected TeraArray createDense(byte[] arrayData) {
        return new TeraDenseArray4Bit(getSizeX(), getSizeY(), getSizeZ(), arrayData);
    }

    @Override
    protected int rowSize() {
        return getSizeXZHalf();
    }

    @Override
    public TeraArray deflate(TeraVisitingDeflator deflator) {
        return Preconditions.checkNotNull(deflator).deflateDenseArray4Bit(data, rowSize(), getSizeX(), getSizeY(), getSizeZ());
    }

    @Override
    public int getElementSizeInBits() {
        return 4;
    }

    @Override
    public int get(int x, int y, int z) {
        int row = y * getSizeXZHalf();
        int pos = pos(x, z);
        if (pos < getSizeXZHalf()) {
            return TeraArrayUtils.getHi(data[row + pos]);
        }
        pos = pos - getSizeXZHalf();
        return TeraArrayUtils.getLo(data[row + pos]);
    }

    @Override
    public int set(int x, int y, int z, int value) {
        int row = y * getSizeXZHalf();
        int pos = pos(x, z);
        if (pos < getSizeXZHalf()) {
            byte raw = data[row + pos];
            byte old = TeraArrayUtils.getHi(raw);
            data[row + pos] = TeraArrayUtils.setHi(raw, value);
            return old;
        }
        pos = pos - getSizeXZHalf();
        byte raw = data[row + pos];
        byte old = TeraArrayUtils.getLo(raw);
        data[row + pos] = TeraArrayUtils.setLo(raw, value);
        return old;
    }

    @Override
    public boolean set(int x, int y, int z, int value, int expected) {
        int row = y * getSizeXZHalf();
        int pos = pos(x, z);
        if (pos < getSizeXZHalf()) {
            byte raw = data[row + pos];
            byte old = TeraArrayUtils.getHi(raw);
            if (old == expected) {
                data[row + pos] = TeraArrayUtils.setHi(raw, value);
                return true;
            }
            return false;
        }
        pos = pos - getSizeXZHalf();
        byte raw = data[row + pos];
        byte old = TeraArrayUtils.getLo(raw);
        if (old == expected) {
            data[row + pos] = TeraArrayUtils.setLo(raw, value);
            return true;
        }
        return false;
    }

    public static class SerializationHandler extends TeraDenseArrayByte.SerializationHandler<TeraDenseArray4Bit> {

        @Override
        public boolean canHandle(Class<?> clazz) {
            return TeraDenseArray4Bit.class.equals(clazz);
        }

        @Override
        protected TeraDenseArray4Bit createArray(int sizeX, int sizeY, int sizeZ, byte[] data) {
            if (data == null) {
                return new TeraDenseArray4Bit(sizeX, sizeY, sizeZ);
            } else {
                return new TeraDenseArray4Bit(sizeX, sizeY, sizeZ, data);
            }
        }
    }

    public static class Factory implements TeraArray.Factory<TeraDenseArray4Bit> {

        @Override
        public Class<TeraDenseArray4Bit> getArrayClass() {
            return TeraDenseArray4Bit.class;
        }

        @Override
        public SerializationHandler createSerializationHandler() {
            return new SerializationHandler();
        }

        @Override
        public TeraDenseArray4Bit create() {
            return new TeraDenseArray4Bit();
        }

        @Override
        public TeraDenseArray4Bit create(int sizeX, int sizeY, int sizeZ) {
            return new TeraDenseArray4Bit(sizeX, sizeY, sizeZ);
        }
    }

}
