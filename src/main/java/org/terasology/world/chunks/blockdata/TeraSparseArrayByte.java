package org.terasology.world.chunks.blockdata;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;

import com.google.common.base.Preconditions;


/**
 * TeraSparseArrayByte is the base class used to implement sparse arrays with elements of size 4 bit or 8 bit.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public abstract class TeraSparseArrayByte extends TeraSparseArray {

    protected byte[][] inflated;
    protected byte[] deflated;
    protected byte fill;
    
    protected abstract TeraArray createSparse(byte fill);
    
    protected abstract TeraArray createSparse(byte[][] inflated, byte[] deflated);

    protected abstract int rowSize();
    
    @Override
    protected void initialize() {}
    
    protected static abstract class SerializationHandler<T extends TeraSparseArrayByte> extends TeraArray.BasicSerializationHandler<T> {

        protected abstract T createArray(int sizeX, int sizeY, int sizeZ);

        @Override
        protected int internalComputeMinimumBufferSize(T array) {
            final byte[][] inf = array.inflated;
            if (inf == null) 
                return 2;
            else {
                final int sizeY = array.getSizeY(), rowSize = array.rowSize();
                int result = 1;
                for (int y = 0; y < sizeY; y++)
                    if (inf[y] == null) 
                        result += 2;
                    else 
                        result += 1 + rowSize;
                return result;
            }
        }

        @Override
        protected void internalSerialize(T array, ByteBuffer buffer) {
            final byte[][] inf = array.inflated;
            if (inf == null) {
                buffer.put((byte) 0);
                buffer.put(array.fill);
            } else {
                buffer.put((byte) 1);
                final int sizeY = array.getSizeY(), rowSize = array.rowSize();
                final byte[] def = array.deflated;
                for (int y = 0; y < sizeY; y++) {
                    final byte[] row = inf[y];
                    if (row == null) {
                        buffer.put((byte) 0);
                        buffer.put(def[y]);
                    } else {
                        buffer.put((byte) 1);
                        buffer.put(row, 0, rowSize);
                    }
                }
            }
        }

        @Override
        protected T internalDeserialize(int sizeX, int sizeY, int sizeZ, ByteBuffer buffer) {
            final byte hasData = buffer.get();
            final T array = createArray(sizeX, sizeY, sizeZ);
            if (hasData == 0) {
                array.fill = buffer.get();
                return array;
            }
            final int rowSize = array.rowSize();
            final byte[][] inf = array.inflated = new byte[sizeY][];
            final byte[] def = array.deflated = new byte[sizeY];
            for (int y = 0; y < sizeY; y++) {
                final byte hasRow = buffer.get();
                if (hasRow == 0)
                    def[y] = buffer.get();
                else {
                    final byte[] row = inf[y] = new byte[rowSize];
                    buffer.get(row, 0, rowSize);
                }
            }
            return array;
        }
    }
    
    protected TeraSparseArrayByte() {
        super();
    }

    protected TeraSparseArrayByte(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ, false);
    }

    protected TeraSparseArrayByte(int sizeX, int sizeY, int sizeZ, byte[][] inflated, byte[] deflated) {
        super(sizeX, sizeY, sizeZ, false);
        this.inflated = Preconditions.checkNotNull(inflated);
        this.deflated = Preconditions.checkNotNull(deflated);
        Preconditions.checkArgument(inflated.length == sizeY, "The length of parameter 'inflated' has to be " + sizeY + " but is " + inflated.length);
        Preconditions.checkArgument(deflated.length == sizeY, "The length of parameter 'deflated' has to be " + sizeY + " but is " + deflated.length);
    }
    
    protected TeraSparseArrayByte(int sizeX, int sizeY, int sizeZ, byte fill) {
        super(sizeX, sizeY, sizeZ, false);
        this.fill = fill;
    }

    @Override
    public final int getEstimatedMemoryConsumptionInBytes() {
        if (inflated == null)
            return 9;
        int result = 9 + getSizeY() + (getSizeY() * 4);
        for (int i = 0; i < getSizeY(); i++) {
            if (inflated[i] != null)
                result += 12 + rowSize();
        }
        return result;
    }

    @Override
    public final TeraArray copy() {
        if (inflated == null) 
            return createSparse(fill);
        byte[][] inf = new byte[getSizeY()][];
        byte[] def = new byte[getSizeY()];
        for (int y = 0; y < getSizeY(); y++) {
            if (inflated[y] != null) {
                inf[y] = new byte[rowSize()];
                System.arraycopy(inflated[y], 0, inf[y], 0, rowSize());
            }
        }
        System.arraycopy(deflated, 0, def, 0, getSizeY());
        return createSparse(inf, def);
    }

    @Override
    public final void writeExternal(ObjectOutput out) throws IOException {
        writeExternalHeader(out);
        out.writeObject(inflated);
        out.writeObject(deflated);
        out.writeByte(fill);
    }

    @Override
    public final void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readExternalHeader(in);
        inflated = (byte[][]) in.readObject();
        deflated = (byte[]) in.readObject();
        fill = in.readByte();
    }
}
