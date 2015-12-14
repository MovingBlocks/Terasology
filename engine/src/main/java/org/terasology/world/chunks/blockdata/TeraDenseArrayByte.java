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

import java.nio.ByteBuffer;


/**
 * TeraDenseArrayByte is the base class used to implement dense arrays with elements of size 4 bit or 8 bit.
 *
 */
public abstract class TeraDenseArrayByte extends TeraDenseArray {

    protected byte[] data;

    protected TeraDenseArrayByte() {
        super();
    }

    protected TeraDenseArrayByte(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ, true);
    }

    protected TeraDenseArrayByte(int sizeX, int sizeY, int sizeZ, byte[] data) {
        super(sizeX, sizeY, sizeZ, false);
        this.data = Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.length == dataSize(), "The length of the parameter 'data' has to be " + dataSize() + " but is " + data.length);
    }

    protected TeraDenseArrayByte(TeraArray in) {
        super(in);
    }

    protected abstract TeraArray createDense(byte[] arrayData);

    protected abstract int rowSize();

    protected final int dataSize() {
        return getSizeY() * rowSize();
    }

    @Override
    protected void initialize() {
        this.data = new byte[dataSize()];
    }

    @Override
    public final int getEstimatedMemoryConsumptionInBytes() {
        if (data == null) {
            return 4;
        } else {
            return 16 + data.length;
        }
    }

    @Override
    public final TeraArray copy() {
        byte[] result = new byte[dataSize()];
        System.arraycopy(data, 0, result, 0, dataSize());
        return createDense(result);
    }

    protected abstract static class SerializationHandler<T extends TeraDenseArrayByte> extends TeraArray.BasicSerializationHandler<T> {

        protected abstract T createArray(int sizeX, int sizeY, int sizeZ, byte[] data);

        @Override
        protected int internalComputeMinimumBufferSize(T array) {
            final byte[] data = array.data;
            if (data == null) {
                return 4;
            } else {
                return 4 + data.length;
            }
        }

        @Override
        protected void internalSerialize(T array, ByteBuffer buffer) {
            final byte[] data = array.data;
            if (data == null) {
                buffer.putInt(0);
            } else {
                buffer.putInt(data.length);
                buffer.put(data);
            }
        }

        @Override
        protected T internalDeserialize(int sizeX, int sizeY, int sizeZ, ByteBuffer buffer) {
            final int length = buffer.getInt();
            if (length > 0) {
                final byte[] data = new byte[length];
                buffer.get(data, 0, length);
                return createArray(sizeX, sizeY, sizeZ, data);
            }
            return createArray(sizeX, sizeY, sizeZ, null);
        }
    }
}
