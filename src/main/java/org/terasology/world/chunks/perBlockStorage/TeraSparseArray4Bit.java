package org.terasology.world.chunks.perBlockStorage;

import java.util.Arrays;

import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.protobuf.ChunksProtobuf.Type;

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
            return TeraArrayUtils.getHi(value);
        }
        return TeraArrayUtils.getLo(value);
    }

    private final int rowGet(byte[] row, int pos) {
        if (pos < getSizeXZHalf()) {
            return TeraArrayUtils.getHi(row[pos]);
        }
        return TeraArrayUtils.getLo(row[pos - getSizeXZHalf()]);
    }

    private final void rowSet(byte[] row, int pos, int value) {
        if (pos < getSizeXZHalf()) {
            byte raw = row[pos];
            row[pos] = TeraArrayUtils.setHi(raw, value);
            return;
        }
        pos = pos - getSizeXZHalf();
        byte raw = row[pos];
        row[pos] = TeraArrayUtils.setLo(raw, value);
    }

    private final int rowSetGetOld(byte[] row, int pos, int value) {
        if (pos < getSizeXZHalf()) {
            byte raw = row[pos];
            byte old = TeraArrayUtils.getHi(raw);
            row[pos] = TeraArrayUtils.setHi(raw, value);
            return old;
        }
        pos = pos - getSizeXZHalf();
        byte raw = row[pos];
        byte old = TeraArrayUtils.getLo(raw);
        row[pos] = TeraArrayUtils.setLo(raw, value);
        return old;
    }

    public static final class SerializationHandler extends TeraSparseArrayByte.SerializationHandler<TeraSparseArray4Bit> {

        @Override
        public Type getProtobufType() {
            return ChunksProtobuf.Type.SparseArray4Bit;
        }

        @Override
        protected TeraSparseArray4Bit createArray(int sizeX, int sizeY, int sizeZ) {
            return new TeraSparseArray4Bit(sizeX, sizeY, sizeZ);
        }
    }
    
    public static class Factory implements TeraArray.Factory {
        
        @Override
        public String getId() {
            return "4-bit-sparse";
        }
        
        @Override
        public TeraSparseArray4Bit create(int sizeX, int sizeY, int sizeZ) {
            return new TeraSparseArray4Bit(sizeX, sizeY, sizeZ);
        }
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
    public int getElementSizeInBits() {
        return 4;
    }

    @Override
    public final int get(int x, int y, int z) {
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
