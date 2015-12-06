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

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

/**
 * TeraDenseArray16Bit implements a sparse array with elements of 16 bit size.
 * Its elements are in the range -32'768 through +32'767 and it internally uses the short type to store its elements.
 * It can reduce memory consumption through sparse memory allocation.
 *
 */
public class TeraSparseArray16Bit extends TeraSparseArray {

    protected short[][] inflated;
    protected short[] deflated;
    protected short fill;

    public TeraSparseArray16Bit() {
        super();
    }

    public TeraSparseArray16Bit(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ, false);
    }

    public TeraSparseArray16Bit(int sizeX, int sizeY, int sizeZ, short[][] inflated, short[] deflated) {
        super(sizeX, sizeY, sizeZ, false);
        this.inflated = Preconditions.checkNotNull(inflated);
        this.deflated = Preconditions.checkNotNull(deflated);
        Preconditions.checkArgument(inflated.length == sizeY, "The length of parameter 'inflated' has to be " + sizeY + " but is " + inflated.length);
        Preconditions.checkArgument(deflated.length == sizeY, "The length of parameter 'deflated' has to be " + sizeY + " but is " + deflated.length);
    }

    public TeraSparseArray16Bit(int sizeX, int sizeY, int sizeZ, short fill) {
        super(sizeX, sizeY, sizeZ, false);
        this.fill = fill;
    }

    @Override
    protected void initialize() {
    }

    @Override
    public TeraArray copy() {
        if (inflated == null) {
            return new TeraSparseArray16Bit(getSizeX(), getSizeY(), getSizeZ(), fill);
        }
        short[][] inf = new short[getSizeY()][];
        short[] def = new short[getSizeY()];
        for (int y = 0; y < getSizeY(); y++) {
            if (inflated[y] != null) {
                inf[y] = new short[getSizeXZ()];
                System.arraycopy(inflated[y], 0, inf[y], 0, getSizeXZ());
            }
        }
        System.arraycopy(deflated, 0, def, 0, getSizeY());
        return new TeraSparseArray16Bit(getSizeX(), getSizeY(), getSizeZ(), inf, def);
    }

    @Override
    public TeraArray deflate(TeraVisitingDeflator deflator) {
        return Preconditions.checkNotNull(deflator).deflateSparseArray16Bit(inflated, deflated, fill, getSizeXZ(), getSizeX(), getSizeY(), getSizeZ());
    }

    @Override
    public int getEstimatedMemoryConsumptionInBytes() {
        if (inflated == null) {
            return 10;
        }
        int result = 10 + (getSizeY() * 2) + (getSizeY() * 4);
        for (int i = 0; i < getSizeY(); i++) {
            if (inflated[i] != null) {
                result += 12 + (getSizeXZ() * 2);
            }
        }
        return result;
    }

    @Override
    public int getElementSizeInBits() {
        return 16;
    }

    @Override
    public int get(int x, int y, int z) {
        if (inflated == null) {
            return fill;
        }
        short[] row = inflated[y];
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
                this.inflated = new short[getSizeY()][];
                this.deflated = new short[getSizeY()];
                Arrays.fill(deflated, fill);
            }
        }
        short[] row = inflated[y];
        if (row != null) {
            int pos = pos(x, z);
            int old = row[pos];
            row[pos] = (short) value;
            return old;
        }
        int old = deflated[y];
        if (old == value) {
            return old;
        }
        inflated[y] = new short[getSizeXZ()];
        Arrays.fill(inflated[y], deflated[y]);
        int pos = pos(x, z);
        inflated[y][pos] = (short) value;
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
                this.inflated = new short[getSizeY()][];
                this.deflated = new short[getSizeY()];
                Arrays.fill(deflated, fill);
            }
        }
        int pos = pos(x, z);
        short[] row = inflated[y];
        if (row != null) {
            int old = row[pos];
            if (old == expected) {
                row[pos] = (short) value;
                return true;
            }
            return false;
        }
        int old = deflated[y];
        if (old == expected) {
            inflated[y] = new short[getSizeXZ()];
            Arrays.fill(inflated[y], deflated[y]);
            inflated[y][pos] = (short) value;
            return true;
        }
        return false;
    }

    public static class SerializationHandler extends TeraArray.BasicSerializationHandler<TeraSparseArray16Bit> {

        private void putRow(final short[] row, final int length, final ByteBuffer buffer) {
            final ShortBuffer sbuffer = buffer.asShortBuffer();
            sbuffer.put(row, 0, length);
            buffer.position(buffer.position() + length * 2);
        }

        private void getRow(final short[] row, final int length, final ByteBuffer buffer) {
            final ShortBuffer sbuffer = buffer.asShortBuffer();
            sbuffer.get(row, 0, length);
            buffer.position(buffer.position() + length * 2);
        }

        @Override
        public boolean canHandle(Class<?> clazz) {
            return TeraSparseArray16Bit.class.equals(clazz);
        }

        @Override
        protected int internalComputeMinimumBufferSize(TeraSparseArray16Bit array) {
            final short[][] inf = array.inflated;
            if (inf == null) {
                return 3;
            } else {
                int sizeY = array.getSizeY();
                int rowSize = array.getSizeXZ() * 2;
                int result = 1;
                for (int y = 0; y < sizeY; y++) {
                    if (inf[y] == null) {
                        result += 3;
                    } else {
                        result += 1 + rowSize;
                    }
                }
                return result;
            }
        }

        @Override
        protected void internalSerialize(TeraSparseArray16Bit array, ByteBuffer buffer) {
            final short[][] inf = array.inflated;
            if (inf == null) {
                buffer.put((byte) 0);
                buffer.putShort(array.fill);
            } else {
                buffer.put((byte) 1);
                int sizeY = array.getSizeY();
                int rowSize = array.getSizeXZ();
                final short[] def = array.deflated;
                for (int y = 0; y < sizeY; y++) {
                    final short[] row = inf[y];
                    if (row == null) {
                        buffer.put((byte) 0);
                        buffer.putShort(def[y]);
                    } else {
                        buffer.put((byte) 1);
                        putRow(row, rowSize, buffer);
                    }
                }
            }
        }

        @Override
        protected TeraSparseArray16Bit internalDeserialize(int sizeX, int sizeY, int sizeZ, ByteBuffer buffer) {
            final byte hasData = buffer.get();
            final TeraSparseArray16Bit array = new TeraSparseArray16Bit(sizeX, sizeY, sizeZ);
            if (hasData == 0) {
                array.fill = buffer.getShort();
                return array;
            }
            final int rowSize = array.getSizeXZ();
            array.inflated = new short[sizeY][];
            array.deflated = new short[sizeY];
            for (int y = 0; y < sizeY; y++) {
                final byte hasRow = buffer.get();
                if (hasRow == 0) {
                    array.deflated[y] = buffer.getShort();
                } else {
                    array.inflated[y] = new short[rowSize];
                    getRow(array.inflated[y], rowSize, buffer);
                }
            }
            return array;
        }
    }

    public static class Factory implements TeraArray.Factory<TeraSparseArray16Bit> {

        @Override
        public Class<TeraSparseArray16Bit> getArrayClass() {
            return TeraSparseArray16Bit.class;
        }

        @Override
        public SerializationHandler createSerializationHandler() {
            return new SerializationHandler();
        }

        @Override
        public TeraSparseArray16Bit create() {
            return new TeraSparseArray16Bit();
        }

        @Override
        public TeraSparseArray16Bit create(int sizeX, int sizeY, int sizeZ) {
            return new TeraSparseArray16Bit(sizeX, sizeY, sizeZ);
        }
    }

}
