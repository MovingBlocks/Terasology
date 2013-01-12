package org.terasology.world.chunks.blockdata;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.terasology.world.chunks.deflate.TeraVisitingDeflator;

import com.google.common.base.Preconditions;


/**
 * TeraArray is the base class used to store block related data in Chunks.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public abstract class TeraArray implements Externalizable {

    private int sizeX, sizeY, sizeZ, sizeXZ, sizeXZHalf, sizeXYZ, sizeXYZHalf;

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
    
    /**
     * This is the interface for tera array factories. Every tera array is required to implement a factory.
     * It should be implemented as a static subclass of the corresponding tera array class and it should be called Factory.
     *  
     * @author Manuel Brotz <manu.brotz@gmx.ch>
     * @see org.terasology.world.chunks.blockdata.TeraDenseArray16Bit.Factory
     *
     */
    public static interface Factory<T extends TeraArray> {

        public Class<T> getArrayClass();

        public T create();
        
        public T create(int sizeX, int sizeY, int sizeZ);
        
    }

    /**
     * This is the interface for serialization handlers for tera arrays. Every tera array is required to implement
     * a serialization handler. It is recommended to subclass {@link org.terasology.world.chunks.blockdata.TeraArray.BasicSerializationHandler TeraArray.BasicSerializationHandler}
     * instead of using this interface directly. It should be implemented as a static subclass of the corresponding tera array class. 
     * 
     * @author Manuel Brotz <manu.brotz@gmx.ch>
     * @see org.terasology.world.chunks.blockdata.TeraArray.BasicSerializationHandler
     */
    public static interface SerializationHandler<T extends TeraArray> extends org.terasology.io.SerializationHandler<T> {
        
        public int computeMinimumBufferSize(T array);

        public ByteBuffer serialize(T array, ByteBuffer buffer);
        
        public T deserialize(ByteBuffer buffer);
        
    }

    /**
     * Extending this class is the recommended way to implement serialization handlers for tera arrays.
     * Tera arrays should implement their serialization handlers as a static subclass called SerializationHandler.
     * 
     * @author Manuel Brotz <manu.brotz@gmx.ch>
     * @see org.terasology.world.chunks.blockdata.TeraDenseArray16Bit.SerializationHandler
     * @see org.terasology.world.chunks.blockdata.TeraDenseArray16Bit.Factory
     *
     */
    protected static abstract class BasicSerializationHandler<T extends TeraArray> implements SerializationHandler<T> {

        protected abstract int internalComputeMinimumBufferSize(T array);
        
        protected abstract void internalSerialize(T array, ByteBuffer buffer);
        
        protected abstract T internalDeserialize(int sizeX, int sizeY, int sizeZ, ByteBuffer buffer);

        @Override
        public final int computeMinimumBufferSize(T array) {
            Preconditions.checkNotNull(array, "The parameter 'array' must not be null");
            return 16 + internalComputeMinimumBufferSize(array);
        }
        
        @Override
        public final ByteBuffer serialize(T array, ByteBuffer buffer) {
            Preconditions.checkNotNull(array, "The parameter 'array' must not be null");
            Preconditions.checkArgument(canHandle(array.getClass()), "Unable to handle the supplied array (" + array.getClass().getName() + ")");
            if (buffer == null) {
                buffer = ByteBuffer.allocateDirect(computeMinimumBufferSize(array));
            }
            final int lengthPos = buffer.position();
            buffer.putInt(0);
            buffer.putInt(array.getSizeX());
            buffer.putInt(array.getSizeY());
            buffer.putInt(array.getSizeZ());
            internalSerialize(array, buffer);
            buffer.putInt(lengthPos, buffer.position() - lengthPos - 4);
            return buffer;
        }

        @Override
        public final T deserialize(ByteBuffer buffer) {
            Preconditions.checkNotNull(buffer, "The parameter 'buffer' must not be null");
            final int length = buffer.getInt();
            if (buffer.remaining() < length)
                throw new BufferUnderflowException();
            final int sizeX = buffer.getInt();
            final int sizeY = buffer.getInt();
            final int sizeZ = buffer.getInt();
            return internalDeserialize(sizeX, sizeY, sizeZ, buffer);
        }
    }
    
    protected TeraArray() {}

    protected TeraArray(int sizeX, int sizeY, int sizeZ, boolean initialize) {
        Preconditions.checkArgument(sizeX > 0);
        Preconditions.checkArgument(sizeY > 0);
        Preconditions.checkArgument(sizeZ > 0);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        sizeXZ = sizeX * sizeZ;
        sizeXZHalf = sizeXZ / 2;
        sizeXYZ = sizeY * sizeXZ;
        sizeXYZHalf = sizeXYZ / 2;
        Preconditions.checkArgument(getSizeXYZ() % 2 == 0, "The product of the parameters 'sizeX', 'sizeY' and 'sizeZ' has to be a multiple of 2 (" + getSizeXYZ() + ")");
        Preconditions.checkArgument(getSizeXZ() % 2 == 0, "The product of the parameters 'sizeX' and 'sizeZ' has to be a multiple of 2 (" + getSizeXZ() + ")");
        if (initialize) initialize();
    }

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
        return getClass().getName() + "(" + getSizeX() + ", " + getSizeY() + ", " + getSizeZ() + ", " + (isSparse() ? "sparse" : "dense") + ", " + getElementSizeInBits() + "bit, " + getEstimatedMemoryConsumptionInBytes() + "byte)";  
    }

    public abstract boolean isSparse();

    public abstract TeraArray copy();
    
    public abstract TeraArray deflate(TeraVisitingDeflator deflator);

    public abstract int getEstimatedMemoryConsumptionInBytes();
    
    public abstract int getElementSizeInBits();

    public abstract int get(int x, int y, int z);

    public abstract int set(int x, int y, int z, int value);

    public abstract boolean set(int x, int y, int z, int value, int expected);
    
}
