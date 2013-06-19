package org.terasology.world.chunks.perBlockStorage;

import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.protobuf.ChunksProtobuf.Type;


/**
 * TeraDenseArray4Bit implements a dense array with elements of 4 bit size.
 * Its elements are in the range 0 - 15 and it increases memory efficiency by storing two elements per byte.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public final class TeraDenseArray4Bit extends TeraDenseArrayByte {

    @Override
    protected final TeraArray createDense(byte[] data) {
        return new TeraDenseArray4Bit(getSizeX(), getSizeY(), getSizeZ(), data);
    }

    @Override
    protected final int rowSize() {
        return getSizeXZHalf();
    }
    
    public static class Deflator implements TeraArray.Deflator<TeraDenseArray4Bit> {
        
        protected final static int DEFLATE_MINIMUM_4BIT = 31;

       /*  
        *  4-bit variant
        *  =============
        *  
        *  dense chunk  : 4 + 12 + (65536 / 2)                                             = 32784
        *  sparse chunk : (4 + 12 + 256) + (4 + 12 + (256 × 4)) + ((12 + (256 / 2)) × 256) = 37152
        *  difference   : 37152 - 32784                                                    =  4368
        *  min. deflate : 4368 / (12 + (256 / 2))                                          =    31.2
        *  
        */
        @Override
        public TeraArray deflate(TeraDenseArray4Bit array) {
            final int sizeX = array.getSizeX();
            final int sizeY = array.getSizeY();
            final int sizeZ = array.getSizeZ();
            final int rowSize = array.getSizeXZHalf();
            final byte[] data = array.data;
            final byte[][] inflated = new byte[sizeY][];
            final byte[] deflated = new byte[sizeY];
            int packed = 0;
            for (int y = 0; y < sizeY; y++) {
                final int start = y * rowSize;
                final byte first = data[start];
                boolean packable = true;
                for (int i = 1; i < rowSize; i++) {
                    if (data[start + i] != first) {
                        packable = false;
                        break;
                    }
                }
                if (packable) {
                    deflated[y] = first;
                    ++packed;
                } else {
                    byte[] tmp = new byte[rowSize];
                    System.arraycopy(data, start, tmp, 0, rowSize);
                    inflated[y] = tmp;
                }
            }
            if (packed == sizeY) {
                final byte first = deflated[0];
                boolean packable = true;
                for (int i = 1; i < sizeY; i++) {
                    if (deflated[i] != first) {
                        packable = false;
                        break;
                    }
                }
                if (packable)
                    return new TeraSparseArray4Bit(sizeX, sizeY, sizeZ, first);
            }
            if (packed > DEFLATE_MINIMUM_4BIT) {
                return new TeraSparseArray4Bit(sizeX, sizeY, sizeZ, inflated, deflated);
            }
            return null;
        }
    }
    
    public static class SerializationHandler extends TeraDenseArrayByte.SerializationHandler<TeraDenseArray4Bit> {

        @Override
        public Type getProtobufType() {
            return ChunksProtobuf.Type.DenseArray4Bit;
        }

        @Override
        protected TeraDenseArray4Bit createArray(int sizeX, int sizeY, int sizeZ, byte[] data) {
            if (data == null) 
                return new TeraDenseArray4Bit(sizeX, sizeY, sizeZ);
            else
                return new TeraDenseArray4Bit(sizeX, sizeY, sizeZ, data);
        }
    }
    
    public static class Factory implements TeraArray.Factory {
        
        @Override
        public String getId() {
            return "4-bit-dense";
        }
        
        @Override
        public TeraDenseArray4Bit create(int sizeX, int sizeY, int sizeZ) {
            return new TeraDenseArray4Bit(sizeX, sizeY, sizeZ);
        }
    }

    public TeraDenseArray4Bit(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ);
    }

    public TeraDenseArray4Bit(int sizeX, int sizeY, int sizeZ, byte[] data) {
        super(sizeX, sizeY, sizeZ, data);
    }

    public TeraDenseArray4Bit(TeraArray in) {
        super(in);
    }

    @Override
    public int getElementSizeInBits() {
        return 4;
    }
    
    @Override
    public final int get(int x, int y, int z) {
        int row = y * getSizeXZHalf(), pos = pos(x, z);
        if (pos < getSizeXZHalf()) {
            return TeraArrayUtils.getHi(data[row + pos]);
        }
        pos = pos - getSizeXZHalf();
        return TeraArrayUtils.getLo(data[row + pos]);
    }

    @Override
    public final int set(int x, int y, int z, int value) {
        int row = y * getSizeXZHalf(), pos = pos(x, z);
        if (pos < getSizeXZHalf()) {
            byte raw = data[row + pos];
            byte old = TeraArrayUtils.getHi(raw);
            data[row + pos] = TeraArrayUtils.setHi(raw, value);
            return old;
        }
        pos = pos - getSizeXZHalf();
        byte raw = data[row + pos];
        byte old = TeraArrayUtils.getLo(raw);
        data[row + pos] = TeraArrayUtils.setLo(raw, value);
        return old;
    }

    @Override
    public final boolean set(int x, int y, int z, int value, int expected) {
        int row = y * getSizeXZHalf(), pos = pos(x, z);
        if (pos < getSizeXZHalf()) {
            byte raw = data[row + pos];
            byte old = TeraArrayUtils.getHi(raw);
            if (old == expected) {
                data[row + pos] = TeraArrayUtils.setHi(raw, value);
                return true;
            }
            return false;
        }
        pos = pos - getSizeXZHalf();
        byte raw = data[row + pos];
        byte old = TeraArrayUtils.getLo(raw);
        if (old == expected) {
            data[row + pos] = TeraArrayUtils.setLo(raw, value);
            return true;
        }
        return false;
    }
}
