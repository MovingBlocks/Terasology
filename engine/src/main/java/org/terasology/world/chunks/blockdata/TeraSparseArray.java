// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.blockdata;


/**
 * TeraSparseArray is the base class used to implement sparse arrays.
 *
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
