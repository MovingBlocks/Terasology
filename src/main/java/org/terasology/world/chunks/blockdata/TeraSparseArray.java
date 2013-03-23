package org.terasology.world.chunks.blockdata;


/**
 * TeraSparseArray is the base class used to implement sparse arrays.
 *
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 */
public abstract class TeraSparseArray extends TeraArray {

    protected TeraSparseArray() {
        super();
    }

    protected TeraSparseArray(int sizeX, int sizeY, int sizeZ, boolean initialize) {
        super(sizeX, sizeY, sizeZ, initialize);
    }

    @Override
    public final boolean isSparse() {
        return true;
    }
}
