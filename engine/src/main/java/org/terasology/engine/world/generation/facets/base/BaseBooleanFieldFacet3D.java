// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import com.google.common.base.Preconditions;
import org.joml.Vector3ic;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;

public abstract class BaseBooleanFieldFacet3D extends BaseFacet3D implements BooleanFieldFacet3D {

    private boolean[] data;

    public BaseBooleanFieldFacet3D(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
        data =
            new boolean[getRelativeRegion().volume()];
    }

    @Override
    public boolean get(int x, int y, int z) {
        return data[getRelativeIndex(x, y, z)];
    }

    @Override
    public boolean get(Vector3ic pos) {
        return get(pos.x(), pos.y(), pos.z());
    }

    @Override
    public boolean getWorld(int x, int y, int z) {
        return data[getWorldIndex(x, y, z)];
    }

    @Override
    public boolean getWorld(Vector3ic pos) {
        return getWorld(pos.x(), pos.y(), pos.z());
    }

    public boolean[] getInternal() {
        return data;
    }

    @Override
    public void set(int x, int y, int z, boolean value) {
        data[getRelativeIndex(x, y, z)] = value;
    }

    @Override
    public void set(Vector3ic pos, boolean value) {
        set(pos.x(), pos.y(), pos.z(), value);
    }

    @Override
    public void setWorld(int x, int y, int z, boolean value) {
        data[getWorldIndex(x, y, z)] = value;
    }

    @Override
    public void setWorld(Vector3ic pos, boolean value) {
        setWorld(pos.x(), pos.y(), pos.z(), value);
    }

    public void set(boolean[] newData) {
        Preconditions.checkArgument(newData.length == data.length);
        System.arraycopy(newData, 0, data, 0, newData.length);
    }
}
