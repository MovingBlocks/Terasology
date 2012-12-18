package org.terasology.world.chunks.blockdata;

import com.google.common.base.Preconditions;

public final class TeraSparseArray4Bit extends TeraSparseArrayByte {

    @Override
    protected final TeraArray createSparse(byte fill) {
        return new TeraSparseArray4Bit(getSizeX(), getSizeY(), getSizeZ(), fill);
    }

    @Override
    protected final TeraArray createSparse(byte[][] inflated, byte[] deflated) {
        return new TeraSparseArray4Bit(getSizeX(), getSizeY(), getSizeZ(), inflated, deflated);
    }

    @Override
    protected final int rowSize() {
        return getSizeXZHalf();
    }

    @Override
    protected final int rowGet(int x, int z, byte value) {
        int pos = z * getSizeX() + x;
        if (pos < getSizeXZHalf()) {
            return TeraArrays.getHi(value);
        }
        return TeraArrays.getLo(value);
    }

    @Override
    protected final int rowGet(byte[] row, int x, int z) {
        int pos = z * getSizeX() + x;
        if (pos < getSizeXZHalf()) {
            return TeraArrays.getHi(row[pos]);
        }
        return TeraArrays.getLo(row[pos - getSizeXZHalf()]);
    }

    @Override
    protected final int rowSet(byte[] row, int x, int z, int value) {
        int pos = z * getSizeX() + x;
        if (pos < getSizeXZHalf()) {
            byte raw = row[pos];
            byte old = TeraArrays.getHi(raw);
            row[pos] = TeraArrays.setHi(raw, value);
            return old;
        }
        pos = pos - getSizeXZHalf();
        byte raw = row[pos];
        byte old = TeraArrays.getLo(raw);
        row[pos] = TeraArrays.setLo(raw, value);
        return old;
    }

    public TeraSparseArray4Bit() {
        super();
    }

    public TeraSparseArray4Bit(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ, 4);
    }

    public TeraSparseArray4Bit(int sizeX, int sizeY, int sizeZ, byte[][] inflated, byte[] deflated) {
        super(sizeX, sizeY, sizeZ, 4, inflated, deflated);
    }

    public TeraSparseArray4Bit(int sizeX, int sizeY, int sizeZ, byte fill) {
        super(sizeX, sizeY, sizeZ, 4, fill);
        Preconditions.checkArgument(fill >= 0 && fill <= 15, "Parameter 'fill' has to be in the range 0 - 15 (" + fill + ")");
    }
}
