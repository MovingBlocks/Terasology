// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import com.google.common.base.Preconditions;
import org.joml.Vector2ic;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.generation.Border3D;

public abstract class BaseFieldFacet2D extends BaseFacet2D implements FieldFacet2D {

    private float[] data;

    public BaseFieldFacet2D(BlockRegionc targetRegion, Border3D border) {
        super(targetRegion, border);
        this.data = new float[getRelativeArea().area()];
    }

    @Override
    public float get(int x, int y) {
        return data[getRelativeIndex(x, y)];
    }

    @Override
    public float get(Vector2ic pos) {
        return get(pos.x(), pos.y());
    }

    @Override
    public float getWorld(int x, int y) {
        return data[getWorldIndex(x, y)];
    }

    @Override
    public float getWorld(Vector2ic pos) {
        return getWorld(pos.x(), pos.y());
    }

    public float[] getInternal() {
        return data;
    }

    @Override
    public void set(int x, int y, float value) {
        data[getRelativeIndex(x, y)] = value;
    }

    @Override
    public void set(Vector2ic pos, float value) {
        set(pos.x(), pos.y(), value);
    }

    @Override
    public void setWorld(int x, int y, float value) {
        data[getWorldIndex(x, y)] = value;
    }

    @Override
    public void setWorld(Vector2ic pos, float value) {
        setWorld(pos.x(), pos.y(), value);
    }

    public void set(float[] newData) {
        Preconditions.checkArgument(newData.length == data.length, "New data must have same length as existing");
        System.arraycopy(newData, 0, data, 0, newData.length);
    }
}
