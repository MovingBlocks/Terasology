package org.terasology.world.chunks.blockdata;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.google.common.base.Preconditions;

public abstract class TeraSparseArrayByte extends TeraSparseArray {

    protected byte[][] inflated;
    protected byte[] deflated;
    protected byte fill;
    
    public TeraSparseArrayByte() {
        super();
    }

    public TeraSparseArrayByte(int sizeX, int sizeY, int sizeZ, int sizeOfElementInBit) {
        super(sizeX, sizeY, sizeZ, sizeOfElementInBit);
        this.inflated = new byte[sizeY][];
        this.deflated = new byte[sizeY];
    }

    public TeraSparseArrayByte(int sizeX, int sizeY, int sizeZ, int sizeOfElementInBit, byte[][] inflated, byte[] deflated) {
        super(sizeX, sizeY, sizeZ, sizeOfElementInBit);
        this.inflated = Preconditions.checkNotNull(inflated);
        this.deflated = Preconditions.checkNotNull(deflated);
        Preconditions.checkArgument(inflated.length == sizeY);
        Preconditions.checkArgument(deflated.length == sizeY);
    }
    
    public TeraSparseArrayByte(int sizeX, int sizeY, int sizeZ, int sizeOfElementInBit, byte fill) {
        super(sizeX, sizeY, sizeZ, sizeOfElementInBit);
        this.inflated = null;
        this.deflated = null;
        this.fill = fill;
    }

    @Override
    public int getEstimatedMemoryConsumptionInBytes() {
        if (inflated == null)
            return 9;
        int result = 9 + sizeY + (sizeY * 16);
        for (int i = 0; i < sizeY; i++) {
            if (inflated[i] != null)
                result += sizeXZ;
        }
        return result;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writeExternalHeader(out);
        if (inflated == null) {
            out.writeBoolean(false);
            out.writeByte(fill);
            return;
        } 
        out.writeBoolean(true);
        int count = 0;
        for (int y = 0; y < sizeY; y++) {
            if (inflated[y] != null)
                ++count;
        }
        out.writeInt(count);
        for (int y = 0; y < sizeY; y++) {
            byte[] values = inflated[y];
            if (values != null) {
                out.writeInt(y);
                for (int i = 0; i < sizeXZ; i++) {
                    out.writeByte(values[i]);
                }
            }
        }
        for (int y = 0; y < sizeY; y++) {
            if (inflated[y] == null)
                out.writeByte(0);
            else
                out.writeByte(deflated[y]);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readExternalHeader(in);
        if (!in.readBoolean()) {
            inflated = null;
            deflated = null;
            fill = in.readByte();
            return;
        }
        inflated = new byte[sizeY][];
        deflated = new byte[sizeY];
        int count = in.readInt();
        Preconditions.checkArgument(count <= sizeY);
        for (int i = 0; i < count; i++) {
            int y = in.readInt();
            Preconditions.checkArgument(y <= sizeY);
            byte[] values = new byte[sizeXZ];
            for (int j = 0; j < sizeXZ; j++) {
                values[j] = in.readByte();
            }
            inflated[y] = values;
        }
        for (int y = 0; y < sizeY; y++) {
            deflated[y] = in.readByte();
        }
    }
}
