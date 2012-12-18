package org.terasology.world.chunks.blockdata;


/**
 * TeraDenseArray is the base class used to implement dense arrays.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public abstract class TeraDenseArray extends TeraArray {

    protected final int pos(int x, int y, int z) {
        return y * getSizeXZ() + z * getSizeX() + x;
    }

    protected final int pos(int x, int z) {
        return z * getSizeX() + x;
    }

    public TeraDenseArray() {
        super();
    }

    public TeraDenseArray(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ);
    }

    @Override
    public final boolean isSparse() {
        return false;
    }

}
