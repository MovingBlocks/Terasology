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

import org.terasology.world.chunks.deflate.TeraVisitingDeflator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * TeraArray is the base class used to store block related data in Chunks.
 *
 */
public abstract class TeraArray {

    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private int sizeXZ;
    private int sizeXZHalf;
    private int sizeXYZ;
    private int sizeXYZHalf;

    protected TeraArray() {
    }

    protected TeraArray(int sizeX, int sizeY, int sizeZ, boolean initialize) {
        checkArgument(sizeX > 0);
        checkArgument(sizeY > 0);
        checkArgument(sizeZ > 0);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        sizeXZ = sizeX * sizeZ;
        sizeXZHalf = sizeXZ / 2;
        sizeXYZ = sizeY * sizeXZ;
        sizeXYZHalf = sizeXYZ / 2;
        checkArgument(getSizeXYZ() % 2 == 0, "The product of the parameters 'sizeX', 'sizeY' and 'sizeZ' has to be a multiple of 2 (" + getSizeXYZ() + ")");
        checkArgument(getSizeXZ() % 2 == 0, "The product of the parameters 'sizeX' and 'sizeZ' has to be a multiple of 2 (" + getSizeXZ() + ")");
        if (initialize) {
            initialize();
        }
    }

    protected final void writeExternalHeader(ObjectOutput out) throws IOException {
        out.writeInt(sizeX);
        out.writeInt(sizeY);
        out.writeInt(sizeZ);
    }

    protected final void readExternalHeader(ObjectInput in) throws IOException {
        sizeX = in.readInt();
        sizeY = in.readInt();
        sizeZ = in.readInt();
        sizeXZ = sizeX * sizeZ;
        sizeXZHalf = sizeXZ / 2;
        sizeXYZ = sizeY * sizeXZ;
        sizeXYZHalf = sizeXYZ / 2;
    }

    protected final int pos(int x, int y, int z) {
        return y * getSizeXZ() + z * getSizeX() + x;
    }

    protected final int pos(int x, int z) {
        return z * getSizeX() + x;
    }

    protected abstract void initialize();

    public final int getSizeX() {
        return sizeX;
    }

    public final int getSizeY() {
        return sizeY;
    }

    public final int getSizeZ() {
        return sizeZ;
    }

    public final int getSizeXZ() {
        return sizeXZ;
    }

    public final int getSizeXZHalf() {
        return sizeXZHalf;
    }

    public final int getSizeXYZ() {
        return sizeXYZ;
    }

    public final int getSizeXYZHalf() {
        return sizeXYZHalf;
    }

    public final boolean contains(int x, int y, int z) {
        return (x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ);
    }

    @Override
    public String toString() {
        return getClass().getName() + "(" + getSizeX() + ", " + getSizeY() + ", " + getSizeZ() + ", "
                + (isSparse() ? "sparse" : "dense") + ", " + getElementSizeInBits() + "bit, " + getEstimatedMemoryConsumptionInBytes() + "byte)";
    }

    public abstract boolean isSparse();

    public abstract TeraArray copy();

    public abstract TeraArray deflate(TeraVisitingDeflator deflator);

    public abstract int getEstimatedMemoryConsumptionInBytes();

    public abstract int getElementSizeInBits();

    public abstract int get(int x, int y, int z);

    public abstract int set(int x, int y, int z, int value);

    public abstract boolean set(int x, int y, int z, int value, int expected);

    /**
     * This is the interface for tera array factories. Every tera array is required to implement a factory.
     * It should be implemented as a static subclass of the corresponding tera array class and it should be called Factory.
     *
     * @see org.terasology.world.chunks.blockdata.TeraDenseArray16Bit.Factory
     */
    public interface Factory<T extends TeraArray> {

        Class<T> getArrayClass();

        SerializationHandler<T> createSerializationHandler();

        T create();

        T create(int sizeX, int sizeY, int sizeZ);

    }

    /**
     * This is the interface for serialization handlers for tera arrays. Every tera array is required to implement
     * a serialization handler. It is recommended to subclass
     * {@link org.terasology.world.chunks.blockdata.TeraArray.BasicSerializationHandler TeraArray.BasicSerializationHandler}
     * instead of using this interface directly. It should be implemented as a static subclass of the corresponding tera array class.
     *
     * @see org.terasology.world.chunks.blockdata.TeraArray.BasicSerializationHandler
     */
    public interface SerializationHandler<T extends TeraArray> {

        int computeMinimumBufferSize(T array);

        ByteBuffer serialize(T array);

        ByteBuffer serialize(T array, ByteBuffer toBuffer);

        T deserialize(ByteBuffer buffer);

        boolean canHandle(Class<?> clazz);
    }

    /**
     * Extending this class is the recommended way to implement serialization handlers for tera arrays.
     * Tera arrays should implement their serialization handlers as a static subclass called SerializationHandler.
     *
     * @see org.terasology.world.chunks.blockdata.TeraDenseArray16Bit.SerializationHandler
     * @see org.terasology.world.chunks.blockdata.TeraDenseArray16Bit.Factory
     */
    protected abstract static class BasicSerializationHandler<T extends TeraArray> implements SerializationHandler<T> {

        protected abstract int internalComputeMinimumBufferSize(T array);

        protected abstract void internalSerialize(T array, ByteBuffer buffer);

        protected abstract T internalDeserialize(int sizeX, int sizeY, int sizeZ, ByteBuffer buffer);

        @Override
        public final int computeMinimumBufferSize(T array) {
            checkNotNull(array, "The parameter 'array' must not be null");
            return 16 + internalComputeMinimumBufferSize(array);
        }

        @Override
        public final ByteBuffer serialize(T array) {
            checkNotNull(array, "The parameter 'array' must not be null");
            return serialize(array, ByteBuffer.allocateDirect(computeMinimumBufferSize(array)));
        }

        @Override
        public final ByteBuffer serialize(T array, ByteBuffer toBuffer) {
            checkNotNull(array, "The parameter 'array' must not be null");
            checkNotNull(toBuffer, "The parameter 'toBuffer' must not be null");
            checkArgument(canHandle(array.getClass()), "Unable to handle the supplied array (" + array.getClass().getName() + ")");
            final int lengthPos = toBuffer.position();
            toBuffer.putInt(0);
            toBuffer.putInt(array.getSizeX());
            toBuffer.putInt(array.getSizeY());
            toBuffer.putInt(array.getSizeZ());
            internalSerialize(array, toBuffer);
            toBuffer.putInt(lengthPos, toBuffer.position() - lengthPos - 4);
            return toBuffer;
        }

        @Override
        public final T deserialize(ByteBuffer buffer) {
            checkNotNull(buffer, "The parameter 'buffer' must not be null");
            final int length = buffer.getInt();
            if (buffer.remaining() < length) {
                throw new BufferUnderflowException();
            }
            final int sizeX = buffer.getInt();
            final int sizeY = buffer.getInt();
            final int sizeZ = buffer.getInt();
            return internalDeserialize(sizeX, sizeY, sizeZ, buffer);
        }
    }
}
