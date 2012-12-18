package org.terasology.world.chunks.blockdata;

public final class TeraSparseArray8Bit extends TeraSparseArrayByte {

    @Override
    protected final TeraArray createSparse(byte fill) {
        return new TeraSparseArray8Bit(getSizeX(), getSizeY(), getSizeZ(), fill);
    }

    @Override
    protected final TeraArray createSparse(byte[][] inflated, byte[] deflated) {
        return new TeraSparseArray8Bit(getSizeX(), getSizeY(), getSizeZ(), inflated, deflated);
    }

    @Override
    protected final int rowSize() {
        return getSizeXZ();
    }

    @Override
    protected final int rowGet(int x, int z, byte value) {
        return value;
    }

    @Override
    protected final int rowGet(byte[] row, int x, int z) {
        return row[z * getSizeX() + x];
    }

    @Override
    protected final int rowSet(byte[] row, int x, int z, int value) {
        int pos = z * getSizeX() + x;
        int old = row[pos];
        row[pos] = (byte) value;
        return old;
    }

    public TeraSparseArray8Bit() {
        super();
    }

    public TeraSparseArray8Bit(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ);
    }

    public TeraSparseArray8Bit(int sizeX, int sizeY, int sizeZ, byte[][] inflated, byte[] deflated) {
        super(sizeX, sizeY, sizeZ, inflated, deflated);
    }

    public TeraSparseArray8Bit(int sizeX, int sizeY, int sizeZ, byte fill) {
        super(sizeX, sizeY, sizeZ, fill);
    }
}
