package org.terasology.world.chunks.blockdata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
    
    protected static abstract class SerializationHandler<T extends TeraSparseArrayByte> extends TeraArray.SerializationHandler<T> {
        @Override
        protected void internalSerialize(T array, DataOutputStream out) throws IOException {
            final byte[][] inflated = array.inflated;
            if (inflated == null) {
                out.writeBoolean(false);
                out.writeByte(array.fill);
            } else {
                final byte[] deflated = array.deflated;
                final int sizeY = array.getSizeY();
                final int rowlen = array.rowSize();
                out.writeBoolean(true);
                int rows = 0;
                for (final byte[] row : inflated) {if (row != null) rows++;}
                out.writeInt(rows);
                for (int index = 0; index < sizeY; index++) {
                    final byte[] row = inflated[index];
                    if (row == null) {
                        continue;
                    }
                    Preconditions.checkState(row.length == rowlen, "Unexpected row length in sparse array of type " + array.getClass().getName() + ", should be " + rowlen + " but is " + row.length);
                    out.writeInt(index);
                    for (final byte b : row) {
                        out.writeByte(b);
                    }
                }
                for (int index = 0; index < sizeY; index++) {
                    if (inflated[index] == null)
                        out.writeByte(deflated[index]);
                    else 
                        out.writeByte(0);
                }
            }
        }
        @Override
        protected void internalDeserialize(T array, DataInputStream in) throws IOException {
            Preconditions.checkState(array.inflated == null, "The internal array 'inflated' of type 'byte[][]' is expected to be null");
            Preconditions.checkState(array.deflated == null, "The internal array 'deflated' of type 'byte[]' is expected to be null");
            if (in.readBoolean()) {
                final int sizeY = array.getSizeY();
                final int rowlen = array.rowSize();
                final byte[][] inflated = array.inflated = new byte[sizeY][];
                final int rows = in.readInt();
                for (int i = 0; i < rows; i++) {
                    final int index = in.readInt();
                    final byte[] row = inflated[index] = new byte[rowlen];
                    for (int j = 0; j < rowlen; j++) {
                        row[j] = in.readByte();
                    }
                }
                final byte[] deflated = array.deflated = new byte[sizeY];
                for (int i = 0; i < sizeY; i++) {
                    deflated[i] = in.readByte();
                }
            } else {
                array.fill = in.readByte();
            }
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
