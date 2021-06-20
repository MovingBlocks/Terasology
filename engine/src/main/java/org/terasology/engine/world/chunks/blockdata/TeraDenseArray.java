// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.blockdata;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * TeraDenseArray is the base class used to implement dense arrays.
 *
 */
public abstract class TeraDenseArray extends TeraArray {

    protected TeraDenseArray() {
        super();
    }

    protected TeraDenseArray(int sizeX, int sizeY, int sizeZ, boolean initialize) {
        super(sizeX, sizeY, sizeZ, initialize);
    }

    protected TeraDenseArray(TeraArray in) {
        super(checkNotNull(in).getSizeX(), in.getSizeY(), in.getSizeZ(), true);
        copyFrom(in);
    }

    public void copyFrom(TeraArray in) {
        checkNotNull(in);
        checkArgument(getSizeX() == in.getSizeX(),
                "Tera arrays have to be of equal size (this.getSizeX() = " + getSizeX() + ", in.getSizeX() = " + in.getSizeX() + ")");
        checkArgument(getSizeY() == in.getSizeY(),
                "Tera arrays have to be of equal size (this.getSizeY() = " + getSizeY() + ", in.getSizeY() = " + in.getSizeY() + ")");
        checkArgument(getSizeZ() == in.getSizeZ(),
                "Tera arrays have to be of equal size (this.getSizeZ() = " + getSizeZ() + ", in.getSizeZ() = " + in.getSizeZ() + ")");
        checkArgument(getElementSizeInBits() >= in.getElementSizeInBits(),
                "Tera arrays are incompatible (this.getElementSizeInBits() = " + getElementSizeInBits()
                        + ", in.getElementSizeInBits() = " + in.getElementSizeInBits() + ")");
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
