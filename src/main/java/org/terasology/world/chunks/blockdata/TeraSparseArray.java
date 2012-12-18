package org.terasology.world.chunks.blockdata;

public abstract class TeraSparseArray extends TeraArray {

    public TeraSparseArray() {
        super();
    }

    public TeraSparseArray(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ);
    }

    @Override
    public final boolean isSparse() {
        return true;
    }
}
