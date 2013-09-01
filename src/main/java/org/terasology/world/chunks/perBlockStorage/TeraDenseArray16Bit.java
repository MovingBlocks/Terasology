package org.terasology.world.chunks.perBlockStorage;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.protobuf.ChunksProtobuf.Type;

import com.google.common.base.Preconditions;

/**
 * TeraDenseArray16Bit implements a dense array with elements of 16 bit size.
 * Its elements are in the range -32'768 through +32'767 and it internally uses the short type to store its elements.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public class TeraDenseArray16Bit extends TeraDenseArray {

    protected short[] data;
    
    @Override
    protected void initialize() {
        this.data = new short[getSizeXYZ()];
    }

    public static class Deflator implements TeraArray.Deflator<TeraDenseArray16Bit> {

        /*
         *  16-bit variant
         *  ==============
         *  
         *  dense chunk  : 4 + 12 + (65536 * 2)                                                   = 131088
         *  sparse chunk : (4 + 12 + (256 * 2)) + (4 + 12 + (256 × 4)) + ((12 + (256 * 2)) × 256) = 135712
         *  difference   : 135712 - 131088                                                        =   4624
         *  min. deflate : 4624 / (12 + (256 * 2))                                                =      8.8
         *  
         */
        protected final static int DEFLATE_MINIMUM_16BIT = 8;
        
        @Override
        public TeraArray deflate(TeraDenseArray16Bit array) {
            final int sizeX = array.getSizeX();
            final int sizeY = array.getSizeY();
            final int sizeZ = array.getSizeZ();
            final int rowSize = array.getSizeXZ();
            final short[] data = array.data;
            final short[][] inflated = new short[sizeY][];
            final short[] deflated = new short[sizeY];
            int packed = 0;
            for (int y = 0; y < sizeY; y++) {
                final int start = y * rowSize;
                final short first = data[start];
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
                    short[] tmp = new short[rowSize];
                    System.arraycopy(data, start, tmp, 0, rowSize);
                    inflated[y] = tmp;
                }
            }
            if (packed == sizeY) {
                final short first = deflated[0];
                boolean packable = true;
                for (int i = 1; i < sizeY; i++) {
                    if (deflated[i] != first) {
                        packable = false;
                        break;
                    }
                }
                if (packable)
                    return new TeraSparseArray16Bit(sizeX, sizeY, sizeZ, first);
            }
            if (packed > DEFLATE_MINIMUM_16BIT) {
                return new TeraSparseArray16Bit(sizeX, sizeY, sizeZ, inflated, deflated);
            }
            return null;
        }
    }
    
    public static class SerializationHandler extends TeraArray.BasicSerializationHandler<TeraDenseArray16Bit> {

        @Override
        public Type getProtobufType() {
            return ChunksProtobuf.Type.DenseArray16Bit;
        }
        
        @Override
        protected int internalComputeMinimumBufferSize(TeraDenseArray16Bit array) {
            final short[] data = array.data;
            if (data == null)
                return 4;
            else 
                return 4 + data.length * 2;
        }

        @Override
        protected void internalSerialize(TeraDenseArray16Bit array, ByteBuffer buffer) {
            final short[] data = array.data;
            if (data == null) 
                buffer.putInt(0);
            else {
                buffer.putInt(data.length);
                final ShortBuffer sbuffer = buffer.asShortBuffer();
                sbuffer.put(data);
                buffer.position(buffer.position() + data.length * 2);
            }
        }

        @Override
        protected TeraDenseArray16Bit internalDeserialize(int sizeX, int sizeY, int sizeZ, ByteBuffer buffer) {
            final int length = buffer.getInt();
            if (length > 0) {
                final short[] data = new short[length];
                final ShortBuffer sbuffer = buffer.asShortBuffer();
                sbuffer.get(data, 0, length);
                buffer.position(buffer.position() + length * 2);
                return new TeraDenseArray16Bit(sizeX, sizeY, sizeZ, data);
            }
            return new TeraDenseArray16Bit(sizeX, sizeY, sizeZ);
        }
    }
    
    public static class Factory implements TeraArray.Factory {
        
        @Override
        public String getId() {
            return "16-bit-dense";
        }
        
        @Override
        public TeraDenseArray16Bit create(int sizeX, int sizeY, int sizeZ) {
            return new TeraDenseArray16Bit(sizeX, sizeY, sizeZ);
        }
    }

    public TeraDenseArray16Bit(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ, true);
    }
    
    public TeraDenseArray16Bit(int sizeX, int sizeY, int sizeZ, short[] data) {
        super(sizeX, sizeY, sizeZ, false);
        this.data = Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.length == getSizeXYZ(), "The length of parameter 'data' has to be " + getSizeXYZ() + " but is " + data.length);
    }    

    public TeraDenseArray16Bit(TeraArray in) {
        super(in);
    }

    @Override
    public TeraArray copy() {
        short[] tmp = new short[getSizeXYZ()];
        System.arraycopy(data, 0, tmp, 0, getSizeXYZ());
        return new TeraDenseArray16Bit(getSizeX(), getSizeY(), getSizeZ(), tmp);
    }

    @Override
    public int getEstimatedMemoryConsumptionInBytes() {
        if (data == null)
            return 4;
        else
            return 16 + data.length * 2;
    }

    @Override
    public int getElementSizeInBits() {
        return 16;
    }

    @Override
    public int get(int x, int y, int z) {
        return data[pos(x, y, z)];
    }

    @Override
    public int set(int x, int y, int z, int value) {
        int pos = pos(x, y, z);
        int old = data[pos];
        data[pos] = (short) value;
        return old;
    }

    @Override
    public boolean set(int x, int y, int z, int value, int expected) {
        int pos = pos(x, y, z);
        int old = data[pos];
        if (old == expected) {
            data[pos] = (short) value;
            return true;
        }
        return false;
    }
}
