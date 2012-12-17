package org.terasology.world.chunks.blockdata;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public abstract class TeraDenseArrayByte extends TeraDenseArray {

    protected byte[] data; 

    public TeraDenseArrayByte() {
        super();
    }

    public TeraDenseArrayByte(int sizeX, int sizeY, int sizeZ, int sizeOfElementInBit) {
        super(sizeX, sizeY, sizeZ, sizeOfElementInBit);
    }

    @Override
    public int estimatedMemoryConsumptionInBytes() {
        return data.length;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writeExternalHeader(out);
        out.writeInt(data.length);
        for (int i = 0; i < data.length; i++) {
            out.writeByte(data[i]);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readExternalHeader(in);
        int length = in.readInt();
        data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = in.readByte();
        }
    }
}
