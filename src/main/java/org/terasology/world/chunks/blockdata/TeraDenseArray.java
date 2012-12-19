package org.terasology.world.chunks.blockdata;

import com.google.common.base.Preconditions;


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
    
    public TeraDenseArray(TeraArray in) {
        super(Preconditions.checkNotNull(in).getSizeX(), in.getSizeY(), in.getSizeZ());
        copyFrom(in);
    }

    public void copyFrom(TeraArray in) {
        Preconditions.checkNotNull(in);
        Preconditions.checkArgument(getSizeX() == in.getSizeX(), "Tera arrays have to be of equal size (this.getSizeX() = " + getSizeX() + ", in.getSizeX() = " + in.getSizeX() + ")");
        Preconditions.checkArgument(getSizeY() == in.getSizeY(), "Tera arrays have to be of equal size (this.getSizeY() = " + getSizeY() + ", in.getSizeY() = " + in.getSizeY() + ")");
        Preconditions.checkArgument(getSizeZ() == in.getSizeZ(), "Tera arrays have to be of equal size (this.getSizeZ() = " + getSizeZ() + ", in.getSizeZ() = " + in.getSizeZ() + ")");
        for (int y = 0; y < getSizeY(); y++) {
            for (int x = 0; x < getSizeX(); x++) {
                for (int z = 0; z < getSizeZ(); z++) {
                    set(x, y, z, in.get(x, y, z));
                }
            }
        }
    }

    @Override
    public final boolean isSparse() {
        return false;
    }

}
