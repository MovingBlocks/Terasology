package org.terasology.world.chunks.blockdata;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.google.common.base.Preconditions;

public abstract class TeraDenseArrayByte extends TeraDenseArray {

    protected byte[] data; 
    
    protected abstract TeraArray createSparse(byte fill);
    
    protected abstract TeraArray createSparse(byte[][] inflated, byte[] deflated);

    protected abstract TeraArray createDense(byte[] data);
    
    protected abstract int rowSize();
    
    protected final int dataSize() {
        return getSizeY() * rowSize();
    }

    public TeraDenseArrayByte() {
        super();
    }

    public TeraDenseArrayByte(int sizeX, int sizeY, int sizeZ, int sizeOfElementInBit) {
        super(sizeX, sizeY, sizeZ, sizeOfElementInBit);
        this.data = new byte[dataSize()];
    }
    
    public TeraDenseArrayByte(int sizeX, int sizeY, int sizeZ, int sizeOfElementInBit, byte[] data) {
        super(sizeX, sizeY, sizeZ, sizeOfElementInBit);
        this.data = Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.length == dataSize(), "The length of the parameter 'data' has to be " + dataSize() + " (" + data.length + ")");
    }

    @Override
    public final int getEstimatedMemoryConsumptionInBytes() {
        return 16 + dataSize();
    }

    @Override
    public final TeraArray deflate() {
        final int rowsize = rowSize();
        final byte[][] inflated = new byte[getSizeY()][];
        final byte[] deflated = new byte[getSizeY()];
        int packed = 0;
        for (int y = 0; y < getSizeY(); y++) {
            final int start = y * rowsize;
            final byte first = data[start];
            boolean packable = true;
            for (int i = 1; i < rowsize; i++) {
                if (data[start + i] != first) {
                    packable = false;
                    break;
                }
            }
            if (packable) {
                deflated[y] = first;
                ++packed;
            } else {
                byte[] tmp = new byte[rowsize];
                System.arraycopy(data, start, tmp, 0, rowsize);
                inflated[y] = tmp;
            }
        }
        if (packed == getSizeY()) {
            final byte first = deflated[0];
            boolean packable = true;
            for (int i = 1; i < getSizeY(); i++) {
                if (deflated[i] != first) {
                    packable = false;
                    break;
                }
            }
            if (packable)
                return createSparse(first);
        }
        if (packed >= 4) {
            return createSparse(inflated, deflated);
        }
        return this;
    }

    @Override
    public final TeraArray inflate() {
        return this;
    }

    @Override
    public final TeraArray copy() {
        byte[] result = new byte[dataSize()];
        System.arraycopy(data, 0, result, 0, dataSize());
        return createDense(result);
    }

    @Override
    public final void writeExternal(ObjectOutput out) throws IOException {
        writeExternalHeader(out);
        out.writeObject(data);
    }

    @Override
    public final void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readExternalHeader(in);
        data = (byte[]) in.readObject();
    }
}
