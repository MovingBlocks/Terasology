package org.terasology.world.chunks.blockdata;


/**
 * TeraSparseArray is the base class used to implement sparse arrays.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
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
