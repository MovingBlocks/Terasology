package org.terasology.world.chunks.blockdata;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.google.common.base.Preconditions;


public abstract class TeraArray implements Externalizable {

    protected int sizeX, sizeY, sizeZ, sizeOfElementInBit, sizeXZ, sizeXYZ;

    protected void writeExternalHeader(ObjectOutput out) throws IOException {
        out.writeInt(sizeX);
        out.writeInt(sizeY);
        out.writeInt(sizeZ); 
        out.writeInt(sizeOfElementInBit);
    }

    protected void readExternalHeader(ObjectInput in) throws IOException {
        sizeX = in.readInt();
        sizeY = in.readInt();
        sizeZ = in.readInt();
        sizeOfElementInBit = in.readInt();
        sizeXZ = sizeX * sizeZ;
        sizeXYZ = sizeY * sizeXZ;
    }

    public TeraArray() {}

    public TeraArray(int sizeX, int sizeY, int sizeZ, int sizeOfElementInBit) {
        Preconditions.checkArgument(sizeX > 0);
        Preconditions.checkArgument(sizeY > 0);
        Preconditions.checkArgument(sizeZ > 0);
        Preconditions.checkArgument(sizeOfElementInBit > 0 && sizeOfElementInBit <= 32);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.sizeOfElementInBit = sizeOfElementInBit;
        this.sizeXZ = sizeX * sizeZ;
        this.sizeXYZ = sizeY * sizeXZ;
    }

    public final int getSizeX() {
        return sizeX;
    }

    public final int getSizeY() {
        return sizeY;
    }

    public final int getSizeZ() {
        return sizeZ;
    }

    public final int getSizeOfElementInBit() {
        return sizeOfElementInBit;
    }

    public final int getSizeXZ() {
        return sizeXZ;
    }

    public final int getSizeXYZ() {
        return sizeXYZ;
    }

    public final boolean contains(int x, int y, int z) {
        return (x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ);
    }

    public abstract boolean isPacked();

    public abstract TeraArray pack();

    public abstract TeraArray copy();

    public abstract int estimatedMemoryConsumptionInBytes();

    public abstract int get(int x, int y, int z);

    public abstract int set(int x, int y, int z, int value);

    public abstract boolean set(int x, int y, int z, int value, int expected);

}
