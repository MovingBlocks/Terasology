package org.terasology.world.chunks.blockdata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
    
    protected static abstract class SerializationHandler<T extends TeraDenseArrayByte> extends TeraArray.SerializationHandler<T> {
        @Override
        protected void internalSerialize(T array, DataOutputStream out) throws IOException {
            final byte[] data = array.data;
            if (data == null)
                out.writeInt(0);
            else {
                out.writeInt(data.length);
                for (byte b : data) {
                    out.writeByte(b);
                }
            }
        }
        @Override
        protected void internalDeserialize(T array, DataInputStream in) throws IOException {
            final byte[] data = array.data;
            final int length = in.readInt();
            Preconditions.checkNotNull(data);
            if (data.length != length)
                throw new IOException("The size of the array (" + data.length + ") does not match the size of the stored data (" + length + ")");
            for (int i = 0; i < length; i++) {
                data[i] = in.readByte();
            }
        }
    }
    
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

    @Override
    public final void writeExternal(ObjectOutput out) throws IOException {
        writeExternalHeader(out);
        out.writeObject(data);
    }

    @Override
    public final void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readExternalHeader(in);
        data = (byte[]) in.readObject();
    }
    
}
