/*
 * Copyright 2013 Moving Blocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.world.chunks.blockdata;

import com.google.common.base.Preconditions;


/**
 * TeraDenseArray is the base class used to implement dense arrays.
 *
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 */
public abstract class TeraDenseArray extends TeraArray {

    protected TeraDenseArray() {
        super();
    }

    protected TeraDenseArray(int sizeX, int sizeY, int sizeZ, boolean initialize) {
        super(sizeX, sizeY, sizeZ, initialize);
    }

    protected TeraDenseArray(TeraArray in) {
        super(Preconditions.checkNotNull(in).getSizeX(), in.getSizeY(), in.getSizeZ(), true);
        copyFrom(in);
    }

    public void copyFrom(TeraArray in) {
        Preconditions.checkNotNull(in);
        Preconditions.checkArgument(getSizeX() == in.getSizeX(), "Tera arrays have to be of equal size (this.getSizeX() = " + getSizeX() + ", in.getSizeX() = " + in.getSizeX() + ")");
        Preconditions.checkArgument(getSizeY() == in.getSizeY(), "Tera arrays have to be of equal size (this.getSizeY() = " + getSizeY() + ", in.getSizeY() = " + in.getSizeY() + ")");
        Preconditions.checkArgument(getSizeZ() == in.getSizeZ(), "Tera arrays have to be of equal size (this.getSizeZ() = " + getSizeZ() + ", in.getSizeZ() = " + in.getSizeZ() + ")");
        Preconditions.checkArgument(getElementSizeInBits() >= in.getElementSizeInBits(), "Tera arrays are incompatible (this.getElementSizeInBits() = " + getElementSizeInBits() + ", in.getElementSizeInBits() = " + in.getElementSizeInBits() + ")");
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
