package org.terasology.world.chunks.blockdata;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import com.google.common.base.Preconditions;

public abstract class TeraSparseArrayByte extends TeraSparseArray {

    protected byte[][] inflated;
    protected byte[] deflated;
    protected byte fill;
    
    protected abstract TeraArray createSparse(byte fill);
    
    protected abstract TeraArray createSparse(byte[][] inflated, byte[] deflated);

    protected abstract int rowSize();
    
    protected abstract int rowGet(int x, int z, byte value);

    protected abstract int rowGet(byte[] row, int x, int z);
    
    protected abstract int rowSet(byte[] row, int x, int z, int value);
    
    public TeraSparseArrayByte() {
        super();
    }

    public TeraSparseArrayByte(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ);
    }

    public TeraSparseArrayByte(int sizeX, int sizeY, int sizeZ, byte[][] inflated, byte[] deflated) {
        super(sizeX, sizeY, sizeZ);
        this.inflated = Preconditions.checkNotNull(inflated);
        this.deflated = Preconditions.checkNotNull(deflated);
        Preconditions.checkArgument(inflated.length == sizeY);
        Preconditions.checkArgument(deflated.length == sizeY);
    }
    
    public TeraSparseArrayByte(int sizeX, int sizeY, int sizeZ, byte fill) {
        super(sizeX, sizeY, sizeZ);
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
    public final TeraArray deflate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final TeraArray inflate() {
        throw new UnsupportedOperationException();
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
    public final int get(int x, int y, int z) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException("Index out of bounds (" + x + ", " + y + ", " + z + ")");
        if (inflated == null) 
            return rowGet(x, z, fill);
        byte[] row = inflated[y];
        if (row != null)
            return rowGet(row, x, z);
        return rowGet(x, z, deflated[y]);
    }

    @Override
    public final int set(int x, int y, int z, int value) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException("Index out of bounds (" + x + ", " + y + ", " + z + ")");
        if (value < -128 || value > 127) throw new IllegalArgumentException("Parameter 'value' has to be in the range of -128 - 127 (" + value + ")");
        if (inflated == null) {
            int old = rowGet(x, z, fill);
            if (old == value)
                return old;
            else {
                this.inflated = new byte[getSizeY()][];
                this.deflated = new byte[getSizeY()];
                Arrays.fill(deflated, fill);
            }
        }
        byte[] row = inflated[y];
        if (row != null)
            return rowSet(row, x, z, value);
        int old = rowGet(x, z, deflated[y]);
        if (old == value)
            return old;
        row = inflated[y] = new byte[rowSize()];
        Arrays.fill(row, deflated[y]);
        return rowSet(row, x, z, value);
    }

    @Override
    public final boolean set(int x, int y, int z, int value, int expected) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException("Index out of bounds (" + x + ", " + y + ", " + z + ")");
        if (value < -128 || value > 127) throw new IllegalArgumentException("Parameter 'value' has to be in the range of -128 - 127 (" + value + ")");
        if (expected < -128 || expected > 127) throw new IllegalArgumentException("Parameter 'expected' has to be in the range of -128 - 127 (" + value + ")");
        if (value == expected) return true;
        if (inflated == null) {
            int old = rowGet(x, z, fill);
            if (old == value)
                return true;
            else {
                this.inflated = new byte[getSizeY()][];
                this.deflated = new byte[getSizeY()];
                Arrays.fill(deflated, fill);
            }
        }
        byte[] row = inflated[y];
        if (row != null) {
            int old = rowGet(row, x, z);
            if (old == expected) {
                rowSet(row, x, z, value);
                return true;
            }
            return false;
        }
        int old = rowGet(x, z, deflated[y]);
        if (old == expected) {
            row = inflated[y] = new byte[rowSize()];
            Arrays.fill(row, deflated[y]);
            rowSet(row, x, z, value);
            return true;
        }
        return false;
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
