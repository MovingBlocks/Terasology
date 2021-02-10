/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.world.chunks.blockdata;

import com.google.common.base.Preconditions;
import org.terasology.world.chunks.deflate.TeraVisitingDeflator;


/**
 * TeraDenseArray8Bit implements a dense array with elements of 8 bit size.
 * Its elements are in the range -128 through +127 and it stores one element per byte.
 *
 */
public final class TeraDenseArray8Bit extends TeraDenseArrayByte {

    public TeraDenseArray8Bit() {
        super();
    }

    public TeraDenseArray8Bit(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ);
    }

    public TeraDenseArray8Bit(int sizeX, int sizeY, int sizeZ, byte[] data) {
        super(sizeX, sizeY, sizeZ, data);
    }

    public TeraDenseArray8Bit(TeraArray in) {
        super(in);
    }

    @Override
    protected TeraArray createDense(byte[] arrayData) {
        return new TeraDenseArray8Bit(getSizeX(), getSizeY(), getSizeZ(), arrayData);
    }

    @Override
    protected int rowSize() {
        return getSizeXZ();
    }

    @Override
    public TeraArray deflate(TeraVisitingDeflator deflator) {
        return Preconditions.checkNotNull(deflator).deflateDenseArray8Bit(data, rowSize(), getSizeX(), getSizeY(), getSizeZ());
    }

    @Override
    public int getElementSizeInBits() {
        return 8;
    }

    @Override
    public int get(int x, int y, int z) {
        int pos = pos(x, y, z);
        return data[pos];
    }

    @Override
    public int set(int x, int y, int z, int value) {
        int pos = pos(x, y, z);
        int old = data[pos];
        data[pos] = (byte) value;
        return old;
    }

    @Override
    public boolean set(int x, int y, int z, int value, int expected) {
        int pos = pos(x, y, z);
        int old = data[pos];
        if (old == expected) {
            data[pos] = (byte) value;
            return true;
        }
        return false;
    }

    public static class SerializationHandler extends TeraDenseArrayByte.SerializationHandler<TeraDenseArray8Bit> {

        @Override
        public boolean canHandle(Class<?> clazz) {
            return TeraDenseArray8Bit.class.equals(clazz);
        }

        @Override
        protected TeraDenseArray8Bit createArray(int sizeX, int sizeY, int sizeZ, byte[] data) {
            if (data == null) {
                return new TeraDenseArray8Bit(sizeX, sizeY, sizeZ);
            } else {
                return new TeraDenseArray8Bit(sizeX, sizeY, sizeZ, data);
            }
        }
    }

    public static class Factory implements TeraArray.Factory<TeraDenseArray8Bit> {

        @Override
        public Class<TeraDenseArray8Bit> getArrayClass() {
            return TeraDenseArray8Bit.class;
        }

        @Override
        public SerializationHandler createSerializationHandler() {
            return new SerializationHandler();
        }

        @Override
        public TeraDenseArray8Bit create() {
            return new TeraDenseArray8Bit();
        }

        @Override
        public TeraDenseArray8Bit create(int sizeX, int sizeY, int sizeZ) {
            return new TeraDenseArray8Bit(sizeX, sizeY, sizeZ);
        }
    }
}
