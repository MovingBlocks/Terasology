package org.terasology.world.chunks.blockdata;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.google.common.base.Preconditions;


/**
 * TeraArray is the base class used to store block related data in Chunks.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public abstract class TeraArray implements Externalizable {

    private int sizeX, sizeY, sizeZ, sizeXZ, sizeXZHalf, sizeXYZ, sizeXYZHalf;

    protected final void writeExternalHeader(ObjectOutput out) throws IOException {
        out.writeInt(sizeX);
        out.writeInt(sizeY);
        out.writeInt(sizeZ); 
    }

    protected final void readExternalHeader(ObjectInput in) throws IOException {
        sizeX = in.readInt();
        sizeY = in.readInt();
        sizeZ = in.readInt();
        sizeXZ = sizeX * sizeZ;
        sizeXZHalf = sizeXZ / 2;
        sizeXYZ = sizeY * sizeXZ;
        sizeXYZHalf = sizeXYZ / 2;
    }

    public TeraArray() {}

    public TeraArray(int sizeX, int sizeY, int sizeZ) {
        Preconditions.checkArgument(sizeX > 0);
        Preconditions.checkArgument(sizeY > 0);
        Preconditions.checkArgument(sizeZ > 0);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        sizeXZ = sizeX * sizeZ;
        sizeXZHalf = sizeXZ / 2;
        sizeXYZ = sizeY * sizeXZ;
        sizeXYZHalf = sizeXYZ / 2;
        Preconditions.checkArgument(getSizeXYZ() % 2 == 0, "The product of the parameters 'sizeX', 'sizeY' and 'sizeZ' has to be a multiple of 2 (" + getSizeXYZ() + ")");
        Preconditions.checkArgument(getSizeXZ() % 2 == 0, "The product of the parameters 'sizeX' and 'sizeZ' has to be a multiple of 2 (" + getSizeXZ() + ")");
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

    public final int getSizeXZ() {
        return sizeXZ;
    }
    
    public final int getSizeXZHalf() {
        return sizeXZHalf;
    }

    public final int getSizeXYZ() {
        return sizeXYZ;
    }
    
    public final int getSizeXYZHalf() {
        return sizeXYZHalf;
    }

    public final boolean contains(int x, int y, int z) {
        return (x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ);
    }

    public abstract boolean isSparse();

    public abstract TeraArray deflate();
    
    public abstract TeraArray inflate();

    public abstract TeraArray copy();

    public abstract int getEstimatedMemoryConsumptionInBytes();

    public abstract int get(int x, int y, int z);

    public abstract int set(int x, int y, int z, int value);

    public abstract boolean set(int x, int y, int z, int value, int expected);

}
