package org.terasology.world.chunks.blockdata;


/**
 * TeraDenseArray8Bit implements a dense array with elements of 8 bit size.
 * Its elements are in the range -128 through +127 and it stores one element per byte.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public final class TeraDenseArray8Bit extends TeraDenseArrayByte { 

    @Override
    protected final TeraArray createSparse(byte fill) {
        return new TeraSparseArray8Bit(getSizeX(), getSizeY(), getSizeZ(), fill);
    }

    @Override
    protected final TeraArray createSparse(byte[][] inflated, byte[] deflated) {
        return new TeraSparseArray8Bit(getSizeX(), getSizeY(), getSizeZ(), inflated, deflated);
    }

    @Override
    protected final TeraArray createDense(byte[] data) {
        return new TeraDenseArray8Bit(getSizeX(), getSizeY(), getSizeZ(), data);
    }

    @Override
    protected final int rowSize() {
        return getSizeXZ();
    }

    public TeraDenseArray8Bit() {
        super();
    }

    public TeraDenseArray8Bit(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ);
    }
    
    public TeraDenseArray8Bit(int sizeX, int sizeY, int sizeZ, byte[] data) {
        super(sizeX, sizeY, sizeZ, data);
    }
    
    public TeraDenseArray8Bit(TeraArray in) {
        super(in);
    }

    @Override
    public final int get(int x, int y, int z) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException("Index out of bounds (" + x + ", " + y + ", " + z + ")");
        int pos = pos(x, y, z);
        return data[pos];
    }

    @Override
    public final int set(int x, int y, int z, int value) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException("Index out of bounds (" + x + ", " + y + ", " + z + ")");
        if (value < -128 || value > 127) throw new IllegalArgumentException("Parameter 'value' has to be in the range of -128 - 127 (" + value + ")");
        int pos = pos(x, y, z);
        int old = data[pos];
        data[pos] = (byte) value;
        return old;
    }

    @Override
    public final boolean set(int x, int y, int z, int value, int expected) {
        if (!contains(x, y, z)) throw new IndexOutOfBoundsException("Index out of bounds (" + x + ", " + y + ", " + z + ")");
        if (value < -128 || value > 127) throw new IllegalArgumentException("Parameter 'value' has to be in the range of -128 - 127 (" + value + ")");
        if (expected < -128 || expected > 127) throw new IllegalArgumentException("Parameter 'expected' has to be in the range of -128 - 127 (" + value + ")");
        int pos = pos(x, y, z);
        int old = data[pos];
        if (old == expected) {
            data[pos] = (byte) value;
            return true;
        }
        return false;
    }
}
