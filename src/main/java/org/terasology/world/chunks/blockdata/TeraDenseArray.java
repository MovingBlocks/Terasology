package org.terasology.world.chunks.blockdata;

public abstract class TeraDenseArray extends TeraArray {

    protected final int pos(int x, int y, int z) {
        return y * sizeXZ + z * sizeX + x;
    }

    public TeraDenseArray() {
        super();
    }

    public TeraDenseArray(int sizeX, int sizeY, int sizeZ, int sizeOfElementInBit) {
        super(sizeX, sizeY, sizeZ, sizeOfElementInBit);
    }

    @Override
    public boolean isPacked() {
        return false;
    }

}
