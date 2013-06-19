package org.terasology.world.chunks.perBlockStorage;

import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.protobuf.ChunksProtobuf.Type;


/**
 * TeraDenseArray8Bit implements a dense array with elements of 8 bit size.
 * Its elements are in the range -128 through +127 and it stores one element per byte.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public final class TeraDenseArray8Bit extends TeraDenseArrayByte { 

    @Override
    protected final TeraArray createDense(byte[] data) {
        return new TeraDenseArray8Bit(getSizeX(), getSizeY(), getSizeZ(), data);
    }

    @Override
    protected final int rowSize() {
        return getSizeXZ();
    }
    
    public static class Deflator implements TeraArray.Deflator<TeraDenseArray8Bit> {

        protected final static int DEFLATE_MINIMUM_8BIT = 16;

       /*  
        *  8-bit variant
        *  =============
        *  
        *  dense chunk  : 4 + 12 + 65536                                                   = 65552
        *  sparse chunk : (4 + 12 + 256) + (4 + 12 + (256 × 4)) + ((12 + 256) × 256)       = 69920
        *  difference   : 69920 - 65552                                                    =  4368
        *  min. deflate : 4368 / (12 + 256)                                                =    16.3
        *  
        */
        @Override
        public TeraArray deflate(TeraDenseArray8Bit array) {
            final int sizeX = array.getSizeX();
            final int sizeY = array.getSizeY();
            final int sizeZ = array.getSizeZ();
            final int rowSize = array.getSizeXZ();
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
                    return new TeraSparseArray8Bit(sizeX, sizeY, sizeZ, first);
            }
            if (packed > DEFLATE_MINIMUM_8BIT) {
                return new TeraSparseArray8Bit(sizeX, sizeY, sizeZ, inflated, deflated);
            }
            return null;
        }
    }

    public static class SerializationHandler extends TeraDenseArrayByte.SerializationHandler<TeraDenseArray8Bit> {

        @Override
        public Type getProtobufType() {
            return ChunksProtobuf.Type.DenseArray8Bit;
        }

        @Override
        protected TeraDenseArray8Bit createArray(int sizeX, int sizeY, int sizeZ, byte[] data) {
            if (data == null)
                return new TeraDenseArray8Bit(sizeX, sizeY, sizeZ);
            else
                return new TeraDenseArray8Bit(sizeX, sizeY, sizeZ, data);
        }
    }
    
    public static class Factory implements TeraArray.Factory {
        
        @Override
        public String getId() {
            return "8-bit-dense";
        }
        
        @Override
        public TeraDenseArray8Bit create(int sizeX, int sizeY, int sizeZ) {
            return new TeraDenseArray8Bit(sizeX, sizeY, sizeZ);
        }
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
    public int getElementSizeInBits() {
        return 8;
    }

    @Override
    public final int get(int x, int y, int z) {
        int pos = pos(x, y, z);
        return data[pos];
    }

    @Override
    public final int set(int x, int y, int z, int value) {
        int pos = pos(x, y, z);
        int old = data[pos];
        data[pos] = (byte) value;
        return old;
    }

    @Override
    public final boolean set(int x, int y, int z, int value, int expected) {
        int pos = pos(x, y, z);
        int old = data[pos];
        if (old == expected) {
            data[pos] = (byte) value;
            return true;
        }
        return false;
    }
}
