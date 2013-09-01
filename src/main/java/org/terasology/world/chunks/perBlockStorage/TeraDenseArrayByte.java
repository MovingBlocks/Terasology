package org.terasology.world.chunks.perBlockStorage;

import java.nio.ByteBuffer;

import com.google.common.base.Preconditions;


/**
 * TeraDenseArrayByte is the base class used to implement dense arrays with elements of size 4 bit or 8 bit.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public abstract class TeraDenseArrayByte extends TeraDenseArray {

    protected byte[] data; 
    
    protected abstract TeraArray createDense(byte[] data);
    
    protected abstract int rowSize();
    
    protected final int dataSize() {
        return getSizeY() * rowSize();
    }

    @Override
    protected void initialize() {
        this.data = new byte[dataSize()];
    }
    
    protected static abstract class SerializationHandler<T extends TeraDenseArrayByte> extends TeraArray.BasicSerializationHandler<T> {
        
        protected abstract T createArray(int sizeX, int sizeY, int sizeZ, byte[] data);
        
        @Override
        protected int internalComputeMinimumBufferSize(T array) {
            final byte[] data = array.data;
            if (data == null)
                return 4;
            else 
                return 4 + data.length;
        }
        
        @Override
        protected void internalSerialize(T array, ByteBuffer buffer) {
            final byte[] data = array.data;
            if (data == null)
                buffer.putInt(0);
            else {
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

    @Override
    public final int getEstimatedMemoryConsumptionInBytes() {
        if (data == null)
            return 4;
        else
            return 16 + data.length;
    }

    @Override
    public final TeraArray copy() {
        byte[] result = new byte[dataSize()];
        System.arraycopy(data, 0, result, 0, dataSize());
        return createDense(result);
    }
}
