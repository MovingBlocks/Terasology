package org.terasology.world.chunks.blockdata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import org.terasology.world.chunks.deflate.TeraVisitingDeflator;

import com.google.common.base.Preconditions;

/**
 * TeraDenseArray16Bit implements a sparse array with elements of 16 bit size.
 * Its elements are in the range -32'768 through +32'767 and it internally uses the short type to store its elements.
 * It can reduce memory consumption through sparse memory allocation.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public class TeraSparseArray16Bit extends TeraSparseArray {

    protected short[][] inflated;
    protected short[] deflated;
    protected short fill;
    
    @Override
    protected void initialize() {}

    public static class SerializationHandler extends TeraArray.SerializationHandler<TeraSparseArray16Bit> {
        @Override
        public Class<TeraSparseArray16Bit> getArrayClass() {
            return TeraSparseArray16Bit.class;
        }
        @Override
        protected void internalSerialize(TeraSparseArray16Bit array, DataOutputStream out) throws IOException {
            final short[][] inflated = array.inflated;
            if (inflated == null) {
                out.writeBoolean(false);
                out.writeShort(array.fill);
            } else {
                final short[] deflated = array.deflated;
                final int sizeY = array.getSizeY();
                final int rowlen = array.getSizeXZ();
                out.writeBoolean(true);
                int rows = 0;
                for (final short[] row : inflated) {if (row != null) rows++;}
                out.writeInt(rows);
                for (int index = 0; index < sizeY; index++) {
                    final short[] row = inflated[index];
                    if (row == null) {
                        continue;
                    }
                    Preconditions.checkState(row.length == rowlen, "Unexpected row length in sparse array of type " + array.getClass().getName() + ", should be " + rowlen + " but is " + row.length);
                    out.writeInt(index);
                    for (final short b : row) {
                        out.writeShort(b);
                    }
                }
                for (int index = 0; index < sizeY; index++) {
                    if (inflated[index] == null)
                        out.writeShort(deflated[index]);
                    else 
                        out.writeShort(0);
                }
            }
        }
        @Override
        protected void internalDeserialize(TeraSparseArray16Bit array, DataInputStream in) throws IOException {
            Preconditions.checkState(array.inflated == null, "The internal array 'inflated' of type 'short[][]' is expected to be null");
            Preconditions.checkState(array.deflated == null, "The internal array 'deflated' of type 'short[]' is expected to be null");
            if (in.readBoolean()) {
                final int sizeY = array.getSizeY();
                final int rowlen = array.getSizeXZ();
                final short[][] inflated = array.inflated = new short[sizeY][];
                final int rows = in.readInt();
                for (int i = 0; i < rows; i++) {
                    final int index = in.readInt();
                    final short[] row = inflated[index] = new short[rowlen];
                    for (int j = 0; j < rowlen; j++) {
                        row[j] = in.readShort();
                    }
                }
                final short[] deflated = array.deflated = new short[sizeY];
                for (int i = 0; i < sizeY; i++) {
                    deflated[i] = in.readShort();
                }
            } else {
                array.fill = in.readShort();
            }
        }
    }

    public static class Factory implements TeraArrayFactory<TeraSparseArray16Bit> {
        @Override
        public Class<TeraSparseArray16Bit> getArrayClass() {
            return TeraSparseArray16Bit.class;
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
    public TeraArray copy() {
        if (inflated == null) 
            return new TeraSparseArray16Bit(getSizeX(), getSizeY(), getSizeZ(), fill);
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
        if (inflated == null)
            return 10;
        int result = 10 + (getSizeY() * 2) + (getSizeY() * 4);
        for (int i = 0; i < getSizeY(); i++) {
            if (inflated[i] != null)
                result += 12 + (getSizeXZ() * 2);
        }
        return result;
    }

    @Override
    public int getElementSizeInBits() {
        return 16;
    }

    @Override
    public int get(int x, int y, int z) {
        if (inflated == null) 
            return fill;
        short[] row = inflated[y];
        if (row != null)
            return row[pos(x, z)];
        return deflated[y];
    }

    @Override
    public int set(int x, int y, int z, int value) {
        if (inflated == null) {
            int old = fill;
            if (old == value)
                return old;
            else {
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
        if (old == value)
            return old;
        row = inflated[y] = new short[getSizeXZ()];
        Arrays.fill(row, deflated[y]);
        int pos = pos(x, z);
        row[pos] = (short) value;
        return deflated[y];
    }

    @Override
    public boolean set(int x, int y, int z, int value, int expected) {
        if (value == expected) return true;
        if (inflated == null) {
            int old = fill;
            if (old == value)
                return true;
            else {
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
            row = inflated[y] = new short[getSizeXZ()];
            Arrays.fill(row, deflated[y]);
            row[pos] = (short) value;
            return true;
        }
        return false;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writeExternalHeader(out);
        out.writeObject(inflated);
        out.writeObject(deflated);
        out.writeShort(fill);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readExternalHeader(in);
        inflated = (short[][]) in.readObject();
        deflated = (short[]) in.readObject();
        fill = in.readShort();
    }

}
