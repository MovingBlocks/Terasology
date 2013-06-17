package org.terasology.world.chunks.perBlockStorage;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.protobuf.ChunksProtobuf.Type;
import org.terasology.world.chunks.deflate.TeraVisitingDeflator;

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

    public static class SerializationHandler extends TeraArray.BasicSerializationHandler<TeraDenseArray16Bit> {

        @Override
        public boolean canHandle(Class<?> clazz) {
            return TeraDenseArray16Bit.class.equals(clazz);
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
    
    public static class Factory implements TeraArray.Factory<TeraDenseArray16Bit> {
        
        @Override
        public String getId() {
            return "16-bit-dense";
        }
        
        @Override
        public Class<TeraDenseArray16Bit> getArrayClass() {
            return TeraDenseArray16Bit.class;
        }

        @Override
        public Type getProtobufType() {
            return ChunksProtobuf.Type.DenseArray16Bit;
        }

        @Override
        public SerializationHandler createSerializationHandler() {
            return new SerializationHandler();
        }
        
        @Override
        public TeraDenseArray16Bit create() {
            return new TeraDenseArray16Bit();
        }
        
        @Override
        public TeraDenseArray16Bit create(int sizeX, int sizeY, int sizeZ) {
            return new TeraDenseArray16Bit(sizeX, sizeY, sizeZ);
        }
    }

    public TeraDenseArray16Bit() {
        super();
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
    public TeraArray deflate(TeraVisitingDeflator deflator) {
        return Preconditions.checkNotNull(deflator).deflateDenseArray16Bit(data, getSizeXZ(), getSizeX(), getSizeY(), getSizeZ());
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

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writeExternalHeader(out);
        out.writeObject(data);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readExternalHeader(in);
        data = (short[]) in.readObject();
    } 

}
