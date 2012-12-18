package org.terasology.world.chunks.blockdata;

public abstract class TeraSparseArray extends TeraArray {

    public TeraSparseArray() {
        super();
    }

    public TeraSparseArray(int sizeX, int sizeY, int sizeZ, int sizeOfElementInBit) {
        super(sizeX, sizeY, sizeZ, sizeOfElementInBit);
    }

    @Override
    public final boolean isSparse() {
        return true;
    }
}
