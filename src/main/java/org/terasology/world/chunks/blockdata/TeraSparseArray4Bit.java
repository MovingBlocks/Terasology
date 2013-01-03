package org.terasology.world.chunks.blockdata;

import java.util.Arrays;

import org.terasology.world.chunks.deflate.TeraVisitingDeflator;

import com.google.common.base.Preconditions;


/**
 * TeraSparseArray4Bit implements a sparse array with elements of 4 bit size.
 * Its elements are in the range 0 - 15 and it increases memory efficiency by storing two elements per byte.
 * It can further reduce memory consumption through sparse memory allocation.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
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

    private final int rowGet(int pos, byte value) {
        if (pos < getSizeXZHalf()) {
            return TeraArrays.getHi(value);
        }
        return TeraArrays.getLo(value);
    }

    private final int rowGet(byte[] row, int pos) {
        if (pos < getSizeXZHalf()) {
            return TeraArrays.getHi(row[pos]);
        }
        return TeraArrays.getLo(row[pos - getSizeXZHalf()]);
    }

    private final void rowSet(byte[] row, int pos, int value) {
        if (pos < getSizeXZHalf()) {
            byte raw = row[pos];
            row[pos] = TeraArrays.setHi(raw, value);
            return;
        }
        pos = pos - getSizeXZHalf();
        byte raw = row[pos];
        row[pos] = TeraArrays.setLo(raw, value);
    }

    private final int rowSetGetOld(byte[] row, int pos, int value) {
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
        super(sizeX, sizeY, sizeZ);
    }

    public TeraSparseArray4Bit(int sizeX, int sizeY, int sizeZ, byte[][] inflated, byte[] deflated) {
        super(sizeX, sizeY, sizeZ, inflated, deflated);
    }

    public TeraSparseArray4Bit(int sizeX, int sizeY, int sizeZ, byte fill) {
        super(sizeX, sizeY, sizeZ, fill);
        Preconditions.checkArgument(fill >= 0 && fill <= 15, "Parameter 'fill' has to be in the range 0 - 15 (" + fill + ")");
    }

    @Override
    public TeraArray deflate(TeraVisitingDeflator deflator) {
        return Preconditions.checkNotNull(deflator).deflateSparseArray4Bit(inflated, deflated, fill, rowSize(), getSizeX(), getSizeY(), getSizeZ());
    }

    @Override
    public int getElementSizeInBits() {
        return 4;
    }

    @Override
    public final int get(int x, int y, int z) {
        //        if (!contains(x, y, z)) throw new IndexOutOfBoundsException("Index out of bounds (" + x + ", " + y + ", " + z + ")");
        int pos = pos(x, z);
        if (inflated == null) 
            return rowGet(pos, fill);
        byte[] row = inflated[y];
        if (row != null)
            return rowGet(row, pos);
        return rowGet(pos, deflated[y]);
    }

    @Override
    public final int set(int x, int y, int z, int value) {
        //        if (!contains(x, y, z)) throw new IndexOutOfBoundsException("Index out of bounds (" + x + ", " + y + ", " + z + ")");
        //        if (value < -128 || value > 127) throw new IllegalArgumentException("Parameter 'value' has to be in the range of -128 - 127 (" + value + ")");
        int pos = pos(x, z);
        if (inflated == null) {
            int old = rowGet(pos, fill);
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
            return rowSetGetOld(row, pos, value);
        int old = rowGet(pos, deflated[y]);
        if (old == value)
            return old;
        row = inflated[y] = new byte[rowSize()];
        Arrays.fill(row, deflated[y]);
        return rowSetGetOld(row, pos, value);
    }

    @Override
    public final boolean set(int x, int y, int z, int value, int expected) {
//                if (!contains(x, y, z)) throw new IndexOutOfBoundsException("Index out of bounds (" + x + ", " + y + ", " + z + ")");
        //        if (value < -128 || value > 127) throw new IllegalArgumentException("Parameter 'value' has to be in the range of -128 - 127 (" + value + ")");
        //        if (expected < -128 || expected > 127) throw new IllegalArgumentException("Parameter 'expected' has to be in the range of -128 - 127 (" + value + ")");
        if (value == expected) return true;
        int pos = pos(x, z);
        if (inflated == null) {
            int old = rowGet(pos, fill);
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
            int old = rowGet(row, pos);
            if (old == expected) {
                rowSet(row, pos, value);
                return true;
            }
            return false;
        }
        int old = rowGet(pos, deflated[y]);
        if (old == expected) {
            row = inflated[y] = new byte[rowSize()];
            Arrays.fill(row, deflated[y]);
            rowSet(row, pos, value);
            return true;
        }
        return false;
    }

}
